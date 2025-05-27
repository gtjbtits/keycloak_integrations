from http.client import HTTPSConnection as sc, HTTPConnection as c
from shutil import copyfile, move
from urllib.parse import urlparse
import json, time, os
import subprocess
import logging


logger = logging.getLogger(f"[{__file__}]")
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(message)s")

PROXY_MODE_AUTO = "auto"
PROXY_MODE_GLOBAL = "global"
PROXY_MODE_LOCAL = "local"


def get_proxy_mode_from_env(default_mode):
    proxy_mode = os.environ.get("PROXY_MODE", default_mode)
    if proxy_mode not in (PROXY_MODE_AUTO, PROXY_MODE_GLOBAL, PROXY_MODE_LOCAL):
        proxy_mode = PROXY_MODE_AUTO
    logger.info(f"The proxy is currently operating in '{proxy_mode}' mode")
    return proxy_mode


def get_int_from_env(variable_name, default_value):
    str_value = os.environ.get(variable_name)
    try:
        value = int(str_value)
    except Exception:
        logger.warning(f"Value for env {variable_name} not found or had invalid format. Default value will be used: {default_value}")
        value = default_value
    return value


PROXY_MODE = get_proxy_mode_from_env(PROXY_MODE_AUTO)
GLOBAL_KC_JWKS_URL = os.environ.get("GLOBAL_KC_JWKS_URL", "")
LOCAL_KC_JWKS_URL = os.environ.get("LOCAL_KC_JWKS_URL", "")
SHARED_JWKS_FILE_NAME = "shared_jwks.json"
CHECK_INTERVAL_SECONDS = get_int_from_env("CHECK_INTERVAL_SECONDS", 10)
CONNECTION_TIMEOUT_SECONDS = get_int_from_env("CONNECTION_TIMEOUT_SECONDS", 30)

global_kc_was_alive = None


def get_jwks(url):
    parsed_url = urlparse(url)
    scheme = parsed_url.scheme
    host = parsed_url.hostname
    port = parsed_url.port
    path = parsed_url.path
    try:
        if scheme == "https":
            if not port:
                port = 443
            connection = sc(host, port=port, timeout=CONNECTION_TIMEOUT_SECONDS)
        elif scheme == "http":
            if not port:
                port = 80
            connection = c(host, port=port, timeout=CONNECTION_TIMEOUT_SECONDS)
        else:
            raise ConnectionError(f"Can't parse url")
        connection.request("GET", path)
        response = connection.getresponse()
        if response.getcode() == 200:
            content = response.read()
            jwks = json.loads(content)
            return jwks
        logger.error(f"Request to '{url}' failed. Response: {response.status} {response.reason}")
        return None
    except Exception as e:
        logger.error(f"Error while obtaining JWKS from {url}: {e}")
        return None


def keys_list_to_kids_dict(keys):
    kids = dict()
    for key in keys:
        kid = key["kid"]
        kids[kid] = key
    return kids


def kids_dict_to_keys_list(kids):
    keys = list()
    for kid in kids:
        keys.append(kids[kid])
    return keys


def merge_shared_jwks_with_instance_jwks(jwks_shared, jwks_url):
    jwks = get_jwks(jwks_url)
    if (jwks and jwks["keys"]):
        instance_kids = keys_list_to_kids_dict(jwks["keys"])
        shared_kids = keys_list_to_kids_dict(jwks_shared["keys"])
        shared_kids |= instance_kids
        jwks_shared["keys"] = kids_dict_to_keys_list(shared_kids)
        return True
    return False


def move_if_exist(src, dst):
    if os.path.isfile(src):
        move(src, dst)


def switch_proxy_configuration(global_kc_alive):
    if global_kc_alive:
        copyfile(src="./nginx/configs/global.conf", dst="/etc/nginx/conf.d/proxy.conf")
        move_if_exist(src="/etc/nginx/conf.d/keycloak_global.conf.bak", dst="/etc/nginx/conf.d/keycloak_global.conf")
        move_if_exist(src="/etc/nginx/conf.d/keycloak_local.conf", dst="/etc/nginx/conf.d/keycloak_local.conf.bak")
    else:
        copyfile(src="./nginx/configs/local.conf", dst="/etc/nginx/conf.d/proxy.conf")
        move_if_exist(src="/etc/nginx/conf.d/keycloak_local.conf.bak", dst="/etc/nginx/conf.d/keycloak_local.conf")
        move_if_exist(src="/etc/nginx/conf.d/keycloak_global.conf", dst="/etc/nginx/conf.d/keycloak_global.conf.bak")

def reload_nginx():
    result = subprocess.run(["nginx", "-s", "reload"], capture_output=True, text=True)
    if result.returncode == 0:
        logger.info("Nginx configuration reloaded")
        return True
    else:
        logger.error(f"Nginx configuration error:\n{result.stderr}")
        return False


def update_shared_jwks(proxy_mode):
    global global_kc_was_alive
    try:
        jwks_shared = None
        with open(SHARED_JWKS_FILE_NAME) as f:
            content = f.read()
            jwks_shared = json.loads(content)
    except Exception:
        logger.warning("File with shared JWKS not exists. Starting with blank JWKS")
        jwks_shared = dict()
        jwks_shared["keys"] = []
    global_kc_alive = merge_shared_jwks_with_instance_jwks(jwks_shared, jwks_url=GLOBAL_KC_JWKS_URL)
    merge_shared_jwks_with_instance_jwks(jwks_shared, jwks_url=LOCAL_KC_JWKS_URL)
    global_kc_alive_changed = global_kc_alive != global_kc_was_alive
    with open(SHARED_JWKS_FILE_NAME, "w") as f:
        f.write(json.dumps(jwks_shared))
    if proxy_mode != PROXY_MODE_AUTO or global_kc_alive_changed:
        if proxy_mode == PROXY_MODE_LOCAL:
            switch_proxy_configuration(False)
        elif proxy_mode == PROXY_MODE_GLOBAL:
            switch_proxy_configuration(True)
        else:
            switch_proxy_configuration(global_kc_alive)
        if reload_nginx():
            global_kc_was_alive = global_kc_alive

updated = False
while True:
    if not updated or PROXY_MODE == PROXY_MODE_AUTO:
        update_shared_jwks(PROXY_MODE)
        updated = True
    time.sleep(CHECK_INTERVAL_SECONDS)

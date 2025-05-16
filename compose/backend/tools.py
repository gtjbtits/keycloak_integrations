from functools import wraps
import jwt
from jwt import PyJWKClient
from flask import request, jsonify
import os

JWKS_URL = os.environ["JWKS_URL"]

def abort(message, http_code):
    return jsonify(message=str(message)), http_code

def parse_token(jwks_url, token):
    print(jwks_url)
    jwks_client = PyJWKClient(jwks_url)
    signing_key = jwks_client.get_signing_key_from_jwt(token)
    payload = jwt.decode(
        token,
        signing_key,
        options={
            "verify_exp": True,
            "verify_aud": False, 
        },
        algorithms=["RS256"],
    )
    return payload

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        if "Authorization" in request.headers:
            token = request.headers["Authorization"].split(" ")[1]
        if not token:
            return abort("Authentication Token is missing!", 401)
        payload = None
        try:
            payload = parse_token(JWKS_URL, token)
        except Exception as e:
            print(e)
            pass
        if not payload:
            return abort("Token processing error", 500)

        return f(*args, **kwargs)

    return decorated
<?php

require __DIR__ . '/vendor/autoload.php';

use JakubOnderka\OpenIDConnectClient;

$oidc = new OpenIDConnectClient("http://127.0.0.1:38080/realms/test", "test", "wMJ70nSwIgQudQo0yqzfuVmueK99l308");
$oidc->setRedirectURL("http://127.0.0.1:38001/");
$oidc->authenticate();
$name = $oidc->requestUserInfo("given_name");

echo "Hello " . $name . "!";
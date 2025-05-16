# Requirements

* Docker

# Launch a Keycloak in Docker
```bash
docker run -p 38080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:24.0.2 start-dev
```

 openssl req -x509 -newkey rsa:4096 -sha256 -days 3650 \
  -nodes -keyout key.pem -out cert.pem -subj "/CN=keycloak24.local" \
  -addext "subjectAltName=DNS:keycloak24.local"

  openssl req -x509 -newkey rsa:4096 -sha256 -days 3650 \
  -nodes -keyout key.pem -out cert.pem -subj "/CN=keycloak16.local" \
  -addext "subjectAltName=DNS:keycloak16.local"
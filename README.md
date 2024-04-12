# Requirements

* Docker

# Launch a Keycloak in Docker
```bash
docker run -p 38080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:24.0.2 start-dev
```
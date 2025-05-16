#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE DATABASE keycloak16;
	CREATE DATABASE keycloak25;
	CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak';
	GRANT ALL PRIVILEGES ON DATABASE keycloak16 TO keycloak;
	GRANT ALL PRIVILEGES ON DATABASE keycloak25 TO keycloak;
EOSQL

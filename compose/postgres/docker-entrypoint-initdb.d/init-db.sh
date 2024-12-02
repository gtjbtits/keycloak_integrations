#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE DATABASE keycloak16;
	CREATE DATABASE keycloak24;
	CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak';
	GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak16;
	GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak24;
EOSQL

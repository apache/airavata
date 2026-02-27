-- Airavata Database Initialization
-- Creates the Airavata and Keycloak databases for local development.
-- Used by: docker-compose, Ansible, K8s init jobs.

CREATE DATABASE IF NOT EXISTS airavata
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'airavata'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON airavata.* TO 'airavata'@'%';

CREATE DATABASE IF NOT EXISTS keycloak
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'keycloak'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON keycloak.* TO 'keycloak'@'%';

FLUSH PRIVILEGES;

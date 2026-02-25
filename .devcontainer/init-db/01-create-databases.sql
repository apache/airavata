-- Airavata Database Initialization (devcontainer)
-- Creates the unified Airavata database and user for local development.
-- For production, use conf/db/create-database.sql from the distribution.

CREATE DATABASE IF NOT EXISTS airavata
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'airavata'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON airavata.* TO 'airavata'@'%';
FLUSH PRIVILEGES;

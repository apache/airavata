-- Airavata Database Initialization Script
-- Creates the unified Airavata database with all tables

-- Create the unified Airavata database
CREATE DATABASE IF NOT EXISTS airavata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant all privileges to the airavata user
GRANT ALL PRIVILEGES ON airavata.* TO 'airavata'@'%';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

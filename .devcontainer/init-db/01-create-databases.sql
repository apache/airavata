-- Airavata Database Initialization Script
-- Creates all required databases and grants privileges to the airavata user

-- Create all required databases
CREATE DATABASE IF NOT EXISTS profile_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS app_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS experiment_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS replica_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS workflow_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sharing_registry CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS credential_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS research_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant all privileges on all databases to the airavata user
GRANT ALL PRIVILEGES ON profile_service.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON app_catalog.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON experiment_catalog.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON replica_catalog.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON workflow_catalog.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON sharing_registry.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON credential_store.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON research_catalog.* TO 'airavata'@'%';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

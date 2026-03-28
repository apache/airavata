-- Research service uses the shared airavata database.
-- This script ensures the user has access.

CREATE USER IF NOT EXISTS 'airavata'@'%' IDENTIFIED BY '123456';
ALTER USER 'airavata'@'%' IDENTIFIED BY '123456';

GRANT ALL PRIVILEGES ON airavata.* TO 'airavata'@'%';

FLUSH PRIVILEGES;

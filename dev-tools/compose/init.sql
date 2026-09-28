-- Grant all privileges to airavata user
GRANT ALL PRIVILEGES ON airavata.* TO 'airavata'@'%';
GRANT ALL PRIVILEGES ON airavata.* TO 'airavata'@'localhost';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

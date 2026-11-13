-- Let an SSH credential store a password for the login's password prompt. The DDL is
-- what AutoMigrate emits for SSHCredential.Password — see
-- TestBaselineMigrationMatchesAutoMigrate.

ALTER TABLE "ssh_credentials" ADD COLUMN "password" varchar(255);

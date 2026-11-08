-- Service accounts: non-human users that authenticate with an Airavata-issued token.
--
-- A service account is an ordinary "users" row with auth_method SERVICE_ACCOUNT and an
-- id of the form "sa:<name>", so it owns resources, receives shares and joins groups
-- through the existing tables. This table carries only what a person does not have:
-- the digest of the account's single active token and its validity window. The token
-- itself is never stored.
--
-- The statements are the ones GORM emits for iam/model.ServiceAccount, which is what
-- keeps this file and AutoMigrate in agreement — see
-- TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "service_accounts" ("user_id" varchar(255),"description" varchar(1024),"created_by" varchar(255) NOT NULL,"token_hash" varchar(64) NOT NULL,"token_issued_at" bigint NOT NULL,"token_expires_at" bigint NOT NULL,PRIMARY KEY ("user_id"),CONSTRAINT "fk_users_service_account" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE UNIQUE INDEX IF NOT EXISTS "uk_service_account_token_hash" ON "service_accounts" ("token_hash");

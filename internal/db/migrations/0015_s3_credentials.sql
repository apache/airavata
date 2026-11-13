-- Access keys for S3-compatible object stores, kept beside SSH credentials in the
-- credential store as a model of their own. secret_access_key and session_token are
-- write-only through the API, and a credential belongs to whoever registered it. The
-- DDL is what AutoMigrate emits for credentials/model.S3Credential — see
-- TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "s3_credentials" ("s3_credential_id" varchar(36),"name" varchar(255) NOT NULL,"access_key_id" varchar(255) NOT NULL,"secret_access_key" text NOT NULL,"session_token" text,"owner_id" varchar(255) NOT NULL,PRIMARY KEY ("s3_credential_id"),CONSTRAINT "fk_s3_credentials_owner" FOREIGN KEY ("owner_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_s3_credentials_owner_id" ON "s3_credentials" ("owner_id");

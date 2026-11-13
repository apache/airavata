-- Buckets on S3-compatible object stores, the object-store counterpart of
-- scp_data_storages: where the bucket is, and the S3 credential presented for it.
-- Shares of one live in resource_sharings under S3_DATA_STORAGE, which needs no
-- schema change: resource_type is a plain varchar. The DDL is what AutoMigrate emits
-- for data/model.S3DataStorage — see TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "s3_data_storages" ("data_id" varchar(36),"data_name" varchar(255),"endpoint_url" varchar(2048),"region" varchar(64) NOT NULL,"bucket_name" varchar(63) NOT NULL,"path_style" boolean NOT NULL DEFAULT false,"s3_credential_id" varchar(36),"user_id" varchar(255),PRIMARY KEY ("data_id"),CONSTRAINT "fk_s3_data_storages_s3_credential" FOREIGN KEY ("s3_credential_id") REFERENCES "s3_credentials"("s3_credential_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_s3_data_storages_owner" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_s3_data_storages_s3_credential_id" ON "s3_data_storages" ("s3_credential_id");

CREATE INDEX IF NOT EXISTS "idx_s3_data_storages_owner_id" ON "s3_data_storages" ("user_id");

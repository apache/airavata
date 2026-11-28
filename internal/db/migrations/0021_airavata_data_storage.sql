-- The storage each user is given on the host Airavata runs on: a directory under
-- AIRAVATA_DATA_STORAGE_PATH where data waits before a job is submitted, for users who
-- do not want to register a permanent storage. A user has at most one, which the
-- unique index on user_id enforces. The owner reference is RESTRICT, like
-- scp_data_storages: a user who still has a storage cannot be deleted out from under it.
-- The DDL is what AutoMigrate emits — see TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "airavata_data_storage" ("data_id" varchar(36),"data_name" varchar(255),"data_expiry_timeout" bigint,"user_id" varchar(255),"root_directory" varchar(255),PRIMARY KEY ("data_id"),CONSTRAINT "fk_airavata_data_storage_owner" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE UNIQUE INDEX IF NOT EXISTS "idx_airavata_data_storage_owner_id" ON "airavata_data_storage" ("user_id");

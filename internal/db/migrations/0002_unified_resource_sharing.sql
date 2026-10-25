-- Collapse the platform's eight sharing tables into one.
--
-- Storages, products, virtual directories and Slurm cluster configs each kept a pair,
-- one per subject kind, differing only in which column named the resource and which
-- named the subject. Here the resource and the subject are each a (type, id) pair, so
-- one query answers "what is shared with this caller" for any of them, and a new
-- shareable record adds a constant rather than two tables and a third copy of the
-- rule for resolving them.
--
-- Neither id can carry a foreign key: a column cannot reference four tables, nor users
-- and groups at once. The services delete a record's shares alongside the record, and
-- the group service withdraws a deleted group's grants.

CREATE TABLE "resource_sharings" ("resource_sharing_id" varchar(36),"resource_type" varchar(32) NOT NULL,"resource_id" varchar(36) NOT NULL,"principal_type" varchar(32) NOT NULL,"principal_id" varchar(255) NOT NULL,"permission" varchar(32),PRIMARY KEY ("resource_sharing_id"));

CREATE INDEX IF NOT EXISTS "idx_resource_sharings_principal_id" ON "resource_sharings" ("principal_id");

CREATE UNIQUE INDEX IF NOT EXISTS "uk_resource_sharing" ON "resource_sharings" ("resource_type","resource_id","principal_type","principal_id");

CREATE INDEX IF NOT EXISTS "idx_resource_sharings_resource" ON "resource_sharings" ("resource_type","resource_id");

-- Carry the existing grants over. The primary keys are reused, so a client holding a
-- sharing id keeps addressing the same grant.
--
-- Rows with no subject, no resource or no permission are left behind: they granted
-- nothing, the old columns being nullable, and the new table refuses them.

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "data_storage_group_sharing_id",'SCP_DATA_STORAGE',"data_storage_id",'GROUP',"group_id","permission"
FROM "scp_data_storage_group_sharings"
WHERE "data_storage_id" IS NOT NULL AND "group_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "data_storage_user_sharing_id",'SCP_DATA_STORAGE',"data_storage_id",'USER',"user_id","permission"
FROM "scp_data_storage_user_sharings"
WHERE "data_storage_id" IS NOT NULL AND "user_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "data_product_group_sharing_id",'DATA_PRODUCT',"data_product_id",'GROUP',"group_id","permission"
FROM "data_product_group_sharings"
WHERE "data_product_id" IS NOT NULL AND "group_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "data_product_user_sharing_id",'DATA_PRODUCT',"data_product_id",'USER',"user_id","permission"
FROM "data_product_user_sharings"
WHERE "data_product_id" IS NOT NULL AND "user_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "virtual_data_directory_group_sharing_id",'VIRTUAL_DATA_DIRECTORY',"virtual_data_directory_id",'GROUP',"group_id","permission"
FROM "virtual_data_directory_group_sharings"
WHERE "virtual_data_directory_id" IS NOT NULL AND "group_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "virtual_data_directory_user_sharing_id",'VIRTUAL_DATA_DIRECTORY',"virtual_data_directory_id",'USER',"user_id","permission"
FROM "virtual_data_directory_user_sharings"
WHERE "virtual_data_directory_id" IS NOT NULL AND "user_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "slurm_cluster_config_group_sharing_id",'SLURM_CLUSTER_CONFIG',"slurm_cluster_config_id",'GROUP',"group_id","permission"
FROM "slurm_cluster_config_group_sharings"
WHERE "slurm_cluster_config_id" IS NOT NULL AND "group_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

INSERT INTO "resource_sharings" ("resource_sharing_id","resource_type","resource_id","principal_type","principal_id","permission")
SELECT "slurm_cluster_config_user_sharing_id",'SLURM_CLUSTER_CONFIG',"slurm_cluster_config_id",'USER',"user_id","permission"
FROM "slurm_cluster_config_user_sharings"
WHERE "slurm_cluster_config_id" IS NOT NULL AND "user_id" IS NOT NULL AND "permission" IN ('READ','WRITE');

DROP TABLE "slurm_cluster_config_group_sharings";

DROP TABLE "slurm_cluster_config_user_sharings";

DROP TABLE "scp_data_storage_group_sharings";

DROP TABLE "scp_data_storage_user_sharings";

DROP TABLE "data_product_group_sharings";

DROP TABLE "data_product_user_sharings";

DROP TABLE "virtual_data_directory_group_sharings";

DROP TABLE "virtual_data_directory_user_sharings";

-- A data product can now have several locations, on storages of different kinds.
--
-- data_products keeps its data_storage_id, data_storage_type and path columns, which
-- mirror the primary replica. data_storage_id here carries no foreign key, for the
-- reason the one on data_products does not: it is qualified by a storage type. The DDL
-- is what AutoMigrate emits for data/model.DataProductReplica — see
-- TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "data_product_replicas" ("replica_id" varchar(36),"data_product_id" varchar(36) NOT NULL,"data_storage_id" varchar(36) NOT NULL,"data_storage_type" varchar(32) NOT NULL,"path" varchar(2048) NOT NULL,"is_primary" boolean NOT NULL,"created_at" bigint NOT NULL,PRIMARY KEY ("replica_id"),CONSTRAINT "fk_data_products_replicas" FOREIGN KEY ("data_product_id") REFERENCES "data_products"("data_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_data_product_replicas_data_product_id" ON "data_product_replicas" ("data_product_id");

CREATE INDEX IF NOT EXISTS "idx_data_product_replicas_data_storage_id" ON "data_product_replicas" ("data_storage_id");

-- Give every located product its primary replica. The replica takes the product's id,
-- which is what marks it as that product's one primary row. A blank storage type is
-- SCP, the only kind a product could be registered on. Products with no storage or no
-- path name no location, so they get no row.

INSERT INTO "data_product_replicas" ("replica_id","data_product_id","data_storage_id","data_storage_type","path","is_primary","created_at")
SELECT "data_id","data_id","data_storage_id",COALESCE(NULLIF("data_storage_type",''),'SCP'),"path",true,"created_at"
FROM "data_products"
WHERE "data_storage_id" IS NOT NULL AND "path" IS NOT NULL AND "path" <> '';

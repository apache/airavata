-- Permission-based authorization: which permissions each role grants.
--
-- Handlers check a permission such as "audit:read"; this table says which roles hold it,
-- so changing who may do something is a data change. The first statement is what GORM
-- emits for iam/model.RolePermission, which keeps this file and AutoMigrate in
-- agreement — see TestBaselineMigrationMatchesAutoMigrate.
--
-- The rows are the defaults in internal/permission.Defaults, which reproduce what the
-- ADMIN/SUPER_ADMIN checks allowed before permissions existed. SUPER_ADMIN has no rows:
-- it holds every permission by definition and is never read from this table.

CREATE TABLE "role_permissions" ("role" varchar(32),"permission" varchar(64),PRIMARY KEY ("role","permission"));

INSERT INTO "role_permissions" ("role","permission") VALUES ('ADMIN','audit:read');

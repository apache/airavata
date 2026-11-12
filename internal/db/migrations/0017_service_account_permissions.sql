-- Per-account permissions for service accounts.
--
-- A service account holds no role that grants anything, so what it may do beyond acting
-- on its own records is exactly the rows here, and each account can differ from the
-- next. The rows belong to the account and are deleted with it. The permissions that
-- manage identities and privileges are refused on write by the API and ignored on read
-- by the resolver (see permission.GrantableToServiceAccount), so no row here can make a
-- service account an administrator.
--
-- The statement is what GORM emits for iam/model.ServiceAccountPermission, which keeps
-- this file and AutoMigrate in agreement — see TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "service_account_permissions" ("user_id" varchar(255),"permission" varchar(64),PRIMARY KEY ("user_id","permission"),CONSTRAINT "fk_service_accounts_permissions" FOREIGN KEY ("user_id") REFERENCES "service_accounts"("user_id") ON DELETE CASCADE ON UPDATE CASCADE);

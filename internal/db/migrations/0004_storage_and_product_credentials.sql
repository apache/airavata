-- A storage names the account its data is reached as; a product names no credential.
--
-- scp_data_storages already carried the host, as ssh_endpoint_id. It now carries the
-- account beside it, as ssh_user_credential_id — a username and key, not a binding. The
-- two are named separately rather than as one ssh_endpoint_credentials row because a
-- binding also carries an owner, and a storage is not staged under one person's standing
-- on the host: whoever holds a share reaches it under their own binding for that same
-- host and account.
--
-- There is no backfill. No account can be inferred for a storage that predates the
-- column — the bindings on its endpoint may name several usernames, or none — so the
-- column is left null rather than guessed at. Every existing row therefore needs its
-- account filled in before it is usable; the create and update paths require
-- sshCredentialId, so the next write through the API supplies one.
--
-- data_products.credential_id was the binding a dataset had been staged under, checked
-- on write to be for its storage's own host. Now that the storage names both the host
-- and the account, the column said nothing its storage did not already say, so it goes.
-- Dropping it takes idx_data_products_credential_id with it; there was never a foreign
-- key, the reference being qualified by data_storage_type instead.
--
-- Both columns are changed here rather than left to AutoMigrate, which adds but never
-- drops: a leftover credential_id would drift a migrated database from a development
-- one, which is what TestBaselineMigrationMatchesAutoMigrate catches.

ALTER TABLE "scp_data_storages" ADD "ssh_user_credential_id" varchar(36);

ALTER TABLE "scp_data_storages"
	ADD CONSTRAINT "fk_scp_data_storages_ssh_user_credential"
	FOREIGN KEY ("ssh_user_credential_id") REFERENCES "ssh_user_credentials"("ssh_credential_id")
	ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX IF NOT EXISTS "idx_scp_data_storages_ssh_user_credential_id"
	ON "scp_data_storages" ("ssh_user_credential_id");

ALTER TABLE "data_products" DROP COLUMN "credential_id";

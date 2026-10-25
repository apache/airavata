-- Rename the SSH key entity to SSH credential, and record the two authentication
-- flags it gained.
--
-- "Key" named the material; "credential" names what the platform actually holds and
-- presents. The distinction stopped being cosmetic once a row could carry a
-- certificate or demand a full interactive login instead of a bare keypair, which is
-- what is_certificate and full_interactive record. Those are the same booleans the
-- entity model declares; existing rows are plain keypairs, so both default to false
-- for the rewrite and the default is then dropped to match what AutoMigrate emits.
--
-- Everything here is a rename, so no data moves and no id changes: a client holding a
-- credential id keeps addressing the same row. The names are the ones GORM derives
-- from the renamed Go identifiers, which is what keeps this file and AutoMigrate in
-- agreement — see TestBaselineMigrationMatchesAutoMigrate.
--
-- This is a migration rather than an eighth re-recording of the baseline because 0002
-- has already run: a database that applied it records version 2 and would skip a
-- rewritten 0001 entirely.

ALTER TABLE "ssh_keys" RENAME TO "ssh_credentials";

ALTER TABLE "ssh_credentials" RENAME COLUMN "ssh_key_id" TO "ssh_credential_id";

ALTER TABLE "ssh_credentials" RENAME COLUMN "ssh_key_name" TO "ssh_credential_name";

ALTER TABLE "ssh_credentials" RENAME CONSTRAINT "fk_ssh_keys_owner" TO "fk_ssh_credentials_owner";

ALTER INDEX "ssh_keys_pkey" RENAME TO "ssh_credentials_pkey";

ALTER INDEX "idx_ssh_keys_owner_id" RENAME TO "idx_ssh_credentials_owner_id";

ALTER TABLE "ssh_credentials" ADD COLUMN "is_certificate" boolean NOT NULL DEFAULT false;

ALTER TABLE "ssh_credentials" ALTER COLUMN "is_certificate" DROP DEFAULT;

ALTER TABLE "ssh_credentials" ADD COLUMN "full_interactive" boolean NOT NULL DEFAULT false;

ALTER TABLE "ssh_credentials" ALTER COLUMN "full_interactive" DROP DEFAULT;

-- The two tables that present a credential. Neither reference changes target; only the
-- column and the names derived from it do.

ALTER TABLE "scp_data_storages" RENAME COLUMN "ssh_key_id" TO "ssh_credential_id";

ALTER TABLE "scp_data_storages" RENAME CONSTRAINT "fk_scp_data_storages_ssh_key" TO "fk_scp_data_storages_ssh_credential";

ALTER INDEX "idx_scp_data_storages_ssh_key_id" RENAME TO "idx_scp_data_storages_ssh_credential_id";

ALTER TABLE "slurm_cluster_configs" RENAME COLUMN "ssh_key_id" TO "ssh_credential_id";

ALTER TABLE "slurm_cluster_configs" RENAME CONSTRAINT "fk_slurm_cluster_configs_ssh_key" TO "fk_slurm_cluster_configs_ssh_credential";

ALTER INDEX "idx_slurm_cluster_configs_ssh_key_id" RENAME TO "idx_slurm_cluster_configs_ssh_credential_id";

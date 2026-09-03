-- A deployment's resource request and partition are defaults; its credential is gone.
--
-- default_submission_credential_id was the binding a run submitted under when it named
-- none of its own. Which identity a job runs as is a property of the run, not of the
-- deployment it was launched from, so batch_processes.submission_credential_id is now
-- the only place it is recorded and a run has to name one. Dropping the column takes
-- its foreign key and index with it.
--
-- The two remaining columns are renamed to say what they are. A run copies the
-- deployment's resource request into a batch_job_configs row of its own and may depart
-- from it, so what the deployment holds is a starting point rather than what any
-- particular job is submitted with — the same relationship the partition has.
--
-- The constraint and the index are renamed alongside the column they guard: AutoMigrate
-- derives both names from the field, so leaving them as they were would drift a
-- migrated database from a development one, which is what
-- TestBaselineMigrationMatchesAutoMigrate catches.

ALTER TABLE "batch_application_deployments" DROP COLUMN "default_submission_credential_id";

ALTER TABLE "batch_application_deployments" RENAME COLUMN "batch_job_config_id" TO "default_batch_job_config_id";
ALTER TABLE "batch_application_deployments" RENAME COLUMN "partition" TO "default_partition";

ALTER TABLE "batch_application_deployments"
	RENAME CONSTRAINT "fk_batch_application_deployments_batch_job_config"
	TO "fk_batch_application_deployments_default_batch_job_config";

ALTER INDEX "idx_batch_application_deployments_batch_job_config_id"
	RENAME TO "idx_batch_application_deployments_default_batch_job_config_id";

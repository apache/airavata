-- The work directory moved from the deployment to the run.
--
-- batch_application_deployments.work_dir was one directory for every run of a
-- deployment; batch_processes.base_work_dir is the directory a particular run works
-- under, with the run's own subdirectory beneath it named for the process id. That is
-- where the launch path builds every staging path from, so it has to be a property of
-- the run for two runs of one deployment to be able to work under different
-- directories — the same reason the resource request lives on the batch process
-- rather than being copied from the deployment's default.
--
-- The backfill gives every existing run the directory its deployment was configured
-- with, so a run created before this migration keeps working under the path it was
-- submitted against. A run whose deployment had none stays null, which the launch
-- path rejects rather than guessing at.
--
-- work_dir goes last, once nothing reads it: the column is dropped rather than left
-- in place because the entity model no longer declares it, and AutoMigrate never
-- drops a column — a leftover here would drift a migrated database from a
-- development one, which is exactly what TestBaselineMigrationMatchesAutoMigrate
-- catches.

ALTER TABLE "batch_processes" ADD "base_work_dir" varchar(1024);

UPDATE "batch_processes" SET "base_work_dir" = (
	SELECT "batch_application_deployments"."work_dir" FROM "batch_application_deployments"
	WHERE "batch_application_deployments"."deployment_id" = "batch_processes"."deployment_id");

ALTER TABLE "batch_application_deployments" DROP COLUMN "work_dir";

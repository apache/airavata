-- Move the partition from the deployment into the job config, so a run can choose it.

ALTER TABLE "batch_job_configs" ADD COLUMN "partition" varchar(255);

UPDATE "batch_job_configs" c SET "partition" = d."default_partition"
FROM "batch_application_deployments" d LEFT JOIN "batch_processes" p ON p."deployment_id" = d."deployment_id"
WHERE c."batch_job_config_id" IN (d."default_batch_job_config_id", p."batch_job_config_id");

ALTER TABLE "batch_application_deployments" DROP COLUMN "default_partition";

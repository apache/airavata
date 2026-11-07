ALTER TABLE "batch_processes" ADD COLUMN "token" text;

ALTER TABLE "batch_job_configs" ADD COLUMN "partition" varchar(255);

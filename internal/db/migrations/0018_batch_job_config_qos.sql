-- Let a batch job config carry a Slurm quality of service. The DDL is what AutoMigrate
-- emits for BatchJobConfig.QOS — see TestBaselineMigrationMatchesAutoMigrate.

ALTER TABLE "batch_job_configs" ADD COLUMN "qos" varchar(255);

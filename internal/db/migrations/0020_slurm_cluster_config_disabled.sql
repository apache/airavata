-- Let a cluster config be disabled, so no new process is created or launched under it.
-- Existing configs stay enabled.

ALTER TABLE "slurm_cluster_configs" ADD COLUMN "disabled" boolean NOT NULL DEFAULT false;

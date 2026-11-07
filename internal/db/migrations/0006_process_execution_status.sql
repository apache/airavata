-- Record where each process execution stands: RUNNING once its workflow is started,
-- then SUCCEEDED or FAILED. Rows recorded before this column existed are left NULL —
-- their outcome was never captured. The DDL is what AutoMigrate emits for
-- ProcessExecution.Status — see TestBaselineMigrationMatchesAutoMigrate.

ALTER TABLE "process_executions" ADD COLUMN "status" varchar(255);

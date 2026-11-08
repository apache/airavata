-- Record the batch script a job submission task built and submitted, so a run can be
-- inspected afterwards. Tasks that ran before this column existed are left NULL — their
-- script was never kept. The DDL is what AutoMigrate emits for
-- JobSubmissionTask.Script — see TestBaselineMigrationMatchesAutoMigrate.

ALTER TABLE "job_submission_tasks" ADD COLUMN "script" text;

-- Rename the process execution entity to process workflow.
--
-- What a row records is a workflow run of the process (or the monitoring task that
-- follows its job), and "execution" collided with the execution engine and the
-- execution context the workflows pass between tasks.
--
-- Everything here is a rename, so no data moves and no id changes: a client holding a
-- process workflow id keeps addressing the same row. The names are the ones GORM
-- derives from the renamed Go identifiers, which is what keeps this file and
-- AutoMigrate in agreement — see TestBaselineMigrationMatchesAutoMigrate.
--
-- This is a migration rather than an edit of 0005 and 0006 because those have already
-- run: a database that applied them records their versions and would skip a rewrite.

ALTER TABLE "process_executions" RENAME TO "process_workflows";

ALTER TABLE "process_workflows" RENAME COLUMN "process_execution_id" TO "process_workflow_id";

ALTER TABLE "process_workflows" RENAME COLUMN "execution_id" TO "workflow_id";

ALTER TABLE "process_workflows" RENAME COLUMN "execution_type" TO "workflow_type";

ALTER TABLE "process_workflows" RENAME CONSTRAINT "fk_process_executions_process" TO "fk_process_workflows_process";

ALTER INDEX "process_executions_pkey" RENAME TO "process_workflows_pkey";

ALTER INDEX "idx_process_executions_process_id" RENAME TO "idx_process_workflows_process_id";

ALTER TABLE "processes" RENAME COLUMN "last_execution_id" TO "last_workflow_id";

ALTER INDEX "idx_processes_last_execution_id" RENAME TO "idx_processes_last_workflow_id";

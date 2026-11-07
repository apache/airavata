-- Record the workflow executions started for a process.
--
-- Each row names one workflow instance (execution_id, the id the workflow engine knows
-- it by) and which of the process's workflows it ran. The process reference is
-- RESTRICT, like process_statuses: Process.BeforeDelete removes the executions first.
--
-- processes.last_execution_id caches the newest execution the way last_status_id caches
-- the newest status, and for the same reason carries no foreign key: the two tables
-- would otherwise reference each other. It is maintained by ProcessExecution.AfterCreate.
-- The DDL is what AutoMigrate emits — see TestBaselineMigrationMatchesAutoMigrate.

ALTER TABLE "processes" ADD COLUMN "last_execution_id" varchar(36);

CREATE INDEX IF NOT EXISTS "idx_processes_last_execution_id" ON "processes" ("last_execution_id");

CREATE TABLE "process_executions" ("process_execution_id" varchar(36),"process_id" varchar(36),"execution_id" varchar(36),"execution_type" varchar(255),"timestamp" bigint,PRIMARY KEY ("process_execution_id"),CONSTRAINT "fk_process_executions_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_process_executions_process_id" ON "process_executions" ("process_id");

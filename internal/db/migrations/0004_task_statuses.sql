-- Record a status history for process tasks.
--
-- A task id may name a row in any of the four task tables, so task_id carries no
-- foreign key and task_type says which table it points into. The process reference is
-- what removes the history with its run; a single task's history is removed by that
-- task's AfterDelete hook. The DDL is what AutoMigrate emits for TaskStatus — see
-- TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "task_statuses" ("task_status_id" varchar(36),"task_id" varchar(36) NOT NULL,"task_type" varchar(32) NOT NULL,"process_id" varchar(36),"status" varchar(255),"log" text,"timestamp" bigint,PRIMARY KEY ("task_status_id"),CONSTRAINT "fk_task_statuses_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_task_statuses_process_id" ON "task_statuses" ("process_id");

CREATE INDEX IF NOT EXISTS "idx_task_statuses_task_id" ON "task_statuses" ("task_id");

-- The template input and output mappings moved from the process to its batch section.
--
-- They name a declaration on the deployment's template, and the deployment is reached
-- through the batch process, so the process was never the right owner: a mapping on a
-- process with no batch section named a template that nothing connected it to. The
-- rows now hang off batch_processes, and Process.BeforeDelete still reaches them —
-- deleting a process deletes its batch process, which cascades to the mappings.
--
-- Each table is rewritten the same way: the old foreign key goes first, since it
-- constrains the column the backfill is about to rewrite; the column is renamed rather
-- than added and dropped, so it keeps its position and no row is copied; the backfill
-- rewrites each process id to the id of that process's batch process; and the new
-- constraint and index arrive afterwards, matching what AutoMigrate emits for the
-- entity model name for name.
--
-- The DELETE removes what cannot be rehomed: a mapping whose process carries no batch
-- process has nowhere to hang, and leaving it with a null owner would keep a row no
-- API path can reach or clean up. Only development databases can hold any — the API
-- now rejects mappings on a process with no batch section.

ALTER TABLE "process_template_input_mappings" DROP CONSTRAINT "fk_processes_input_mappings";

ALTER TABLE "process_template_input_mappings" RENAME COLUMN "process_id" TO "batch_process_id";

UPDATE "process_template_input_mappings" SET "batch_process_id" = (
	SELECT "batch_processes"."batch_process_id" FROM "batch_processes"
	WHERE "batch_processes"."parent_process_id" = "process_template_input_mappings"."batch_process_id");

DELETE FROM "process_template_input_mappings" WHERE "batch_process_id" IS NULL;

ALTER TABLE "process_template_input_mappings" ADD CONSTRAINT "fk_batch_processes_input_mappings" FOREIGN KEY ("batch_process_id") REFERENCES "batch_processes"("batch_process_id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER INDEX "idx_process_template_input_mappings_process_id" RENAME TO "idx_process_template_input_mappings_batch_process_id";

ALTER TABLE "process_template_output_mappings" DROP CONSTRAINT "fk_processes_output_mappings";

ALTER TABLE "process_template_output_mappings" RENAME COLUMN "process_id" TO "batch_process_id";

UPDATE "process_template_output_mappings" SET "batch_process_id" = (
	SELECT "batch_processes"."batch_process_id" FROM "batch_processes"
	WHERE "batch_processes"."parent_process_id" = "process_template_output_mappings"."batch_process_id");

DELETE FROM "process_template_output_mappings" WHERE "batch_process_id" IS NULL;

ALTER TABLE "process_template_output_mappings" ADD CONSTRAINT "fk_batch_processes_output_mappings" FOREIGN KEY ("batch_process_id") REFERENCES "batch_processes"("batch_process_id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER INDEX "idx_process_template_output_mappings_process_id" RENAME TO "idx_process_template_output_mappings_batch_process_id";

-- The workflow engine's execution id of a recorded workflow, which cancelling the
-- workflow needs alongside its instance id.

ALTER TABLE "process_workflows" ADD "execution_id" varchar(36);

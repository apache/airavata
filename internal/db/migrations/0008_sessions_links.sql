CREATE TABLE "sessions" ("session_id" varchar(36),"user_id" varchar(255) NOT NULL,"created_at" bigint NOT NULL,"current_process_id" varchar(36) NOT NULL,PRIMARY KEY ("session_id"));
CREATE INDEX IF NOT EXISTS "idx_sessions_owner_id" ON "sessions" ("user_id");
ALTER TABLE "processes" ADD COLUMN "session_id" varchar(36);
CREATE INDEX IF NOT EXISTS "idx_processes_session_id" ON "processes" ("session_id");
ALTER TABLE "batch_processes" ADD COLUMN "link_token" text;
ALTER TABLE "batch_processes" ALTER COLUMN "connection" TYPE text;

-- The secrets a process's job is handed so its Linkspan can link back to Airavata:
-- the digest of the link token, the Jupyter token the owner opens the job's Jupyter
-- Server with, and the loopback ports its servers listen on. One row per process,
-- replaced on each submission. The DDL is what AutoMigrate emits for ProcessLink —
-- see TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "process_links" ("process_id" varchar(36),"link_token_hash" varchar(64) NOT NULL,"jupyter_token" varchar(64) NOT NULL,"control_port" integer NOT NULL,"jupyter_port" integer NOT NULL,"issued_at" bigint NOT NULL,PRIMARY KEY ("process_id"),CONSTRAINT "fk_process_links_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

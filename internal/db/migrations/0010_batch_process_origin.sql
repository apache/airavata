-- Let a batch process record the client that last launched it. The DDL is what
-- AutoMigrate emits for BatchJobProcess.Origin — see TestBaselineMigrationMatchesAutoMigrate.

ALTER TABLE "batch_processes" ADD COLUMN "origin" varchar(255);

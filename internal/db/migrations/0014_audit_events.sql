-- The audit log: one row per change to a record, recording who made it, what was done
-- and to which record. Written by the audit plugin (internal/audit) in the same
-- transaction as the change. Append-only, and deliberately free of foreign keys so the
-- trail outlives the users and records it describes. The DDL is what AutoMigrate emits
-- for audit/model.Event — see TestBaselineMigrationMatchesAutoMigrate.

CREATE TABLE "audit_events" ("id" bigserial,"occurred_at" bigint NOT NULL,"actor" varchar(255) NOT NULL,"action" varchar(10) NOT NULL,"entity_type" varchar(64) NOT NULL,"entity_id" varchar(255) NOT NULL DEFAULT '',"changed_fields" varchar(1024) NOT NULL DEFAULT '',PRIMARY KEY ("id"));

CREATE INDEX IF NOT EXISTS "idx_audit_events_entity" ON "audit_events" ("entity_type","entity_id","occurred_at");

CREATE INDEX IF NOT EXISTS "idx_audit_events_actor" ON "audit_events" ("actor","occurred_at");

CREATE INDEX IF NOT EXISTS "idx_audit_events_occurred_at" ON "audit_events" ("occurred_at");

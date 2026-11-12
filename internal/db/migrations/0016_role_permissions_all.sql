-- Permission checks for the remaining administrative actions.
--
-- 0015 introduced role_permissions with audit:read. This grants ADMIN the permissions
-- that replace the rest of the ADMIN/SUPER_ADMIN role checks, so behaviour is unchanged:
-- ADMIN keeps everything it could do, and the three actions reserved to SUPER_ADMIN
-- (user:create, user:update_any, admin:manage) are granted to no role, since SUPER_ADMIN
-- holds every permission without a row.
--
-- The rows are the defaults in internal/permission.Defaults; TestDefaultsMatchMigrations
-- keeps the two in step.

INSERT INTO "role_permissions" ("role","permission") VALUES
  ('ADMIN','cluster:discover'),
  ('ADMIN','cluster:manage'),
  ('ADMIN','clusterconfig:list'),
  ('ADMIN','dataproduct:list'),
  ('ADMIN','deployment:manage'),
  ('ADMIN','group:list'),
  ('ADMIN','group:manage_any'),
  ('ADMIN','group:read_any'),
  ('ADMIN','process:delete'),
  ('ADMIN','process:list'),
  ('ADMIN','process:manage_any'),
  ('ADMIN','process:update'),
  ('ADMIN','resource:manage_any'),
  ('ADMIN','serviceaccount:manage'),
  ('ADMIN','serviceaccount:read'),
  ('ADMIN','storage:list'),
  ('ADMIN','template:manage'),
  ('ADMIN','user:list'),
  ('ADMIN','user:read_any'),
  ('ADMIN','user:set_status'),
  ('ADMIN','virtualdirectory:list');

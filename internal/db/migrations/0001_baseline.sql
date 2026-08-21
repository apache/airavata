-- Baseline schema for PostgreSQL, recorded from a real run of AutoMigrate against a
-- PostgreSQL 17 server (see Entities() in internal/db/migrate.go). It is the schema
-- every fresh production database should start from before any versioned migration
-- runs, and it is byte-for-byte what the entity model produces rather than a
-- translation of anything.
--
-- Migration history restarts here. The previous files (0001-0007) were MariaDB DDL —
-- backquoted identifiers, inline INDEX clauses, MODIFY COLUMN, DROP FOREIGN KEY — none
-- of which PostgreSQL will parse, and their intermediate states (a text is_file, a
-- cluster_credentials table) never existed in a PostgreSQL database. A MariaDB
-- deployment carrying data cannot replay anything to get here; it needs a data
-- migration, which no DDL file can stand in for.
--
-- Re-recorded when the process vertical was reorganised around a single Process entity:
-- batch_job_processes and batch_job_process_statuses became processes, batch_processes
-- and process_statuses, the tasks traded their process_type discriminator for a real
-- foreign key, and the template input and output mappings arrived. None of those tables
-- had run outside development, so the baseline was re-recorded rather than migrated
-- forward from a shape no deployment held.
--
-- Do not hand-edit this file once it has run anywhere outside development: a change to
-- a table's shape belongs in a new migration (0002_..., 0003_..., ...), the same way
-- ddl-auto never narrows a column and this framework never rewrites history.
--
-- Note the generated identifiers capped at 63 characters, PostgreSQL's limit. That cap
-- is set explicitly on the naming strategy in internal/db/open.go, so what GORM
-- generates and what the server stores stay identical rather than being silently
-- truncated on arrival.

CREATE TABLE "users" ("user_id" varchar(255),"auth_method" varchar(32),"email" varchar(255),"first_name" varchar(255),"last_name" varchar(255),"status" varchar(32),"created_at" bigint NOT NULL,PRIMARY KEY ("user_id"));

CREATE TABLE "ssh_keys" ("ssh_key_id" varchar(36),"ssh_key_name" varchar(255) NOT NULL,"public_key" text NOT NULL,"private_key" text NOT NULL,"passphrase" varchar(255),PRIMARY KEY ("ssh_key_id"));

CREATE TABLE "ssh_endpoints" ("ssh_endpoint_id" varchar(36),"name" varchar(255) NOT NULL,"host_name" varchar(255) NOT NULL,"port" bigint NOT NULL,PRIMARY KEY ("ssh_endpoint_id"));

CREATE TABLE "application_templates" ("template_id" varchar(36),"template_name" varchar(255),"template_description" varchar(2048),PRIMARY KEY ("template_id"));

CREATE TABLE "batch_job_configs" ("batch_job_config_id" varchar(36),"cpus" integer,"mem" varchar(64),"mem_per_cpu" varchar(64),"ntasks_per_node" integer,"cpus_per_task" integer,"nodes" integer,"ntasks" integer,"gres" varchar(255),"gpus" integer,"mem_per_gpu" varchar(64),"cpus_per_gpu" varchar(64),"gpus_per_node" integer,"wall_time_minutes" bigint NOT NULL,"constraints" varchar(255),"allocation" varchar(255) NOT NULL,PRIMARY KEY ("batch_job_config_id"));

CREATE TABLE "user_roles" ("user_id" varchar(255),"role" varchar(32),PRIMARY KEY ("user_id","role"),CONSTRAINT "fk_users_roles" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE "groups" ("group_id" varchar(36),"group_name" varchar(255),"user_id" varchar(255),"created_at" bigint NOT NULL,PRIMARY KEY ("group_id"),CONSTRAINT "fk_groups_owner" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_groups_owner_id" ON "groups" ("user_id");

CREATE TABLE "ssh_user_credentials" ("ssh_credential_id" varchar(36),"username" varchar(255) NOT NULL,"ssh_key_id" varchar(36),PRIMARY KEY ("ssh_credential_id"),CONSTRAINT "fk_ssh_user_credentials_ssh_key" FOREIGN KEY ("ssh_key_id") REFERENCES "ssh_keys"("ssh_key_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_ssh_user_credentials_ssh_key_id" ON "ssh_user_credentials" ("ssh_key_id");

CREATE TABLE "clusters" ("cluster_id" varchar(36),"cluster_name" varchar(255) NOT NULL,"cluster_description" varchar(1024),"slurm_home" varchar(1024) NOT NULL,"ssh_endpoint_id" varchar(36),PRIMARY KEY ("cluster_id"),CONSTRAINT "fk_clusters_ssh_endpoint" FOREIGN KEY ("ssh_endpoint_id") REFERENCES "ssh_endpoints"("ssh_endpoint_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_clusters_ssh_endpoint_id" ON "clusters" ("ssh_endpoint_id");

CREATE TABLE "scp_data_storages" ("data_id" varchar(36),"data_name" varchar(255),"ssh_endpoint_id" varchar(36),"user_id" varchar(255),PRIMARY KEY ("data_id"),CONSTRAINT "fk_scp_data_storages_ssh_endpoint" FOREIGN KEY ("ssh_endpoint_id") REFERENCES "ssh_endpoints"("ssh_endpoint_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_scp_data_storages_owner" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_scp_data_storages_owner_id" ON "scp_data_storages" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_scp_data_storages_ssh_endpoint_id" ON "scp_data_storages" ("ssh_endpoint_id");

CREATE TABLE "data_products" ("data_id" varchar(36),"data_name" varchar(255),"data_description" varchar(2048),"is_file" boolean NOT NULL,"path" varchar(2048),"provision_status" varchar(32),"user_id" varchar(255),"data_storage_id" varchar(36),"data_storage_type" varchar(32),"credential_id" varchar(36),"created_at" bigint NOT NULL,PRIMARY KEY ("data_id"),CONSTRAINT "fk_data_products_owner" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_data_products_credential_id" ON "data_products" ("credential_id");

CREATE INDEX IF NOT EXISTS "idx_data_products_data_storage_id" ON "data_products" ("data_storage_id");

CREATE INDEX IF NOT EXISTS "idx_data_products_owner_id" ON "data_products" ("user_id");

CREATE TABLE "application_template_inputs" ("input_id" varchar(36),"template_id" varchar(36),"input_name" varchar(255),"display_name" varchar(255),"input_description" varchar(2048),"input_type" varchar(32),"is_required" boolean NOT NULL,"default_value" text,PRIMARY KEY ("input_id"),CONSTRAINT "fk_application_templates_inputs" FOREIGN KEY ("template_id") REFERENCES "application_templates"("template_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE UNIQUE INDEX IF NOT EXISTS "uk_template_input_name" ON "application_template_inputs" ("template_id","input_name");

CREATE TABLE "application_template_outputs" ("output_id" varchar(36),"template_id" varchar(36),"output_type" varchar(32),"output_name" varchar(255),"display_name" varchar(255),"output_description" varchar(2048),PRIMARY KEY ("output_id"),CONSTRAINT "fk_application_templates_outputs" FOREIGN KEY ("template_id") REFERENCES "application_templates"("template_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_application_template_outputs_template_id" ON "application_template_outputs" ("template_id");

CREATE TABLE "group_members" ("group_id" varchar(36),"user_id" varchar(255),"group_role" varchar(32),"group_member_status" varchar(32),PRIMARY KEY ("group_id","user_id"),CONSTRAINT "fk_users_memberships" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT "fk_groups_members" FOREIGN KEY ("group_id") REFERENCES "groups"("group_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE "cluster_partitions" ("partition_id" varchar(36),"cluster_id" varchar(36),"name" varchar(255) NOT NULL,"description" varchar(1024),"max_run_time" integer,"max_nodes" integer,"max_processors" integer,"max_jobs_in_queue" integer,"max_memory" bigint,"cpu_per_node" integer,"default_node_count" integer,"default_cpu_count" integer,"default_walltime" bigint,"gres" varchar(1024),"nodes" varchar(4096),"is_default_queue" boolean,"is_checkpointable" boolean,PRIMARY KEY ("partition_id"),CONSTRAINT "fk_clusters_partitions" FOREIGN KEY ("cluster_id") REFERENCES "clusters"("cluster_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_cluster_partitions_cluster_id" ON "cluster_partitions" ("cluster_id");

CREATE TABLE "ssh_endpoint_credentials" ("ssh_endpoint_credential_id" varchar(36),"ssh_endpoint_id" varchar(36),"ssh_credential_id" varchar(36),"user_id" varchar(255),PRIMARY KEY ("ssh_endpoint_credential_id"),CONSTRAINT "fk_ssh_endpoint_credentials_ssh_endpoint" FOREIGN KEY ("ssh_endpoint_id") REFERENCES "ssh_endpoints"("ssh_endpoint_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_ssh_endpoint_credentials_ssh_credential" FOREIGN KEY ("ssh_credential_id") REFERENCES "ssh_user_credentials"("ssh_credential_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_ssh_endpoint_credentials_owner" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_ssh_endpoint_credentials_owner_id" ON "ssh_endpoint_credentials" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_ssh_endpoint_credentials_ssh_credential_id" ON "ssh_endpoint_credentials" ("ssh_credential_id");

CREATE INDEX IF NOT EXISTS "idx_ssh_endpoint_credentials_ssh_endpoint_id" ON "ssh_endpoint_credentials" ("ssh_endpoint_id");

CREATE TABLE "batch_application_deployments" ("deployment_id" varchar(36),"cluster_id" varchar(36),"template_id" varchar(36),"slurm_run_section" text NOT NULL,"batch_job_config_id" varchar(36) NOT NULL,"default_submission_credential_id" varchar(36) NOT NULL,"work_dir" varchar(1024),"partition" varchar(255),PRIMARY KEY ("deployment_id"),CONSTRAINT "fk_batch_application_deployments_template" FOREIGN KEY ("template_id") REFERENCES "application_templates"("template_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_batch_application_deployments_batch_job_config" FOREIGN KEY ("batch_job_config_id") REFERENCES "batch_job_configs"("batch_job_config_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_batch_application_deployments_default_submission_credential" FOREIGN KEY ("default_submission_credential_id") REFERENCES "ssh_endpoint_credentials"("ssh_endpoint_credential_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_batch_application_deployments_cluster" FOREIGN KEY ("cluster_id") REFERENCES "clusters"("cluster_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_batch_application_deployments_default_submission_cr850dbe93" ON "batch_application_deployments" ("default_submission_credential_id");

CREATE UNIQUE INDEX IF NOT EXISTS "idx_batch_application_deployments_batch_job_config_id" ON "batch_application_deployments" ("batch_job_config_id");

CREATE INDEX IF NOT EXISTS "idx_batch_application_deployments_template_id" ON "batch_application_deployments" ("template_id");

CREATE INDEX IF NOT EXISTS "idx_batch_application_deployments_cluster_id" ON "batch_application_deployments" ("cluster_id");

CREATE TABLE "processes" ("process_id" varchar(36),"user_id" varchar(255),"process_type" varchar(32),"last_status_id" varchar(36),PRIMARY KEY ("process_id"),CONSTRAINT "fk_processes_owner" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_processes_last_status_id" ON "processes" ("last_status_id");

CREATE INDEX IF NOT EXISTS "idx_processes_owner_id" ON "processes" ("user_id");

CREATE TABLE "ssh_endpoint_credential_group_sharings" ("ssh_endpoint_credential_group_sharing_id" varchar(36),"ssh_endpoint_credential_id" varchar(36),"group_id" varchar(36),"permission" varchar(32),PRIMARY KEY ("ssh_endpoint_credential_group_sharing_id"),CONSTRAINT "fk_ssh_endpoint_credential_group_sharings_ssh_endpoint_e0bc6690" FOREIGN KEY ("ssh_endpoint_credential_id") REFERENCES "ssh_endpoint_credentials"("ssh_endpoint_credential_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_ssh_endpoint_credential_group_sharings_group" FOREIGN KEY ("group_id") REFERENCES "groups"("group_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_ssh_endpoint_credential_group_sharings_group_id" ON "ssh_endpoint_credential_group_sharings" ("group_id");

CREATE INDEX IF NOT EXISTS "idx_ssh_endpoint_credential_group_sharings_ssh_endpoint620154af" ON "ssh_endpoint_credential_group_sharings" ("ssh_endpoint_credential_id");

CREATE TABLE "ssh_endpoint_credential_user_sharings" ("ssh_endpoint_credential_user_sharing_id" varchar(36),"ssh_endpoint_credential_id" varchar(36),"user_id" varchar(255),"permission" varchar(32),PRIMARY KEY ("ssh_endpoint_credential_user_sharing_id"),CONSTRAINT "fk_ssh_endpoint_credential_user_sharings_ssh_endpoint_c4cde1714" FOREIGN KEY ("ssh_endpoint_credential_id") REFERENCES "ssh_endpoint_credentials"("ssh_endpoint_credential_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_ssh_endpoint_credential_user_sharings_user" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_ssh_endpoint_credential_user_sharings_user_id" ON "ssh_endpoint_credential_user_sharings" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_ssh_endpoint_credential_user_sharings_ssh_endpoint_802d3210" ON "ssh_endpoint_credential_user_sharings" ("ssh_endpoint_credential_id");

CREATE TABLE "scp_data_storage_group_sharings" ("data_storage_group_sharing_id" varchar(36),"data_storage_id" varchar(36),"group_id" varchar(36),"permission" varchar(32),PRIMARY KEY ("data_storage_group_sharing_id"),CONSTRAINT "fk_scp_data_storage_group_sharings_data_storage" FOREIGN KEY ("data_storage_id") REFERENCES "scp_data_storages"("data_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_scp_data_storage_group_sharings_group" FOREIGN KEY ("group_id") REFERENCES "groups"("group_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_scp_data_storage_group_sharings_group_id" ON "scp_data_storage_group_sharings" ("group_id");

CREATE INDEX IF NOT EXISTS "idx_scp_data_storage_group_sharings_data_storage_id" ON "scp_data_storage_group_sharings" ("data_storage_id");

CREATE TABLE "scp_data_storage_user_sharings" ("data_storage_user_sharing_id" varchar(36),"data_storage_id" varchar(36),"user_id" varchar(255),"permission" varchar(32),PRIMARY KEY ("data_storage_user_sharing_id"),CONSTRAINT "fk_scp_data_storage_user_sharings_data_storage" FOREIGN KEY ("data_storage_id") REFERENCES "scp_data_storages"("data_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_scp_data_storage_user_sharings_user" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_scp_data_storage_user_sharings_user_id" ON "scp_data_storage_user_sharings" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_scp_data_storage_user_sharings_data_storage_id" ON "scp_data_storage_user_sharings" ("data_storage_id");

CREATE TABLE "data_product_group_sharings" ("data_product_group_sharing_id" varchar(36),"data_product_id" varchar(36),"group_id" varchar(36),"permission" varchar(32),PRIMARY KEY ("data_product_group_sharing_id"),CONSTRAINT "fk_data_product_group_sharings_data_product" FOREIGN KEY ("data_product_id") REFERENCES "data_products"("data_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_data_product_group_sharings_group" FOREIGN KEY ("group_id") REFERENCES "groups"("group_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_data_product_group_sharings_group_id" ON "data_product_group_sharings" ("group_id");

CREATE INDEX IF NOT EXISTS "idx_data_product_group_sharings_data_product_id" ON "data_product_group_sharings" ("data_product_id");

CREATE TABLE "data_product_user_sharings" ("data_product_user_sharing_id" varchar(36),"data_product_id" varchar(36),"user_id" varchar(255),"permission" varchar(32),PRIMARY KEY ("data_product_user_sharing_id"),CONSTRAINT "fk_data_product_user_sharings_data_product" FOREIGN KEY ("data_product_id") REFERENCES "data_products"("data_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_data_product_user_sharings_user" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_data_product_user_sharings_user_id" ON "data_product_user_sharings" ("user_id");

CREATE INDEX IF NOT EXISTS "idx_data_product_user_sharings_data_product_id" ON "data_product_user_sharings" ("data_product_id");

CREATE TABLE "batch_processes" ("batch_process_id" varchar(36),"parent_process_id" varchar(36),"deployment_id" varchar(36),"batch_job_config_id" varchar(36) NOT NULL,"job_id" varchar(255),"job_name" varchar(255),PRIMARY KEY ("batch_process_id"),CONSTRAINT "fk_batch_processes_deployment" FOREIGN KEY ("deployment_id") REFERENCES "batch_application_deployments"("deployment_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_batch_processes_batch_job_config" FOREIGN KEY ("batch_job_config_id") REFERENCES "batch_job_configs"("batch_job_config_id") ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT "fk_processes_batch_process" FOREIGN KEY ("parent_process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE UNIQUE INDEX IF NOT EXISTS "idx_batch_processes_batch_job_config_id" ON "batch_processes" ("batch_job_config_id");

CREATE INDEX IF NOT EXISTS "idx_batch_processes_deployment_id" ON "batch_processes" ("deployment_id");

CREATE UNIQUE INDEX IF NOT EXISTS "idx_batch_processes_process_id" ON "batch_processes" ("parent_process_id");

CREATE TABLE "process_statuses" ("process_status_id" varchar(36),"process_id" varchar(36),"status" varchar(255),"log" text,"timestamp" bigint,PRIMARY KEY ("process_status_id"),CONSTRAINT "fk_process_statuses_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_process_statuses_process_id" ON "process_statuses" ("process_id");

CREATE TABLE "process_template_input_mappings" ("template_input_mapping_id" varchar(36),"template_input_id" varchar(36),"process_id" varchar(36),"value" text,PRIMARY KEY ("template_input_mapping_id"),CONSTRAINT "fk_process_template_input_mappings_template_input" FOREIGN KEY ("template_input_id") REFERENCES "application_template_inputs"("input_id") ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT "fk_processes_input_mappings" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_process_template_input_mappings_process_id" ON "process_template_input_mappings" ("process_id");

CREATE INDEX IF NOT EXISTS "idx_process_template_input_mappings_template_input_id" ON "process_template_input_mappings" ("template_input_id");

CREATE TABLE "process_template_output_mappings" ("template_output_mapping_id" varchar(36),"template_output_id" varchar(36),"process_id" varchar(36),"value" text,PRIMARY KEY ("template_output_mapping_id"),CONSTRAINT "fk_process_template_output_mappings_template_output" FOREIGN KEY ("template_output_id") REFERENCES "application_template_outputs"("output_id") ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT "fk_processes_output_mappings" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_process_template_output_mappings_process_id" ON "process_template_output_mappings" ("process_id");

CREATE INDEX IF NOT EXISTS "idx_process_template_output_mappings_template_output_id" ON "process_template_output_mappings" ("template_output_id");

CREATE TABLE "data_staging_tasks" ("task_id" varchar(36),"process_id" varchar(36),"source_data_storage_id" varchar(36),"source_credential_id" varchar(36),"source_data_storage_type" varchar(32),"destination_data_storage_id" varchar(36),"destination_credential_id" varchar(36),"destination_data_storage_type" varchar(32),"source_path" text,"destination_path" text,"on_failure" varchar(32),"retry_count" bigint,"task_order" bigint,PRIMARY KEY ("task_id"),CONSTRAINT "fk_data_staging_tasks_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_data_staging_tasks_destination_credential_id" ON "data_staging_tasks" ("destination_credential_id");

CREATE INDEX IF NOT EXISTS "idx_data_staging_tasks_destination_data_storage_id" ON "data_staging_tasks" ("destination_data_storage_id");

CREATE INDEX IF NOT EXISTS "idx_data_staging_tasks_source_credential_id" ON "data_staging_tasks" ("source_credential_id");

CREATE INDEX IF NOT EXISTS "idx_data_staging_tasks_source_data_storage_id" ON "data_staging_tasks" ("source_data_storage_id");

CREATE INDEX IF NOT EXISTS "idx_data_staging_tasks_process_id" ON "data_staging_tasks" ("process_id");

CREATE TABLE "job_submission_tasks" ("task_id" varchar(36),"process_id" varchar(36),"job_id" varchar(255),"on_failure" varchar(32),"retry_count" bigint,"task_order" bigint,PRIMARY KEY ("task_id"),CONSTRAINT "fk_job_submission_tasks_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_job_submission_tasks_process_id" ON "job_submission_tasks" ("process_id");

CREATE TABLE "job_monitoring_tasks" ("task_id" varchar(36),"process_id" varchar(36),"job_id" varchar(255),"on_failure" varchar(32),"retry_count" bigint,"task_order" bigint,PRIMARY KEY ("task_id"),CONSTRAINT "fk_job_monitoring_tasks_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_job_monitoring_tasks_process_id" ON "job_monitoring_tasks" ("process_id");

CREATE TABLE "interactive_command_tasks" ("task_id" varchar(36),"process_id" varchar(36),"command" text,"output" text,"on_failure" varchar(32),"retry_count" bigint,"task_order" bigint,PRIMARY KEY ("task_id"),CONSTRAINT "fk_interactive_command_tasks_process" FOREIGN KEY ("process_id") REFERENCES "processes"("process_id") ON DELETE CASCADE ON UPDATE CASCADE);

CREATE INDEX IF NOT EXISTS "idx_interactive_command_tasks_process_id" ON "interactive_command_tasks" ("process_id");

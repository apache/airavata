-- Adds the four kinds of process task: data staging, job submission, job monitoring
-- and interactive command.
--
-- A task names its process by (process_id, process_type) rather than through a foreign
-- key. That is deliberate and matches the entity model: the pair is a discriminated
-- reference, so a task is not tied to batch job processes alone — today
-- `process_type` only ever holds 'BATCH_JOB'. The service resolves the process and
-- authorises against its owner, which is also what keeps a task from naming a run that
-- does not exist.
--
-- `task_order` is nullable: an unordered task runs after the ordered ones rather than
-- ahead of them, and tasks sharing an order run in parallel.
--
-- Names, column order and index names are the ones AutoMigrate derives from the entity
-- model (verified against the schema it emits).

CREATE TABLE `data_staging_tasks` (`task_id` varchar(36),`process_id` varchar(36),`process_type` varchar(32),`source_data_storage_id` varchar(36),`source_credential_id` varchar(36),`source_data_storage_type` varchar(32),`destination_data_storage_id` varchar(36),`destination_credential_id` varchar(36),`destination_data_storage_type` varchar(32),`source_path` text,`destination_path` text,`on_failure` varchar(32),`retry_count` int,`task_order` int,PRIMARY KEY (`task_id`),INDEX `idx_data_staging_tasks_process_id` (`process_id`),INDEX `idx_data_staging_tasks_source_data_storage_id` (`source_data_storage_id`),INDEX `idx_data_staging_tasks_source_credential_id` (`source_credential_id`),INDEX `idx_data_staging_tasks_destination_data_storage_id` (`destination_data_storage_id`),INDEX `idx_data_staging_tasks_destination_credential_id` (`destination_credential_id`));

CREATE TABLE `job_submission_tasks` (`task_id` varchar(36),`process_id` varchar(36),`process_type` varchar(32),`job_id` varchar(255),`on_failure` varchar(32),`retry_count` int,`task_order` int,PRIMARY KEY (`task_id`),INDEX `idx_job_submission_tasks_process_id` (`process_id`));

CREATE TABLE `job_monitoring_tasks` (`task_id` varchar(36),`process_id` varchar(36),`process_type` varchar(32),`job_id` varchar(255),`on_failure` varchar(32),`retry_count` int,`task_order` int,PRIMARY KEY (`task_id`),INDEX `idx_job_monitoring_tasks_process_id` (`process_id`));

CREATE TABLE `interactive_command_tasks` (`task_id` varchar(36),`process_id` varchar(36),`process_type` varchar(32),`command` text,`output` text,`on_failure` varchar(32),`retry_count` int,`task_order` int,PRIMARY KEY (`task_id`),INDEX `idx_interactive_command_tasks_process_id` (`process_id`));

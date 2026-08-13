-- Adds BatchJobProcessStatus and BatchJobProcess.LastStatusID.
--
-- The two tables reference each other — a status belongs to a process, and a process
-- points at its most recent status — so the new table is created first (its FK to the
-- already-existing batch_job_processes resolves immediately), then
-- batch_job_processes gets its new column and the FK back, exactly as AutoMigrate
-- sequenced it when captured against a copy of the pre-status schema.

CREATE TABLE `batch_job_process_statuses` (`process_status_id` varchar(36),`process_id` varchar(36),`status` varchar(255),`log` text,`timestamp` bigint,PRIMARY KEY (`process_status_id`),INDEX `idx_batch_job_process_statuses_process_id` (`process_id`),CONSTRAINT `fk_batch_job_process_statuses_process` FOREIGN KEY (`process_id`) REFERENCES `batch_job_processes`(`process_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

ALTER TABLE `batch_job_processes` ADD `last_status_id` varchar(36);

ALTER TABLE `batch_job_processes` ADD CONSTRAINT `fk_batch_job_processes_last_status` FOREIGN KEY (`last_status_id`) REFERENCES `batch_job_process_statuses`(`process_status_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX `idx_batch_job_processes_last_status_id` ON `batch_job_processes`(`last_status_id`);

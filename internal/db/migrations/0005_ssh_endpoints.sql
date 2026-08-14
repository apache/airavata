-- Splits the SSH host out of Cluster and rebases credentials on it.
--
-- Before: a cluster carried a bare `host_name`, and a credential binding named a
-- cluster. After: an SSH endpoint is an entity of its own, a cluster points at one,
-- and a binding is held against the endpoint rather than against one cluster's view of
-- it. Two sharing tables are added alongside, so a binding can be granted to a group
-- or to a named user with READ or WRITE.
--
-- Existing rows are carried across rather than dropped. Every cluster gets an endpoint
-- minted from its old host name, reusing the cluster id as the endpoint id so the
-- backfill needs no join table and stays deterministic on re-runs of the same data.
-- Port 22 is assumed, which is what the old schema implied by having no port at all.
--
-- Names, column order and constraint names are the ones AutoMigrate derives from the
-- entity model (verified against the schema it emits), including the hashed suffixes
-- MySQL's 64-character identifier limit forces on the sharing tables.

CREATE TABLE `ssh_endpoints` (`ssh_endpoint_id` varchar(36),`name` varchar(255) NOT NULL,`host_name` varchar(255) NOT NULL,`port` bigint NOT NULL,PRIMARY KEY (`ssh_endpoint_id`));

INSERT INTO `ssh_endpoints` (`ssh_endpoint_id`, `name`, `host_name`, `port`)
  SELECT `cluster_id`, `cluster_name`, `host_name`, 22 FROM `clusters`;

ALTER TABLE `clusters` ADD `ssh_endpoint_id` varchar(36);

UPDATE `clusters` SET `ssh_endpoint_id` = `cluster_id`;

ALTER TABLE `clusters` ADD CONSTRAINT `fk_clusters_ssh_endpoint` FOREIGN KEY (`ssh_endpoint_id`) REFERENCES `ssh_endpoints`(`ssh_endpoint_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX `idx_clusters_ssh_endpoint_id` ON `clusters`(`ssh_endpoint_id`);

ALTER TABLE `clusters` DROP COLUMN `host_name`;

CREATE TABLE `ssh_endpoint_credentials` (`ssh_endpoint_credential_id` varchar(36),`ssh_endpoint_id` varchar(36),`ssh_credential_id` varchar(36),`user_id` varchar(255),PRIMARY KEY (`ssh_endpoint_credential_id`),INDEX `idx_ssh_endpoint_credentials_ssh_endpoint_id` (`ssh_endpoint_id`),INDEX `idx_ssh_endpoint_credentials_ssh_credential_id` (`ssh_credential_id`),INDEX `idx_ssh_endpoint_credentials_owner_id` (`user_id`),CONSTRAINT `fk_ssh_endpoint_credentials_ssh_endpoint` FOREIGN KEY (`ssh_endpoint_id`) REFERENCES `ssh_endpoints`(`ssh_endpoint_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_ssh_endpoint_credentials_ssh_credential` FOREIGN KEY (`ssh_credential_id`) REFERENCES `ssh_user_credentials`(`ssh_credential_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_ssh_endpoint_credentials_owner` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

-- The binding id is preserved, so everything already pointing at a binding — every
-- deployment's default_submission_credential_id — keeps resolving. cluster_id maps to
-- the endpoint of the same id, minted above.
INSERT INTO `ssh_endpoint_credentials` (`ssh_endpoint_credential_id`, `ssh_endpoint_id`, `ssh_credential_id`, `user_id`)
  SELECT `cluster_credential_id`, `cluster_id`, `ssh_credential_id`, `user_id` FROM `cluster_credentials`;

CREATE TABLE `ssh_endpoint_credential_group_sharings` (`ssh_endpoint_credential_group_sharing_id` varchar(36),`ssh_endpoint_credential_id` varchar(36),`group_id` varchar(36),`permission` varchar(32),PRIMARY KEY (`ssh_endpoint_credential_group_sharing_id`),INDEX `idx_ssh_endpoint_credential_group_sharings_ssh_endpoint_620154af` (`ssh_endpoint_credential_id`),INDEX `idx_ssh_endpoint_credential_group_sharings_group_id` (`group_id`),CONSTRAINT `fk_ssh_endpoint_credential_group_sharings_ssh_endpoint_ce0bc6690` FOREIGN KEY (`ssh_endpoint_credential_id`) REFERENCES `ssh_endpoint_credentials`(`ssh_endpoint_credential_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_ssh_endpoint_credential_group_sharings_group` FOREIGN KEY (`group_id`) REFERENCES `groups`(`group_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE TABLE `ssh_endpoint_credential_user_sharings` (`ssh_endpoint_credential_user_sharing_id` varchar(36),`ssh_endpoint_credential_id` varchar(36),`user_id` varchar(255),`permission` varchar(32),PRIMARY KEY (`ssh_endpoint_credential_user_sharing_id`),INDEX `idx_ssh_endpoint_credential_user_sharings_ssh_endpoint_c802d3210` (`ssh_endpoint_credential_id`),INDEX `idx_ssh_endpoint_credential_user_sharings_user_id` (`user_id`),CONSTRAINT `fk_ssh_endpoint_credential_user_sharings_ssh_endpoint_credential` FOREIGN KEY (`ssh_endpoint_credential_id`) REFERENCES `ssh_endpoint_credentials`(`ssh_endpoint_credential_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_ssh_endpoint_credential_user_sharings_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

-- Repoint the deployment's submission credential at the new table. The ids are the
-- same rows under a new name, so no deployment changes value here.
ALTER TABLE `batch_application_deployments` DROP FOREIGN KEY `fk_batch_application_deployments_default_submission_credential`;

ALTER TABLE `batch_application_deployments` ADD CONSTRAINT `fk_batch_application_deployments_default_submission_credential` FOREIGN KEY (`default_submission_credential_id`) REFERENCES `ssh_endpoint_credentials`(`ssh_endpoint_credential_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- scp_data predates the current data model and is no longer mapped by any entity. Its
-- foreign key is dropped so the old table can go, but the column itself is left in
-- place: the ids it holds are still meaningful, and dropping it would discard them.
ALTER TABLE `scp_data` DROP FOREIGN KEY `fk_scp_data_cluster_credential`;

DROP TABLE `cluster_credentials`;

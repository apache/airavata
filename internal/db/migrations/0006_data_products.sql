-- Adds the data vertical: SCP data storages, registered data products, and the four
-- sharing tables that decide who may reach either.
--
-- Both a storage and a product belong to whoever registered them, and are reachable by
-- that owner plus whoever a sharing row names.
--
-- Every sharing foreign key is RESTRICT: a share must be revoked (or removed with the
-- record it opens up) rather than being cascaded away behind the owner's back.
--
-- `data_products.data_storage_id` deliberately carries no foreign key. It is qualified
-- by `data_storage_type`, so the row it names lives in whichever storage table that
-- type selects; today that is only `scp_data_storages`. The service checks the
-- reference instead, which is also what lets it report a storage still in use as a
-- conflict rather than orphaning products. `credential_id` is unconstrained for the
-- same reason it is checked the same way: the service resolves it against the SSH
-- endpoint credentials and refuses one that is not for the storage's own host.
--
-- Names, column order, index and constraint names are the ones AutoMigrate derives
-- from the entity model (verified against the schema it emits).

CREATE TABLE `scp_data_storages` (`data_id` varchar(36),`data_name` varchar(255),`ssh_endpoint_id` varchar(36),`user_id` varchar(255),PRIMARY KEY (`data_id`),INDEX `idx_scp_data_storages_ssh_endpoint_id` (`ssh_endpoint_id`),INDEX `idx_scp_data_storages_owner_id` (`user_id`),CONSTRAINT `fk_scp_data_storages_ssh_endpoint` FOREIGN KEY (`ssh_endpoint_id`) REFERENCES `ssh_endpoints`(`ssh_endpoint_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_scp_data_storages_owner` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE TABLE `data_products` (`data_id` varchar(36),`data_name` varchar(255),`data_description` varchar(2048),`is_file` boolean NOT NULL,`path` varchar(2048),`provision_status` varchar(32),`user_id` varchar(255),`data_storage_id` varchar(36),`data_storage_type` varchar(32),`credential_id` varchar(36),`created_at` bigint NOT NULL,PRIMARY KEY (`data_id`),INDEX `idx_data_products_owner_id` (`user_id`),INDEX `idx_data_products_data_storage_id` (`data_storage_id`),INDEX `idx_data_products_credential_id` (`credential_id`),CONSTRAINT `fk_data_products_owner` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE TABLE `scp_data_storage_group_sharings` (`data_storage_group_sharing_id` varchar(36),`data_storage_id` varchar(36),`group_id` varchar(36),`permission` varchar(32),PRIMARY KEY (`data_storage_group_sharing_id`),INDEX `idx_scp_data_storage_group_sharings_data_storage_id` (`data_storage_id`),INDEX `idx_scp_data_storage_group_sharings_group_id` (`group_id`),CONSTRAINT `fk_scp_data_storage_group_sharings_data_storage` FOREIGN KEY (`data_storage_id`) REFERENCES `scp_data_storages`(`data_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_scp_data_storage_group_sharings_group` FOREIGN KEY (`group_id`) REFERENCES `groups`(`group_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE TABLE `scp_data_storage_user_sharings` (`data_storage_user_sharing_id` varchar(36),`data_storage_id` varchar(36),`user_id` varchar(255),`permission` varchar(32),PRIMARY KEY (`data_storage_user_sharing_id`),INDEX `idx_scp_data_storage_user_sharings_data_storage_id` (`data_storage_id`),INDEX `idx_scp_data_storage_user_sharings_user_id` (`user_id`),CONSTRAINT `fk_scp_data_storage_user_sharings_data_storage` FOREIGN KEY (`data_storage_id`) REFERENCES `scp_data_storages`(`data_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_scp_data_storage_user_sharings_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE TABLE `data_product_group_sharings` (`data_product_group_sharing_id` varchar(36),`data_product_id` varchar(36),`group_id` varchar(36),`permission` varchar(32),PRIMARY KEY (`data_product_group_sharing_id`),INDEX `idx_data_product_group_sharings_data_product_id` (`data_product_id`),INDEX `idx_data_product_group_sharings_group_id` (`group_id`),CONSTRAINT `fk_data_product_group_sharings_data_product` FOREIGN KEY (`data_product_id`) REFERENCES `data_products`(`data_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_data_product_group_sharings_group` FOREIGN KEY (`group_id`) REFERENCES `groups`(`group_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE TABLE `data_product_user_sharings` (`data_product_user_sharing_id` varchar(36),`data_product_id` varchar(36),`user_id` varchar(255),`permission` varchar(32),PRIMARY KEY (`data_product_user_sharing_id`),INDEX `idx_data_product_user_sharings_data_product_id` (`data_product_id`),INDEX `idx_data_product_user_sharings_user_id` (`user_id`),CONSTRAINT `fk_data_product_user_sharings_data_product` FOREIGN KEY (`data_product_id`) REFERENCES `data_products`(`data_id`) ON DELETE RESTRICT ON UPDATE CASCADE,CONSTRAINT `fk_data_product_user_sharings_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

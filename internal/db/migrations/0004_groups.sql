-- Adds Group and GroupMember, the entities behind /api/v1/groups.
--
-- The membership table is created second because its foreign keys point at both
-- `groups` and the already-existing `users`. Table, column, index and constraint names
-- are the ones AutoMigrate derives from the entity model (verified against the schema
-- it emits), so a database migrated with this file matches one created by AutoMigrate.
--
-- `groups` is a reserved word in some MySQL versions and is quoted everywhere for that
-- reason.
--
-- Ownership is RESTRICT: a user who still owns groups cannot be deleted out from under
-- them. Membership is CASCADE from both sides — a membership is meaningless once
-- either the group or the user is gone.

CREATE TABLE `groups` (`group_id` varchar(36),`group_name` varchar(255),`user_id` varchar(255),`created_at` bigint NOT NULL,PRIMARY KEY (`group_id`),INDEX `idx_groups_owner_id` (`user_id`),CONSTRAINT `fk_groups_owner` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE);

CREATE TABLE `group_members` (`group_id` varchar(36),`user_id` varchar(255),`group_role` varchar(32),`group_member_status` varchar(32),PRIMARY KEY (`group_id`,`user_id`),CONSTRAINT `fk_groups_members` FOREIGN KEY (`group_id`) REFERENCES `groups`(`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,CONSTRAINT `fk_users_memberships` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE);

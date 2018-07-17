--
--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

-- NOTE: the following is only needed if you previously installed Airavata from
-- the master branch and have a sharing catalog database
ALTER TABLE `ENTITY` CHANGE `SHARED_COUNT` `SHARED_COUNT` BIGINT DEFAULT 0;
ALTER TABLE `SHARING` DROP FOREIGN KEY IF EXISTS `SHARING_ibfk_1`;
ALTER TABLE `SHARING` DROP FOREIGN KEY IF EXISTS `SHARING_ibfk_2`;
ALTER TABLE `SHARING` DROP FOREIGN KEY IF EXISTS `SHARING_ibfk_3`;
ALTER TABLE `SHARING` DROP FOREIGN KEY IF EXISTS `SHARING_ibfk_4`;
ALTER TABLE `SHARING` ADD CONSTRAINT `SHARING_PERMISSION_TYPE_ID_DOMAIN_ID_FK` FOREIGN KEY (PERMISSION_TYPE_ID, DOMAIN_ID) REFERENCES PERMISSION_TYPE(PERMISSION_TYPE_ID, DOMAIN_ID) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `SHARING` ADD CONSTRAINT `SHARING_ENTITY_ID_DOMAIN_ID_FK` FOREIGN KEY (ENTITY_ID, DOMAIN_ID) REFERENCES ENTITY(ENTITY_ID, DOMAIN_ID) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `SHARING` ADD CONSTRAINT `SHARING_INHERITED_PARENT_ID_DOMAIN_ID_FK` FOREIGN KEY (INHERITED_PARENT_ID, DOMAIN_ID) REFERENCES ENTITY(ENTITY_ID, DOMAIN_ID) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `SHARING` ADD CONSTRAINT `SHARING_GROUP_ID_DOMAIN_ID_FK` FOREIGN KEY (GROUP_ID, DOMAIN_ID) REFERENCES USER_GROUP(GROUP_ID, DOMAIN_ID) ON DELETE CASCADE ON UPDATE NO ACTION;

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

use sharing_catalog;

-- AIRAVATA-2938: bring database schema into sync with registry-refactoring DB init scripts
CREATE TABLE IF NOT EXISTS GROUP_ADMIN (
  ADMIN_ID VARCHAR(255) NOT NULL,
  GROUP_ID VARCHAR(255) NOT NULL,
  DOMAIN_ID VARCHAR(255) NOT NULL,
  PRIMARY KEY (ADMIN_ID, GROUP_ID, DOMAIN_ID),
  FOREIGN KEY (ADMIN_ID, DOMAIN_ID) REFERENCES SHARING_USER(USER_ID, DOMAIN_ID) ON DELETE CASCADE ON UPDATE NO ACTION
)ENGINE=InnoDB DEFAULT CHARACTER SET=latin1;

ALTER TABLE `GROUP_ADMIN` ADD CONSTRAINT `GROUP_ADMIN_ibfk_1` FOREIGN KEY IF NOT EXISTS (`ADMIN_ID`, `DOMAIN_ID`) REFERENCES `SHARING_USER` (`USER_ID`, `DOMAIN_ID`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `GROUP_ADMIN` ADD KEY IF NOT EXISTS `ADMIN_ID` (`ADMIN_ID`,`DOMAIN_ID`);

-- Some SINGLE_USER groups were incorrectly created as MULTI_USER
update USER_GROUP set GROUP_CARDINALITY = 'SINGLE_USER' where GROUP_CARDINALITY = 'MULTI_USER' and OWNER_ID = GROUP_ID;

-- AIRAVATA-3238: add INITIAL_USER_GROUP_ID to DOMAIN
ALTER TABLE DOMAIN ADD COLUMN IF NOT EXISTS INITIAL_USER_GROUP_ID varchar(255);
ALTER TABLE DOMAIN ADD CONSTRAINT `DOMAIN_INITIAL_USER_GROUP_ID_FK` FOREIGN KEY IF NOT EXISTS (INITIAL_USER_GROUP_ID, DOMAIN_ID) REFERENCES USER_GROUP(GROUP_ID, DOMAIN_ID) ON DELETE CASCADE ON UPDATE NO ACTION;

-- AIRAVATA-3303 Slashes in experiment id
set FOREIGN_KEY_CHECKS=0;

update SHARING set ENTITY_ID = REPLACE(ENTITY_ID, "/", "_") where ENTITY_ID in (select ENTITY_ID from ENTITY where ENTITY_ID like '%/%' and ENTITY_TYPE_ID like '%:EXPERIMENT');
update ENTITY set ENTITY_ID = REPLACE(ENTITY_ID, "/", "_") where ENTITY_ID like '%/%' and ENTITY_TYPE_ID like '%:EXPERIMENT';

set FOREIGN_KEY_CHECKS=1;

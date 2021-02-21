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

use replica_catalog;

-- AIRAVATA-2872: NOT NULL foreign key columsn
set FOREIGN_KEY_CHECKS=0;
alter table DATA_PRODUCT_METADATA modify column PRODUCT_URI VARCHAR(255) NOT NULL;
alter table DATA_REPLICA_METADATA modify column REPLICA_ID VARCHAR(255) NOT NULL;
set FOREIGN_KEY_CHECKS=1;

-- AIRAVATA-2938: bring database schema into sync with registry-refactoring DB init scripts
-- somehow these workflow tables got created in the replica_catalog schema
DROP TABLE IF EXISTS `PORT`;
DROP TABLE IF EXISTS `WORKFLOW_OUTPUT`;
DROP TABLE IF EXISTS `WORKFLOW_STATUS`;
DROP TABLE IF EXISTS `WORKFLOW_INPUT`;
DROP TABLE IF EXISTS `NODE`;
DROP TABLE IF EXISTS `COMPONENT_STATUS`;
DROP TABLE IF EXISTS `EDGE`;
DROP TABLE IF EXISTS `WORKFLOW`;
set FOREIGN_KEY_CHECKS=0;
ALTER TABLE `DATA_PRODUCT` CHANGE `PRODUCT_TYPE` `PRODUCT_TYPE` varchar(10) DEFAULT NULL;
ALTER TABLE `DATA_REPLICA_LOCATION` CHANGE `REPLICA_LOCATION_CATEGORY` `REPLICA_LOCATION_CATEGORY` varchar(26) DEFAULT NULL;
ALTER TABLE `DATA_REPLICA_LOCATION` CHANGE `REPLICA_PERSISTENT_TYPE` `REPLICA_PERSISTENT_TYPE` varchar(10) DEFAULT NULL;
set FOREIGN_KEY_CHECKS=1;

-- AIRAVATA-3280: Widen FILE_PATH to accommodate longer file paths
ALTER TABLE `DATA_REPLICA_LOCATION` MODIFY COLUMN `FILE_PATH` VARCHAR(1024);

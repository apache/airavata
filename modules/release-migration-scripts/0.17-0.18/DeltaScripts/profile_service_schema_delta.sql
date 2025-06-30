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

use profile_service;

CREATE TABLE IF NOT EXISTS `CUSTOMIZED_DASHBOARD` (
 `AIRAVATA_INTERNAL_USER_ID` varchar(255) NOT NULL,
 `ENABLED_APPLICATION` varchar(255) DEFAULT NULL,
 `ENABLED_COMPUTE_RESOURCE` varchar(255) DEFAULT NULL,
 `ENABLED_CPU_COUNT` varchar(255) DEFAULT NULL,
 `ENABLED_CREATION_TIME` varchar(255) DEFAULT NULL,
 `ENABLED_DESCRIPTION` varchar(255) DEFAULT NULL,
 `ENABLED_ERRORS` varchar(255) DEFAULT NULL,
 `ENABLED_EXPERIMENT_ID` varchar(255) DEFAULT NULL,
 `ENABLED_INPUTS` varchar(255) DEFAULT NULL,
 `ENABLED_JOB_CREATION_TIME` varchar(255) DEFAULT NULL,
 `ENABLED_JOB_DESCRIPTION` varchar(255) DEFAULT NULL,
 `ENABLED_JOB_ID` varchar(255) DEFAULT NULL,
 `ENABLED_JOB_NAME` varchar(255) DEFAULT NULL,
 `ENABLED_JOB_STATUS` varchar(255) DEFAULT NULL,
 `ENABLED_LAST_MODIFIED_TIME` varchar(255) DEFAULT NULL,
 `ENABLED_NAME` varchar(255) DEFAULT NULL,
 `ENABLED_NODE_COUNT` varchar(255) DEFAULT NULL,
 `ENABLED_NOTIFICATIONS_TO` varchar(255) DEFAULT NULL,
 `ENABLED_OUTPUTS` varchar(255) DEFAULT NULL,
 `ENABLED_OWNER` varchar(255) DEFAULT NULL,
 `ENABLED_PROJECT` varchar(255) DEFAULT NULL,
 `ENABLED_QUEUE` varchar(255) DEFAULT NULL,
 `ENABLED_STORAGE_DIR` varchar(255) DEFAULT NULL,
 `ENABLED_WALL_TIME` varchar(255) DEFAULT NULL,
 `ENABLED_WORKING_DIR` varchar(255) DEFAULT NULL,
 PRIMARY KEY (`AIRAVATA_INTERNAL_USER_ID`),
 CONSTRAINT `CUSTOMIZED_DASHBOARD_ibfk_1` FOREIGN KEY (`AIRAVATA_INTERNAL_USER_ID`) REFERENCES `USER_PROFILE` (`AIRAVATA_INTERNAL_USER_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

alter database profile_service character set = 'latin1';

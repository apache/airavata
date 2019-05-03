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

# Will migrate 0.16 DataBase schema to 0.17 Database Schema

CREATE TABLE `QUEUE_STATUS` (
  `HOST_NAME` varchar(255) NOT NULL,
  `QUEUE_NAME` varchar(255) NOT NULL,
  `CREATED_TIME` bigint(20) NOT NULL,
  `QUEUE_UP` bit(1) DEFAULT NULL,
  `QUEUED_JOBS` int(11) DEFAULT NULL,
  `RUNNING_JOBS` int(11) DEFAULT NULL,
  PRIMARY KEY (`HOST_NAME`,`QUEUE_NAME`,`CREATED_TIME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
ALTER TABLE `GATEWAY` ADD `REQUESTER_USERNAME` varchar(255) DEFAULT NULL;
ALTER TABLE `GATEWAY` ADD `GATEWAY_DOMAIN` varchar(255) DEFAULT NULL;
ALTER TABLE `USER_CONFIGURATION_DATA` ADD `IS_USE_USER_CR_PREF` bit(1) DEFAULT NULL;
ALTER TABLE `PROCESS_INPUT` ADD `IS_READ_ONLY` smallint(1) NOT NULL DEFAULT '0';
ALTER TABLE `PROCESS` ADD `USE_USER_CR_PREF` bit(1) DEFAULT NULL;
ALTER TABLE `GATEWAY` ADD `REQUEST_CREATION_TIME` datetime DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE `EXPERIMENT_INPUT` ADD `IS_READ_ONLY` smallint(1) NOT NULL DEFAULT '0';
ALTER TABLE `GATEWAY` ADD `OAUTH_CLIENT_ID` varchar(255) DEFAULT NULL;
ALTER TABLE `GATEWAY` ADD `DECLINED_REASON` varchar(255) DEFAULT NULL;
ALTER TABLE `GATEWAY` ADD `OAUTH_CLIENT_SECRET` varchar(255) DEFAULT NULL;
ALTER TABLE `USERS` CHANGE `AIRAVATA_INTERNAL_USER_ID` `AIRAVATA_INTERNAL_USER_ID` varchar(255) NOT NULL;
ALTER TABLE `EXPERIMENT_STATUS` CHANGE `TIME_OF_STATE_CHANGE` `TIME_OF_STATE_CHANGE` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);
ALTER TABLE `NOTIFICATION` CHANGE `NOTIFICATION_MESSAGE` `NOTIFICATION_MESSAGE` varchar(4096) NOT NULL;
ALTER TABLE `JOB_STATUS` CHANGE `TIME_OF_STATE_CHANGE` `TIME_OF_STATE_CHANGE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE `USERS` ADD UNIQUE KEY `AIRAVATA_INTERNAL_USER_ID` (`AIRAVATA_INTERNAL_USER_ID`);

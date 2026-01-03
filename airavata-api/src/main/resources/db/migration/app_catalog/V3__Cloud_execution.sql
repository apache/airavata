/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

-- Add missing tables to match ddl.sql structure

CREATE TABLE IF NOT EXISTS `agent_deployment_info` (
  `agent_application_id` varchar(255) DEFAULT NULL,
  `agent_deployment_info_id` varchar(255) DEFAULT NULL,
  `compute_resource_id` varchar(255) DEFAULT NULL,
  `user_friendly_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `agent_execution` (
  `agent_execution_id` varchar(255) DEFAULT NULL,
  `agent_id` varchar(255) DEFAULT NULL,
  `airavata_experiment_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `agent_execution_status` (
  `status` tinyint(4) DEFAULT NULL,
  `updated_time` bigint(20) DEFAULT NULL,
  `additional_info` varchar(2000) DEFAULT NULL,
  `agent_execution_agent_execution_id` varchar(255) DEFAULT NULL,
  `agent_execution_status_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `AWS_GROUP_COMPUTE_RESOURCE_PREFERENCE` (
  `RESOURCE_ID` varchar(255) NOT NULL,
  `GROUP_RESOURCE_PROFILE_ID` varchar(255) NOT NULL,
  `AWS_REGION` varchar(255) NOT NULL,
  `PREFERRED_AMI_ID` varchar(255) NOT NULL,
  `PREFERRED_INSTANCE_TYPE` varchar(255) NOT NULL,
  PRIMARY KEY (`RESOURCE_ID`,`GROUP_RESOURCE_PROFILE_ID`),
  CONSTRAINT `FK_AWS_PREF_TO_BASE` FOREIGN KEY (`RESOURCE_ID`, `GROUP_RESOURCE_PROFILE_ID`) REFERENCES `GROUP_COMPUTE_RESOURCE_PREFERENCE` (`RESOURCE_ID`, `GROUP_RESOURCE_PROFILE_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `plan` (
  `data` text DEFAULT NULL,
  `gateway_id` varchar(255) NOT NULL,
  `plan_id` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `SLURM_GROUP_COMPUTE_RESOURCE_PREFERENCE` (
  `RESOURCE_ID` varchar(255) NOT NULL,
  `GROUP_RESOURCE_PROFILE_ID` varchar(255) NOT NULL,
  `PREFERED_BATCH_QUEUE` varchar(255) DEFAULT NULL,
  `ALLOCATION_PROJECT_NUMBER` varchar(255) DEFAULT NULL,
  `USAGE_REPORTING_GATEWAY_ID` varchar(255) DEFAULT NULL,
  `QUALITY_OF_SERVICE` varchar(255) DEFAULT NULL,
  `RESERVATION` varchar(255) DEFAULT NULL,
  `RESERVATION_START_TIME` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `RESERVATION_END_TIME` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `SSH_ACCOUNT_PROVISIONER` varchar(255) DEFAULT NULL,
  `SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO` text DEFAULT NULL,
  PRIMARY KEY (`RESOURCE_ID`,`GROUP_RESOURCE_PROFILE_ID`),
  CONSTRAINT `FK_SLURM_PREF_TO_BASE` FOREIGN KEY (`RESOURCE_ID`, `GROUP_RESOURCE_PROFILE_ID`) REFERENCES `GROUP_COMPUTE_RESOURCE_PREFERENCE` (`RESOURCE_ID`, `GROUP_RESOURCE_PROFILE_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


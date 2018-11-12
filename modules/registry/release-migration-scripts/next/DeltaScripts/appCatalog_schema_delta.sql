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

-- AIRAVATA-2768
alter table APPLICATION_INPUT modify METADATA VARCHAR(4096);

-- AIRAVATA-2758
-- Note: this doesn't really represent a schema change, rather some legacy database instances improperly had created these columns as NOT NULL
alter table COMPUTE_RESOURCE modify column CREATION_TIME TIMESTAMP DEFAULT NOW() null;
alter table COMPUTE_RESOURCE modify column CPUS_PER_NODE int default 0 null;
alter table COMPUTE_RESOURCE modify column DEFAULT_NODE_COUNT int default 0 null;
alter table COMPUTE_RESOURCE modify column DEFAULT_CPU_COUNT int default 0 null;
alter table COMPUTE_RESOURCE modify column DEFAULT_WALLTIME int default 0 null;
alter table COMPUTE_RESOURCE modify column UPDATE_TIME timestamp default '0000-00-00 00:00:00' null;

-- AIRAVATA-2827: OpenJPA 2.4.3 upgrade, convert BIT -> TINYINT(1)
alter table APPLICATION_OUTPUT modify column OUTPUT_STREAMING tinyint(1);
alter table APPLICATION_INTERFACE modify column ARCHIVE_WORKING_DIRECTORY tinyint(1);
alter table APPLICATION_INTERFACE modify column HAS_OPTIONAL_FILE_INPUTS tinyint(1);
alter table APPLICATION_DEPLOYMENT modify column EDITABLE_BY_USER tinyint(1);
alter table BATCH_QUEUE modify column IS_DEFAULT_QUEUE tinyint(1);
alter table COMPUTE_RESOURCE modify column GATEWAY_USAGE_REPORTING tinyint(1);
alter table USER_COMPUTE_RESOURCE_PREFERENCE modify column VALIDATED tinyint(1) default 0;

-- AIRAVATA-2872: NOT NULL foreign key columsn
set FOREIGN_KEY_CHECKS=0;
alter table HOST_ALIAS modify column RESOURCE_ID VARCHAR(255) NOT NULL;
alter table HOST_IPADDRESS modify column RESOURCE_ID VARCHAR(255) NOT NULL;
alter table GSISSH_EXPORT modify column SUBMISSION_ID VARCHAR(255) NOT NULL;
alter table GSISSH_PREJOBCOMMAND modify column SUBMISSION_ID VARCHAR(255) NOT NULL;
alter table GSISSH_POSTJOBCOMMAND modify column SUBMISSION_ID VARCHAR(255) NOT NULL;
alter table GLOBUS_GK_ENDPOINT modify column SUBMISSION_ID VARCHAR(255) NOT NULL;
alter table APPLICATION_DEPLOYMENT modify column APP_MODULE_ID VARCHAR(255) NOT NULL;
alter table APPLICATION_DEPLOYMENT modify column COMPUTE_HOSTID VARCHAR(255) NOT NULL;
alter table PREJOB_COMMAND modify column APPDEPLOYMENT_ID VARCHAR(255) NOT NULL;
alter table POSTJOB_COMMAND modify column APPDEPLOYMENT_ID VARCHAR(255) NOT NULL;
alter table LIBRARY_PREPAND_PATH modify column DEPLOYMENT_ID VARCHAR(255) NOT NULL;
alter table LIBRARY_APEND_PATH modify column DEPLOYMENT_ID VARCHAR(255) NOT NULL;
alter table APP_ENVIRONMENT modify column DEPLOYMENT_ID VARCHAR(255) NOT NULL;
alter table APP_MODULE_MAPPING modify column INTERFACE_ID VARCHAR(255) NOT NULL;
alter table APP_MODULE_MAPPING modify column MODULE_ID VARCHAR(255) NOT NULL;
alter table APPLICATION_INPUT modify column INTERFACE_ID VARCHAR(255) NOT NULL;
alter table APPLICATION_OUTPUT modify column INTERFACE_ID VARCHAR(255) NOT NULL;
alter table COMPUTE_RESOURCE_PREFERENCE modify column GATEWAY_ID VARCHAR(255) NOT NULL;
alter table COMPUTE_RESOURCE_PREFERENCE modify column RESOURCE_ID VARCHAR(255) NOT NULL;
alter table SSH_ACCOUNT_PROVISIONER_CONFIG modify column GATEWAY_ID VARCHAR(255) NOT NULL;
alter table SSH_ACCOUNT_PROVISIONER_CONFIG modify column RESOURCE_ID VARCHAR(255) NOT NULL;
alter table DATA_STORAGE_PREFERENCE modify column GATEWAY_ID VARCHAR(255) NOT NULL;
alter table WORKFLOW_INPUT modify column WF_TEMPLATE_ID VARCHAR(255) NOT NULL;
alter table WORKFLOW_OUTPUT modify column WF_TEMPLATE_ID VARCHAR(255) NOT NULL;
set FOREIGN_KEY_CHECKS=1;

-- AIRAVATA-2872: add missing column
alter table APP_ENVIRONMENT add column ENV_ORDER INTEGER;

-- AIRAVATA-2939: add NOT NULL constraints to DB fields that aren't in data model
alter table APPLICATION_MODULE modify column GATEWAY_ID VARCHAR(255) NOT NULL;
alter table APPLICATION_MODULE modify column CREATION_TIME TIMESTAMP DEFAULT NOW() NOT NULL;
alter table APPLICATION_MODULE modify column UPDATE_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL;
alter table APPLICATION_DEPLOYMENT modify column CREATION_TIME TIMESTAMP DEFAULT NOW() NOT NULL;
alter table APPLICATION_DEPLOYMENT modify column UPDATE_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL;
alter table APPLICATION_DEPLOYMENT modify column GATEWAY_ID VARCHAR(255) NOT NULL;
alter table APPLICATION_INTERFACE modify column GATEWAY_ID VARCHAR(255) NOT NULL;
alter table APPLICATION_INTERFACE modify column CREATION_TIME TIMESTAMP DEFAULT NOW() NOT NULL;
alter table APPLICATION_INTERFACE modify column UPDATE_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL;

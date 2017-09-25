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

alter table COMPUTE_RESOURCE_PREFERENCE add SSH_ACCOUNT_PROVISIONER VARCHAR(255);
alter table COMPUTE_RESOURCE_PREFERENCE add SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO VARCHAR(1000);

CREATE TABLE SSH_ACCOUNT_PROVISIONER_CONFIG
(
        GATEWAY_ID VARCHAR(255),
        RESOURCE_ID VARCHAR(255),
        CONFIG_NAME VARCHAR(255),
        CONFIG_VALUE VARCHAR(255),
        PRIMARY KEY (GATEWAY_ID, RESOURCE_ID, CONFIG_NAME),
        FOREIGN KEY (GATEWAY_ID, RESOURCE_ID) REFERENCES COMPUTE_RESOURCE_PREFERENCE (GATEWAY_ID, RESOURCE_ID) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

alter table USER_COMPUTE_RESOURCE_PREFERENCE add VALIDATED tinyint(1) NOT NULL DEFAULT 0;
-- VALIDATED defaults to false (0) but set all existing ones to be true (1)
update USER_COMPUTE_RESOURCE_PREFERENCE set VALIDATED = 1;
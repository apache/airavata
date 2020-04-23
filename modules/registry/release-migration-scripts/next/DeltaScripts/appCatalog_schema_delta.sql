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

use app_catalog;

-- AIRAVATA-3276 Replace JSON configuration: "toggle": ["isRequired"] ->
-- "isRequired": true. Toggling requiredToAddedToCommandLine no longer needed.

-- replace toggle with is isRequired
update APPLICATION_INPUT
set METADATA = REGEXP_REPLACE(METADATA, '"toggle": \\[[^}]+\\]', CONCAT('"isRequired": ', IF(IS_REQUIRED=1, 'true', 'false')))
-- showOptions has "toggle" but not "isRequired"
where METADATA rlike '"showOptions": {"toggle": \\[[^}]+\\]'
  and NOT METADATA rlike '"showOptions": {.*"isRequired": (true|false)'
;

-- remove toggle since isRequired is already there
update APPLICATION_INPUT
set METADATA = REGEXP_REPLACE(METADATA, '(, )?"toggle": \\[[^}]+\\](, )?', '')
-- showOptions has BOTH "toggle" and "isRequired"
where METADATA rlike '"showOptions": {"toggle": \\[[^}]+\\]'
  and METADATA rlike '"showOptions": {.*"isRequired": (true|false)'
;
-- AIRAVATA-3126
CREATE TABLE IF NOT EXISTS COMPUTE_RESOURCE_RESERVATION -- ComputeResourceReservationEntity
    (RESERVATION_ID VARCHAR(255) NOT NULL, END_TIME TIMESTAMP NOT NULL DEFAULT 0, RESERVATION_NAME VARCHAR(255) NOT NULL, START_TIME TIMESTAMP NOT NULL DEFAULT 0, RESOURCE_ID VARCHAR(255) NOT NULL, GROUP_RESOURCE_PROFILE_ID VARCHAR(255) NOT NULL, PRIMARY KEY (RESERVATION_ID)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE IF NOT EXISTS COMPUTE_RESOURCE_RESERVATION_QUEUE (RESERVATION_ID VARCHAR(255), QUEUE_NAME VARCHAR(255) NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE INDEX IF NOT EXISTS I_CMPTN_Q_RESERVATION_ID ON COMPUTE_RESOURCE_RESERVATION_QUEUE (RESERVATION_ID);
ALTER TABLE COMPUTE_RESOURCE_RESERVATION ADD CONSTRAINT FK_COMPUTE_RESOURCE_RESERVATION FOREIGN KEY IF NOT EXISTS (RESOURCE_ID, GROUP_RESOURCE_PROFILE_ID) REFERENCES GROUP_COMPUTE_RESOURCE_PREFERENCE (RESOURCE_ID, GROUP_RESOURCE_PROFILE_ID) ON DELETE CASCADE;

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

use experiment_catalog;

CREATE TABLE IF NOT exists `GATEWAY_USAGE_REPORTING_COMMAND` (
  `GATEWAY_ID` varchar(255) NOT NULL,
  `COMPUTE_RESOURCE_ID` varchar(255) NOT NULL,
  `COMMAND` longtext NOT NULL,
  PRIMARY KEY (`GATEWAY_ID`, `COMPUTE_RESOURCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- AIRAVATA-3369: Convert USER_FRIENDLY_DESCRIPTION from VARCHAR to TEXT (CLOB)
alter table EXPERIMENT_INPUT modify column USER_FRIENDLY_DESCRIPTION TEXT;
alter table PROCESS_INPUT modify column USER_FRIENDLY_DESCRIPTION TEXT;

-- AIRAVATA-3322: Index on experiment_status to help statistics queries
CREATE INDEX IF NOT EXISTS experiment_status_experiment_id_time_of_state_change_state ON EXPERIMENT_STATUS (EXPERIMENT_ID, TIME_OF_STATE_CHANGE, STATE);

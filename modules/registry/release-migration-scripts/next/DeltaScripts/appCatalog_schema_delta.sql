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

-- AIRAVATA-3126
CREATE TABLE IF NOT EXISTS COMPUTE_RESOURCE_RESERVATION -- ComputeResourceReservationEntity
    (RESERVATION_ID VARCHAR(255) NOT NULL, END_TIME TIMESTAMP NOT NULL, RESERVATION_NAME VARCHAR(255) NOT NULL, START_TIME TIMESTAMP NOT NULL, RESOURCE_ID VARCHAR(255) NOT NULL, GROUP_RESOURCE_PROFILE_ID VARCHAR(255) NOT NULL, PRIMARY KEY (RESERVATION_ID)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE IF NOT EXISTS COMPUTE_RESOURCE_RESERVATION_QUEUE (RESERVATION_ID VARCHAR(255), QUEUE_NAME VARCHAR(255) NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE INDEX IF NOT EXISTS I_CMPTN_Q_RESERVATION_ID ON COMPUTE_RESOURCE_RESERVATION_QUEUE (RESERVATION_ID);
ALTER TABLE COMPUTE_RESOURCE_RESERVATION ADD CONSTRAINT FK_COMPUTE_RESOURCE_RESERVATION FOREIGN KEY IF NOT EXISTS (RESOURCE_ID, GROUP_RESOURCE_PROFILE_ID) REFERENCES GROUP_COMPUTE_RESOURCE_PREFERENCE (RESOURCE_ID, GROUP_RESOURCE_PROFILE_ID) ON DELETE CASCADE;

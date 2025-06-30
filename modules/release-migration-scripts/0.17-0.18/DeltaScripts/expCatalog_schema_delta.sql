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

CREATE TABLE PROCESS_WORKFLOW
(
  PROCESS_ID varchar(255) NOT NULL,
  WORKFLOW_ID varchar(255) NOT NULL,
  TYPE varchar(255) DEFAULT NULL,
  CREATION_TIME timestamp DEFAULT NOW(),
  PRIMARY KEY (PROCESS_ID, WORKFLOW_ID),
  FOREIGN KEY (PROCESS_ID) REFERENCES PROCESS(PROCESS_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `TASK` ADD `MAX_RETRY` int(11) NOT NULL DEFAULT '3';
ALTER TABLE `TASK` ADD `CURRENT_RETRY` int(11) NOT NULL DEFAULT '0';

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
USE experiment_catalog;

CREATE TABLE IF NOT EXISTS COMPUTE_RESOURCE_SCHEDULING (
    EXPERIMENT_ID varchar(255) NOT NULL,
    RESOURCE_HOST_ID varchar(255) NOT NULL,
    TOTAL_CPU_COUNT INT,
    NODE_COUNT INT,
    NUMBER_OF_THREADS INT,
    QUEUE_NAME varchar(255) NOT NULL,
    WALL_TIME_LIMIT INT,
    TOTAL_PHYSICAL_MEMORY INT,
    STATIC_WORKING_DIR varchar(255),
    OVERRIDE_LOGIN_USER_NAME varchar(255),
    OVERRIDE_SCRATCH_LOCATION varchar(255),
    OVERRIDE_ALLOCATION_PROJECT_NUMBER varchar(255),
    PARALLEL_GROUP_COUNT INT,
    PRIMARY KEY (EXPERIMENT_ID, RESOURCE_HOST_ID, QUEUE_NAME),
    FOREIGN KEY (EXPERIMENT_ID) REFERENCES EXPERIMENT(EXPERIMENT_ID) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = latin1;

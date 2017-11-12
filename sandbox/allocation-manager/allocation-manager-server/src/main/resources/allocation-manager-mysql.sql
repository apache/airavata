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

CREATE TABLE IF NOT EXISTS USER_ALLOCATION_DETAILS (
    REQUESTED_DATE BIGINT NOT NULL,
    TITLE TEXT NOT NULL,
    PROJECT_DESCRIPTION TEXT NOT NULL,
    KEYWORDS TEXT,
    FIELD_OF_SCIENCE TEXT,
    DOCUMENTS BLOB,
    TYPE_OF_ALLOCATION VARCHAR (255) NOT NULL,
    USERNAME VARCHAR (255) NOT NULL,
    PROJECT_ID VARCHAR (255) ,
    APPLICATIONS_TO_BE_USED TEXT,
    SPECIFIC_RESOURCE_SELECTION TEXT,
    SERVICE_UNITS BIGINT default 1,
    TYPICAL_SU_PER_JOB BIGINT,
    MAX_MEMORY_PER_CPU BIGINT,
    DISK_USAGE_RANGE_PER_JOB BIGINT,
    NUMBER_OF_CPU_PER_JOB BIGINT,
    PROJECT_REVIEWED_AND_FUNDED_BY TEXT,
    PRIMARY KEY (PROJECT_ID, USERNAME)
);

CREATE TABLE IF NOT EXISTS USER_DETAILS (
    USERNAME VARCHAR (255) PRIMARY KEY,
    PASSWORD VARCHAR (255) NOT NULL,
    FULL_NAME TEXT,
    EMAIL TEXT NOT NULL,
    USER_TYPE VARCHAR (255) NOT NULL
);

CREATE TABLE IF NOT EXISTS REQUEST_STATUS (
    PROJECT_ID VARCHAR (255) PRIMARY KEY,
    STATUS VARCHAR (255) NOT NULL,
    REVIEWERS TEXT,
    START_DATE BIGINT,
    END_DATE BIGINT,
    AWARD_ALLOCATION BIGINT
);

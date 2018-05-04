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
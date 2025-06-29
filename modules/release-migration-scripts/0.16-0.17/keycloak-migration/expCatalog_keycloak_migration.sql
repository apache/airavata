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

-- experiment catalog
--- disable foreign key checks
SET FOREIGN_KEY_CHECKS=0;
update GATEWAY set REQUESTER_USERNAME = lower(REQUESTER_USERNAME);
update USERS set AIRAVATA_INTERNAL_USER_ID = lower(AIRAVATA_INTERNAL_USER_ID), USER_NAME = lower(USER_NAME);
update GATEWAY_WORKER set USER_NAME = lower(USER_NAME);
update PROJECT set USER_NAME = lower(USER_NAME);
update PROJECT_USER set USER_NAME = lower(USER_NAME);
update EXPERIMENT set USER_NAME = lower(USER_NAME);
update PROCESS set USERNAME = lower(USERNAME);
SET FOREIGN_KEY_CHECKS=1;
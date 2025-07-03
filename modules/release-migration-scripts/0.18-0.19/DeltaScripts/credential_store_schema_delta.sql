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

-- AIRAVATA-2938: bring database schema into sync with registry-refactoring DB init scripts
use credential_store;

set FOREIGN_KEY_CHECKS=0;
ALTER TABLE `CREDENTIALS` CHANGE `TIME_PERSISTED` `TIME_PERSISTED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE `CREDENTIALS` CHANGE `DESCRIPTION` `DESCRIPTION` varchar(500) DEFAULT NULL;
ALTER TABLE `CREDENTIALS` CHANGE `TOKEN_ID` `TOKEN_ID` varchar(100) NOT NULL;
ALTER TABLE `COMMUNITY_USER` CHANGE `TOKEN_ID` `TOKEN_ID` varchar(100) NOT NULL;
ALTER TABLE `COMMUNITY_USER` CHANGE `GATEWAY_ID` `GATEWAY_ID` varchar(100) NOT NULL;
ALTER TABLE `COMMUNITY_USER` CHANGE `COMMUNITY_USER_NAME` `COMMUNITY_USER_NAME` varchar(100) NOT NULL;
ALTER TABLE `CREDENTIALS` CHANGE `GATEWAY_ID` `GATEWAY_ID` varchar(100) NOT NULL;
set FOREIGN_KEY_CHECKS=1;

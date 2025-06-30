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

use sharing_catalog;

CREATE TABLE IF NOT EXISTS `GROUP_ADMIN` (
 `ADMIN_ID` varchar(255) NOT NULL,
 `DOMAIN_ID` varchar(255) NOT NULL,
 `GROUP_ID` varchar(255) NOT NULL,
 PRIMARY KEY (`ADMIN_ID`,`DOMAIN_ID`,`GROUP_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

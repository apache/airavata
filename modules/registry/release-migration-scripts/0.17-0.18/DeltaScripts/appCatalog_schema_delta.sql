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

ALTER TABLE `COMPUTE_RESOURCE` ADD `DEFAULT_NODE_COUNT` int(11) NOT NULL DEFAULT '0';
ALTER TABLE `COMPUTE_RESOURCE` ADD `DEFAULT_CPU_COUNT` int(11) NOT NULL DEFAULT '0';
ALTER TABLE `COMPUTE_RESOURCE` ADD `DEFAULT_WALLTIME` int(11) NOT NULL DEFAULT '0';
ALTER TABLE `COMPUTE_RESOURCE` ADD `CPUS_PER_NODE` int(11) NOT NULL DEFAULT '0';
ALTER TABLE `BATCH_QUEUE` ADD `QUEUE_SPECIFIC_MACROS` varchar(255) NOT NULL;
ALTER TABLE `APPLICATION_DEPLOYMENT` ADD `DEFAULT_WALLTIME` int(11) NOT NULL DEFAULT '0';
ALTER TABLE `BATCH_QUEUE` ADD `DEFAULT_WALLTIME` int(11) NOT NULL DEFAULT '0';
ALTER TABLE `APPLICATION_DEPLOYMENT` CHANGE `DEFAULT_NODE_COUNT` `DEFAULT_NODE_COUNT` int(11) DEFAULT '0';
ALTER TABLE `APPLICATION_DEPLOYMENT` CHANGE `DEFAULT_CPU_COUNT` `DEFAULT_CPU_COUNT` int(11) NOT NULL DEFAULT '0';


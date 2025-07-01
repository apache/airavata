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

ALTER TABLE Configuration ADD category_id varchar(255) NOT NULL DEFAULT 'SYSTEM';

ALTER TABLE Configuration DROP PRIMARY KEY;

ALTER TABLE Configuration ADD PRIMARY KEY(config_key, config_val, category_id);

ALTER TABLE Node_Data
ADD execution_index int NOT NULL DEFAULT 0;

ALTER TABLE Node_Data DROP PRIMARY KEY;

ALTER TABLE Node_Data ADD PRIMARY KEY(workflow_instanceID, node_id, execution_index);



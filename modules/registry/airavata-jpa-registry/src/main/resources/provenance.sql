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
 
create table Experiment_Data
(
	experiment_ID varchar(255),
	name varchar(255),
	username varchar(255),
	PRIMARY KEY (experiment_ID)
);

create table Experiment_Metadata
(
	experiment_ID varchar(255),
	metadata BLOB,
	PRIMARY KEY (experiment_ID)
);


create table Workflow_Data
(
       experiment_ID varchar(255),
       workflow_instanceID varchar(255),
       template_name varchar(255),
       status varchar(100),
       start_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       last_update_time TIMESTAMP DEFAULT now() on update now(),
       PRIMARY KEY(workflow_instanceID),
       FOREIGN KEY (experiment_ID) REFERENCES Experiment_Data(experiment_ID) ON DELETE CASCADE
);

create table Node_Data
(
       workflow_instanceID varchar(255),
       node_id varchar(255),
       node_type varchar(255),
       inputs BLOB,
       outputs BLOB,
       status varchar(100),
       start_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       last_update_time TIMESTAMP DEFAULT now() on update now(),
       PRIMARY KEY(workflow_instanceID, node_id),
       FOREIGN KEY (workflow_instanceID) REFERENCES Workflow_Data(workflow_instanceID) ON DELETE CASCADE
);

create table Gram_Data
(
       workflow_instanceID varchar(255),
       node_id varchar(255),
       rsl BLOB,
       invoked_host varchar(255),
       local_Job_ID varchar(255),
       PRIMARY KEY(workflow_instanceID, node_id),
       FOREIGN KEY (workflow_instanceID) REFERENCES Workflow_Data(workflow_instanceID) ON DELETE CASCADE
);

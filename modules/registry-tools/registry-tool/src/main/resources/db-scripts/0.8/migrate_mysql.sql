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

CREATE TABLE Execution_Error
(
       error_id INT NOT NULL AUTO_INCREMENT,
       experiment_ID varchar(255),
       workflow_instanceID varchar(255),
       node_id varchar(255),
       gfacJobID varchar(255),
       source_type varchar(255),
       error_date TIMESTAMP DEFAULT now() on update now(),
       error_msg LONGTEXT,
       error_des LONGTEXT,
       error_code varchar(255),
       error_reporter varchar(255),
       error_location varchar(255),
       action_taken varchar(255),
       error_reference INTEGER,
       PRIMARY KEY(error_id),
       FOREIGN KEY (workflow_instanceID) REFERENCES Workflow_Data(workflow_instanceID) ON DELETE CASCADE,
       FOREIGN KEY (experiment_ID) REFERENCES Experiment_Data(experiment_ID) ON DELETE CASCADE
);

create table GFac_Job_Data
(
       experiment_ID varchar(255),
       workflow_instanceID varchar(255),
       node_id varchar(255),
       application_descriptor_ID varchar(255),
       host_descriptor_ID varchar(255),
       service_descriptor_ID varchar(255),
       job_data LONGTEXT,
       local_Job_ID varchar(255),
       submitted_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       status_update_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       status varchar(255),
       metadata LONGTEXT,
       PRIMARY KEY(local_Job_ID)
       FOREIGN KEY (experiment_ID) REFERENCES Experiment_Data(experiment_ID),
       FOREIGN KEY (workflow_instanceID) REFERENCES Workflow_Data(workflow_instanceID)
);

create table GFac_Job_Status
(
       local_Job_ID varchar(255),
       status_update_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       status varchar(255),
       FOREIGN KEY (local_Job_ID) REFERENCES GFac_Job_Data(local_Job_ID)
);

INSERT INTO GFac_Job_Data(experiment_ID, workflow_instanceID, node_id, application_descriptor_ID, host_descriptor_ID, service_descriptor_ID,
job_data, local_Job_ID, submitted_time, status_update_time, status, metadata) SELECT null, workflow_instanceID, node_id, null, invoked_host,
null, null, local_Job_ID, null, null, 'UNKNOWN', null FROM Gram_Data;
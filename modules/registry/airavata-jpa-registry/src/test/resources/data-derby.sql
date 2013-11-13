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
create table Gateway
(
        gateway_name varchar(255),
	      owner varchar(255),
        PRIMARY KEY (gateway_name)
);


create table Configuration
(
        config_key varchar(255),
        config_val varchar(255),
        expire_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
        category_id varchar (255),
        PRIMARY KEY(config_key, config_val, category_id)
);

INSERT INTO CONFIGURATION (config_key, config_val, expire_date, category_id) VALUES('registry.version', '0.11', CURRENT_TIMESTAMP ,'SYSTEM');

create table Users
(
        user_name varchar(255),
        password varchar(255),
        PRIMARY KEY(user_name)
);

create table Gateway_Worker
(
      gateway_name varchar(255),
      user_name varchar(255),
      PRIMARY KEY (gateway_name, user_name),
      FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
      FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE

);

create table Project
(
       gateway_name varchar(255),
       user_name varchar(255),
       project_name varchar(255),
       PRIMARY KEY (project_name),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Published_Workflow
(
       gateway_name varchar(255),
       created_user varchar(255),
       publish_workflow_name varchar(255),
       version varchar(255),
       published_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       path varchar (255),
       workflow_content BLOB,
       PRIMARY KEY(gateway_name, publish_workflow_name),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (created_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table User_Workflow

(
       gateway_name varchar(255),
       owner varchar(255),
       template_name varchar(255),
       last_updated_date TIMESTAMP DEFAULT CURRENT TIMESTAMP,
       path varchar (255),
       workflow_graph BLOB,
       PRIMARY KEY(gateway_name, owner, template_name),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (owner) REFERENCES Users(user_name) ON DELETE CASCADE
);


create table Host_Descriptor
(
       gateway_name varchar(255),
       updated_user varchar(255),
       host_descriptor_ID varchar(255),
       host_descriptor_xml BLOB,
       PRIMARY KEY(gateway_name, host_descriptor_ID),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Service_Descriptor
(
         gateway_name varchar(255),
         updated_user varchar(255),
         service_descriptor_ID varchar(255),
         service_descriptor_xml BLOB,
         PRIMARY KEY(gateway_name,service_descriptor_ID),
         FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
         FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Application_Descriptor
(
         gateway_name varchar(255),
         updated_user varchar(255),
         application_descriptor_ID varchar(255),
         host_descriptor_ID varchar(255),
         service_descriptor_ID varchar(255),
         application_descriptor_xml BLOB,
         PRIMARY KEY(gateway_name,application_descriptor_ID),
         FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
         FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Experiment
(
          project_name varchar(255),
	        gateway_name varchar(255),
          user_name varchar(255),
          experiment_ID varchar(255),
          submitted_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
          PRIMARY KEY(experiment_ID),
          FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
          FOREIGN KEY (project_name) REFERENCES Project(project_name) ON DELETE CASCADE,
          FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);

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
       status varchar(255),
       start_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
       status varchar(255),
       start_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       execution_index int NOT NULL,
       PRIMARY KEY(workflow_instanceID, node_id, execution_index),
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

create table GFac_Job_Data
(
       experiment_ID varchar(255),
       workflow_instanceID varchar(255),
       node_id varchar(255),
       application_descriptor_ID varchar(255),
       host_descriptor_ID varchar(255),
       service_descriptor_ID varchar(255),
       job_data CLOB,
       local_Job_ID varchar(255),
       submitted_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       status_update_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       status varchar(255),
       metadata CLOB,
       PRIMARY KEY(local_Job_ID),
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

CREATE TABLE COMMUNITY_USER
(
        GATEWAY_NAME VARCHAR(256) NOT NULL,
        COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,
        COMMUNITY_USER_EMAIL VARCHAR(256) NOT NULL,
        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)
);


CREATE TABLE CREDENTIALS
(
        GATEWAY_NAME VARCHAR(256) NOT NULL,
        COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,
        CREDENTIAL BLOB NOT NULL,
        PRIVATE_KEY BLOB NOT NULL,
        NOT_BEFORE VARCHAR(256) NOT NULL,
        NOT_AFTER VARCHAR(256) NOT NULL,
        LIFETIME INTEGER NOT NULL,
        REQUESTING_PORTAL_USER_NAME VARCHAR(256) NOT NULL,
        REQUESTED_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',
        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)
);

CREATE TABLE Execution_Error
(
       error_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY,
       experiment_ID varchar(255),
       workflow_instanceID varchar(255),
       node_id varchar(255),
       gfacJobID varchar(255),
       source_type varchar(255),
       error_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       error_msg CLOB,
       error_des CLOB,
       error_code varchar(255),
       error_reporter varchar(255),
       error_location varchar(255),
       action_taken varchar(255),
       error_reference INTEGER,
       PRIMARY KEY(error_id),
       FOREIGN KEY (workflow_instanceID) REFERENCES Workflow_Data(workflow_instanceID) ON DELETE CASCADE,
       FOREIGN KEY (experiment_ID) REFERENCES Experiment_Data(experiment_ID) ON DELETE CASCADE
);

-- CREATE TABLE openjpa_sequence_table
-- (
--   id SMALLINT NOT NULL,
--   sequence_value BIGINT,
--   PRIMARY KEY  (id)
-- );






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
CREATE TABLE Gateway
(
        gateway_name VARCHAR(255),
	    owner VARCHAR(255),
        PRIMARY KEY (gateway_name)
);

CREATE TABLE Configuration
(
        config_ID INT(11) NOT NULL AUTO_INCREMENT,
        config_key VARCHAR(255),
        config_val VARCHAR(255),
        expire_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
        PRIMARY KEY(config_ID)
);

CREATE TABLE Users
(
        user_name VARCHAR(255),
        password VARCHAR(255),
        PRIMARY KEY(user_name)
);

CREATE TABLE Gateway_Worker
(
      gateway_name VARCHAR(255),
      user_name VARCHAR(255),
      PRIMARY KEY (gateway_name, user_name),
      FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
      FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE

);

CREATE TABLE Project
(
       project_ID INT(11) NOT NULL AUTO_INCREMENT,
       gateway_name VARCHAR(255),
       user_name VARCHAR(255),
       project_name VARCHAR(255),
       PRIMARY KEY(project_ID),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);

CREATE TABLE Published_Workflow
(
       gateway_name VARCHAR(255),
       created_user VARCHAR(255),
       publish_workflow_name VARCHAR(255),
       version VARCHAR(255),
       published_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       path VARCHAR (255),
       workflow_content VARCHAR(2000),
       PRIMARY KEY(gateway_name, publish_workflow_name),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (created_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

CREATE TABLE User_Workflow

(
       gateway_name VARCHAR(255),
       owner VARCHAR(255),
       template_name VARCHAR(255),
       last_updated_date TIMESTAMP DEFAULT now() ON UPDATE now(),
       path VARCHAR (255),
       workflow_graph VARCHAR(2000),
       PRIMARY KEY(gateway_name, owner, template_name),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (owner) REFERENCES Users(user_name) ON DELETE CASCADE
);


CREATE TABLE Host_Descriptor
(
       gateway_name VARCHAR(255),
       updated_user VARCHAR(255),
       host_descriptor_ID VARCHAR(255),
       host_descriptor_xml VARCHAR(2000),
       PRIMARY KEY(gateway_name, host_descriptor_ID),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

CREATE TABLE Service_Descriptor
(
         gateway_name VARCHAR(255),
         updated_user VARCHAR(255),
         service_descriptor_ID VARCHAR(255),
         service_descriptor_xml VARCHAR(2000),
         PRIMARY KEY(gateway_name,service_descriptor_ID),
         FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
         FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

CREATE TABLE Application_Descriptor
(
         gateway_name VARCHAR(255),
         updated_user VARCHAR(255),
         application_descriptor_ID VARCHAR(255),
         host_descriptor_ID VARCHAR(255),
         service_descriptor_ID VARCHAR(255),
         application_descriptor_xml VARCHAR(2000),
         PRIMARY KEY(gateway_name,host_descriptor_ID, service_descriptor_ID,application_descriptor_ID),
         FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
         FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

CREATE TABLE Experiment
(
          project_ID INT(11),
	        gateway_name VARCHAR(255),
          user_name VARCHAR(255),
          experiment_ID VARCHAR(255),
          submitted_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
          PRIMARY KEY(experiment_ID),
          FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
          FOREIGN KEY (project_ID) REFERENCES Project(project_ID) ON DELETE CASCADE,
          FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);


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
        PRIMARY KEY(config_key, config_val)
);

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
       project_ID int(11) NOT NULL AUTO_INCREMENT,
       gateway_name varchar(255),
       user_name varchar(255),
       project_name varchar(255),
       PRIMARY KEY(project_ID),
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
       last_updated_date TIMESTAMP DEFAULT now() on update now(),
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
       host_descriptor_xml varchar(2000),
       PRIMARY KEY(gateway_name, host_descriptor_ID),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Service_Descriptor
(
         gateway_name varchar(255),
         updated_user varchar(255),
         service_descriptor_ID varchar(255),
         service_descriptor_xml varchar(2000),
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
         application_descriptor_xml varchar(2000),
         PRIMARY KEY(gateway_name,application_descriptor_ID),
         FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
         FOREIGN KEY (updated_user) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Experiment
(
          project_ID int(11),
	      gateway_name varchar(255),
          user_name varchar(255),
          experiment_ID varchar(255),
          submitted_date TIMESTAMP DEFAULT '0000-00-00 00:00:00',
          PRIMARY KEY(experiment_ID),
          FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
          FOREIGN KEY (project_ID) REFERENCES Project(project_ID) ON DELETE CASCADE,
          FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);


-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE TABLE IF NOT EXISTS user_table(
	userid VARCHAR(200) NOT NULL PRIMARY KEY,
	description TEXT,
	isAdmin BIT
)ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_table(
	groupid VARCHAR(200) NOT NULL PRIMARY KEY,
	description TEXT
)ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_groups_table (
	groupid VARCHAR(200) NOT NULL, 
	userid VARCHAR(200) NOT NULL,
	FOREIGN KEY (groupid) REFERENCES group_table(groupid),
	FOREIGN KEY (userid) REFERENCES user_table (userid)
)ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS resource_table(
	resourceid VARCHAR(200) NOT NULL ,
	owner VARCHAR(200) NOT NULL,
	created TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(resourceid,owner)
)ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_group_table(
	groupid VARCHAR(200) NOT NULL,
	contained_groupid VARCHAR(200) NOT NULL,
	FOREIGN KEY (groupid) REFERENCES group_table(groupid),
	FOREIGN KEY (contained_groupid) REFERENCES group_table(groupid)
)ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS hostdesc_table(
	resourceid VARCHAR(200) NOT NULL,
	host_name VARCHAR(200) NOT NULL PRIMARY KEY, INDEX(host_name),
	hostdesc_str LONGTEXT NOT NULL,
	FOREIGN KEY (resourceid) REFERENCES resource_table(resourceid)
);

CREATE TABLE IF NOT EXISTS appdesc_table(
	resourceid VARCHAR(200) NOT NULL,
	qname VARCHAR(200) NOT NULL,
	host_name VARCHAR(200) NOT NULL,
	appdesc_str LONGTEXT NOT NULL,
	PRIMARY KEY (qname,host_name),
	FOREIGN KEY (resourceid) REFERENCES resource_table(resourceid)/*,
	FOREIGN KEY (host_name) REFERENCES hostdesc_table(host_name) */
)ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS service_map_table(
	resourceid VARCHAR(200) NOT NULL,
	qname VARCHAR(200) NOT NULL PRIMARY KEY,
	servicemap_str LONGTEXT NOT NULL,
	awsdl_str LONGTEXT NOT NULL,
	FOREIGN KEY (resourceid) REFERENCES resource_table(resourceid)
)ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cwsdl_table(
	resourceid VARCHAR(200) NOT NULL,
	qname VARCHAR(200) NOT NULL PRIMARY KEY,
	wsdl_str LONGTEXT NOT NULL,
	time_stamp BIGINT,
	life_time BIGINT,
	port_type_name VARCHAR(200) NOT NULL,
	FOREIGN KEY (resourceid) REFERENCES resource_table(resourceid)
)ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS capability_table(
        owner VARCHAR(200) NOT NULL,
        resourceid VARCHAR(200) NOT NULL,
        allowed_actor VARCHAR(200) NOT NULL,
        isUser BIT,
        action_type VARCHAR(200) NOT NULL,
        assertions LONGTEXT,
        notbefore TIMESTAMP,
        notafter TIMESTAMP,
        PRIMARY KEY (resourceid,allowed_actor),
        FOREIGN KEY (owner) REFERENCES user_table (userid)
 )ENGINE=InnoDB;
 
 CREATE TABLE IF NOT EXISTS doc_table(
	resourceid VARCHAR(200) NOT NULL,
	doc_str LONGTEXT NOT NULL,
	FOREIGN KEY (resourceid) REFERENCES resource_table(resourceid)
);

CREATE TABLE IF NOT EXISTS ogce_resource_table(
    resourceid VARCHAR(200) NOT NULL,
    resourcename VARCHAR(200),
    resourcetype VARCHAR(200) NOT NULL,
    resourceDesc LONGTEXT,
    resourceDocument LONGTEXT,
    parentTypedID VARCHAR(200),
    created TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resourceid) REFERENCES resource_table(resourceid)
)ENGINE=InnoDB;
   

  
	
	
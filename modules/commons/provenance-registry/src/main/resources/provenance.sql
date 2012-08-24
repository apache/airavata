create table Experiment_Data
(
	experiment_ID varchar(255),
	name varchar(255),
	PRIMARY KEY (experiment_ID)
)

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
)

create table Node_Data
(
       workflow_instanceID varchar(255),
       node_id varchar(255),
       node_type varchar(255),
       inputs varchar(1000),
       outputs varchar(1000),
       status varchar(100),
       start_time TIMESTAMP DEFAULT '0000-00-00 00:00:00',
       last_update_time TIMESTAMP DEFAULT now() on update now(),
       PRIMARY KEY(workflow_instanceID, node_id),
       FOREIGN KEY (workflow_instanceID) REFERENCES Workflow_Data(workflow_instanceID) ON DELETE CASCADE
)

create table Gram_Data
(
       workflow_instanceID varchar(255),
       node_id varchar(255),
       rsl varchar(2000),
       invoked_host varchar(255),
       PRIMARY KEY(workflow_instanceID, node_id),
       FOREIGN KEY (workflow_instanceID) REFERENCES Workflow_Data(workflow_instanceID) ON DELETE CASCADE
)
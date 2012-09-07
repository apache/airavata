create table Gateway
(
        gateway_name varchar(255),
        PRIMARY KEY (gateway_name)
);

create table Configuration
(
        config_ID int(11) NOT NULL AUTO_INCREMENT,
        config_key varchar(255),
        config_val varchar(255),
        expire_date date,
        PRIMARY KEY(config_ID)
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
       publish_workflow_name varchar(255),
       version varchar(255),
       published_date DATE,
       workflow_content varchar(2000),
       PRIMARY KEY(gateway_name, publish_workflow_name),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE
);

create table User_Workflow
(
       gateway_name varchar(255),
       user_name varchar(255),
       user_workflow_name varchar(255),
       last_update_date DATE,
       workflow_content varchar(2000),
       PRIMARY KEY(project_ID, user_name, user_workflow_name),
       FOREIGN KEY (project_ID) REFERENCES Project(project_ID) ON DELETE CASCADE,
       FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);


create table Host_Descriptor
(
       gateway_name varchar(255),
       user_name varchar(255),
       host_descriptor_ID varchar(255),
       host_descriptor_xml varchar(2000),
       PRIMARY KEY(host_descriptor_ID),
       FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
       FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Service_Descriptor
(
         gateway_name varchar(255),
         user_name varchar(255),
         service_descriptor_ID varchar(255),
         service_descriptor_xml varchar(2000),
         PRIMARY KEY(service_descriptor_ID),
         FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
         FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Application_Descriptor
(
         gateway_name varchar(255),
         user_name varchar(255),
         application_descriptor_ID varchar(255),
         host_descriptor_ID varchar(255),
         service_descriptor_ID varchar(255),
         application_descriptor_xml varchar(2000),
         PRIMARY KEY(application_descriptor_ID),
         FOREIGN KEY (gateway_name) REFERENCES Gateway(gateway_name) ON DELETE CASCADE,
         FOREIGN KEY (host_descriptor_ID) REFERENCES Host_Descriptor(host_descriptor_ID) ON DELETE CASCADE,
         FOREIGN KEY (service_descriptor_ID) REFERENCES Service_Descriptor(service_descriptor_ID) ON DELETE CASCADE,
         FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);

create table Experiment
(
          project_ID int(11),
          user_name varchar(255),
          experiment_ID varchar(255),
          submitted_date Date,
          PRIMARY KEY(experiment_ID),
          FOREIGN KEY (project_ID) REFERENCES Project(project_ID) ON DELETE CASCADE,
          FOREIGN KEY (user_name) REFERENCES Users(user_name) ON DELETE CASCADE
);


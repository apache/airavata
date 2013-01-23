ALTER TABLE Configuration
ADD category_id varchar(255);

UPDATE Configuration SET category_id="SYSTEM" ;

ALTER TABLE Configuration DROP PRIMARY KEY, ADD PRIMARY KEY(config_key, config_val, category_id);

ALTER TABLE Node_Data
ADD execution_index int NOT NULL;

ALTER TABLE Node_Data DROP PRIMARY KEY, ADD PRIMARY KEY(workflow_instanceID, node_id, execution_index);
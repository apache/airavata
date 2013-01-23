ALTER TABLE Configuration ADD category_id varchar(255) NOT NULL DEFAULT 'SYSTEM';

ALTER TABLE Configuration DROP PRIMARY KEY;

ALTER TABLE Configuration ADD PRIMARY KEY(config_key, config_val, category_id);

ALTER TABLE Node_Data
ADD execution_index int NOT NULL DEFAULT 0;

ALTER TABLE Node_Data DROP PRIMARY KEY;

ALTER TABLE Node_Data ADD PRIMARY KEY(workflow_instanceID, node_id, execution_index);
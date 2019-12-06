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

CREATE TABLE DATA_PRODUCT
(
        PRODUCT_URI VARCHAR (255),
        GATEWAY_ID VARCHAR (255),
        PRODUCT_NAME VARCHAR (255),
        PRODUCT_DESCRIPTION VARCHAR (255),
        OWNER_NAME VARCHAR (255),
        PARENT_PRODUCT_URI VARCHAR (255),
        PRODUCT_SIZE INTEGER ,
        CREATION_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        LAST_MODIFIED_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',
        PRODUCT_TYPE VARCHAR(10),
        PRIMARY KEY (PRODUCT_URI)
        -- in Derby can't create self-reference FOREIGN KEY in CREATE TABLE
        -- statement so this gets added via the following ALTER TABLE statement
        -- FOREIGN KEY (PARENT_PRODUCT_URI) REFERENCES DATA_PRODUCT (PRODUCT_URI) ON DELETE CASCADE
);

ALTER TABLE DATA_PRODUCT ADD FOREIGN KEY (PARENT_PRODUCT_URI) REFERENCES DATA_PRODUCT (PRODUCT_URI) ON DELETE CASCADE;

CREATE TABLE DATA_REPLICA_LOCATION
(
        REPLICA_ID VARCHAR (255),
        PRODUCT_URI VARCHAR (255) NOT NULL,
        REPLICA_NAME VARCHAR (255),
        REPLICA_DESCRIPTION VARCHAR (255),
        STORAGE_RESOURCE_ID VARCHAR (255),
        FILE_PATH VARCHAR (1024),
        CREATION_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        LAST_MODIFIED_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',
        VALID_UNTIL_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',
        REPLICA_LOCATION_CATEGORY VARCHAR(26),
        REPLICA_PERSISTENT_TYPE VARCHAR(10),
        PRIMARY KEY (REPLICA_ID),
        FOREIGN KEY (PRODUCT_URI) REFERENCES DATA_PRODUCT(PRODUCT_URI) ON DELETE CASCADE
);

CREATE TABLE DATA_PRODUCT_METADATA
(
        PRODUCT_URI VARCHAR(255) NOT NULL,
        METADATA_KEY VARCHAR(255),
        METADATA_VALUE VARCHAR(255),
        PRIMARY KEY(PRODUCT_URI, METADATA_KEY),
        FOREIGN KEY (PRODUCT_URI) REFERENCES DATA_PRODUCT(PRODUCT_URI) ON DELETE CASCADE
);

CREATE TABLE DATA_REPLICA_METADATA
(
        REPLICA_ID VARCHAR(255) NOT NULL,
        METADATA_KEY VARCHAR(255),
        METADATA_VALUE VARCHAR(255),
        PRIMARY KEY(REPLICA_ID, METADATA_KEY),
        FOREIGN KEY (REPLICA_ID) REFERENCES DATA_REPLICA_LOCATION(REPLICA_ID) ON DELETE CASCADE
);


CREATE TABLE CONFIGURATION
(
        CONFIG_KEY VARCHAR(255),
        CONFIG_VAL VARCHAR(255),
        PRIMARY KEY(CONFIG_KEY, CONFIG_VAL)
);

INSERT INTO CONFIGURATION (CONFIG_KEY, CONFIG_VAL) VALUES('data_catalog_version', '0.16');

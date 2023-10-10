--
--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--
USE replica_catalog;

-- AIRAVATA-3697: Support file names that have UTF8 characters and that are long
ALTER TABLE
    DATA_PRODUCT
MODIFY
    PRODUCT_NAME text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

ALTER TABLE
    DATA_PRODUCT
MODIFY
    PRODUCT_DESCRIPTION varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

ALTER TABLE
    DATA_REPLICA_LOCATION
MODIFY
    REPLICA_NAME text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

ALTER TABLE
    DATA_REPLICA_LOCATION
MODIFY
    REPLICA_DESCRIPTION varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

ALTER TABLE
    DATA_REPLICA_LOCATION
MODIFY
    FILE_PATH varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

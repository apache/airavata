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

SET
    FOREIGN_KEY_CHECKS = 0;

ALTER DATABASE replica_catalog CHARACTER SET = utf8 COLLATE = utf8_bin;

ALTER TABLE
    DATA_PRODUCT CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;

ALTER TABLE
    DATA_REPLICA_LOCATION CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;

ALTER TABLE
    DATA_PRODUCT_METADATA CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;

ALTER TABLE
    DATA_REPLICA_METADATA CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;

ALTER TABLE
    CONFIGURATION CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;

SET
    FOREIGN_KEY_CHECKS = 1;

ALTER TABLE
    DATA_REPLICA_LOCATION
MODIFY
    REPLICA_NAME text;

ALTER TABLE
    DATA_PRODUCT
MODIFY
    PRODUCT_NAME text;

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

-- user_data_dir is the path to the gateway's data storage directory
set @user_data_dir = '/var/www/user_data/';
set @storage_id = '149.165.156.11_b5f26430-14d5-4372-8a7e-39b125aa640b';
update DATA_REPLICA_LOCATION
inner join (
    select
        REPLICA_ID,
        FILE_PATH,
        SUBSTR(FILE_PATH,
            LOCATE(@user_data_dir, FILE_PATH) + LENGTH(@user_data_dir),
            LOCATE('/', FILE_PATH, LOCATE(@user_data_dir, FILE_PATH) + LENGTH(@user_data_dir))
            - (LOCATE(@user_data_dir, FILE_PATH) + LENGTH(@user_data_dir))
        ) USERNAME
    from DATA_REPLICA_LOCATION where STORAGE_RESOURCE_ID = @storage_id
    and FILE_PATH like concat('%', @user_data_dir, '%')
) a
on a.REPLICA_ID = DATA_REPLICA_LOCATION.REPLICA_ID
set DATA_REPLICA_LOCATION.FILE_PATH = REPLACE(DATA_REPLICA_LOCATION.FILE_PATH, concat(@user_data_dir, a.USERNAME), concat(@user_data_dir, LOWER(a.USERNAME)));
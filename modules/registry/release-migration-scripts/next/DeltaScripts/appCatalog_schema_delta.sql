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

use app_catalog;

-- AIRAVATA-3276 Replace JSON configuration: "toggle": ["isRequired"] ->
-- "isRequired": true. Toggling requiredToAddedToCommandLine no longer needed.

-- replace toggle with is isRequired
update APPLICATION_INPUT
set METADATA = REGEXP_REPLACE(METADATA, '"toggle": \\[[^}]+\\]', CONCAT('"isRequired": ', IF(IS_REQUIRED=1, 'true', 'false')))
-- showOptions has "toggle" but not "isRequired"
where METADATA rlike '"showOptions": {"toggle": \\[[^}]+\\]'
  and NOT METADATA rlike '"showOptions": {.*"isRequired": (true|false)'
;

-- remove toggle since isRequired is already there
update APPLICATION_INPUT
set METADATA = REGEXP_REPLACE(METADATA, '(, )?"toggle": \\[[^}]+\\](, )?', '')
-- showOptions has BOTH "toggle" and "isRequired"
where METADATA rlike '"showOptions": {"toggle": \\[[^}]+\\]'
  and METADATA rlike '"showOptions": {.*"isRequired": (true|false)'
;

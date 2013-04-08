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
CREATE TABLE COMMUNITY_USER
(
	GATEWAY_NAME VARCHAR(256) NOT NULL,
	COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,
	COMMUNITY_USER_EMAIL VARCHAR(256) NOT NULL,
        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)
);


CREATE TABLE CREDENTIALS
(
	GATEWAY_NAME VARCHAR(256) NOT NULL,
	COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,
	CREDENTIAL CLOB NOT NULL,
	PRIVATE_KEY CLOB NOT NULL,
	NOT_BEFORE VARCHAR(256) NOT NULL,
	NOT_AFTER VARCHAR(256) NOT NULL,
	LIFETIME MEDIUMINT NOT NULL,
	REQUESTING_PORTAL_USER_NAME VARCHAR(256) NOT NULL,
	REQUESTED_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',
        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)
);
/**
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
 */
package org.apache.airavata.service.profile.commons.utils;

public class JPAConstants {
	public static final String KEY_JDBC_URL = "profile.service.jdbc.url";
	public static final String KEY_JDBC_USER = "profile.service.jdbc.user";
	public static final String KEY_JDBC_PASSWORD = "profile.service.jdbc.password";
	public static final String KEY_JDBC_DRIVER = "profile.service.jdbc.driver";

	// TODO: is this needed?
	public static final String KEY_DERBY_START_ENABLE = "profile.service.start.derby.server.mode";
	public static final String VALIDATION_QUERY = "profile.service.validationQuery";
	public static final String JPA_CACHE_SIZE = "profile.service.jpa.cache.size";
	public static final String ENABLE_CACHING = "profile.service.cache.enable";
}

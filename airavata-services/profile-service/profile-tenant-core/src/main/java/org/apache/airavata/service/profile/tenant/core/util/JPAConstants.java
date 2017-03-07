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

package org.apache.airavata.service.profile.tenant.core.util;
public class JPAConstants {
	public static final String KEY_JDBC_URL = "gateway.profile.catalog.registry.jdbc.url";
	public static final String KEY_JDBC_USER = "gateway.profile.catalog.registry.jdbc.user";
	public static final String KEY_JDBC_PASSWORD = "gateway.profile.catalog.registry.jdbc.password";
	public static final String KEY_JDBC_DRIVER = "gateway.profile.catalog.registry.jdbc.driver";
	// TODO: is this needed?
	public static final String KEY_DERBY_START_ENABLE = "gateway.profile.catalog.start.derby.server.mode";
	public static final String VALIDATION_QUERY = "gateway.profile.catalog.validationQuery";
	public static final String JPA_CACHE_SIZE = "gateway.profile.catalog.jpa.cache.size";
	public static final String ENABLE_CACHING = "gateway.profile.catalog.cache.enable";
}

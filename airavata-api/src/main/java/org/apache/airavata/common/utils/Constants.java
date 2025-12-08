/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.utils;

/**
 * Constants used in Airavata should go here.
 */
public final class Constants {
    public static final String JOB = "job";

    // API security relates property names
    public static final String SECURITY_MANAGER_CLASS = "security.iam.classpath";
    public static final String IS_TLS_ENABLED = "security.tls.enabled";
    public static final String KEYSTORE_PATH = "security.keystore.path";
    public static final String KEYSTORE_PASSWORD = "security.keystore.password";
    public static final String TLS_CLIENT_TIMEOUT = "security.tls.client-timeout";

    public static final String API_METHOD_NAME = "api.method.name";

    // constants in XACML authorization response.

    public static final String AUTHZ_CACHE_MANAGER_CLASS = "security.authzCache.classpath";
    public static final String AUTHZ_CACHE_ENABLED = "security.authzCache.enabled";

    public static final String IN_MEMORY_CACHE_SIZE = "airavata.in-memory-cache-size";
    public static final String LOCAL_DATA_LOCATION = "airavata.local-data-location";

    // Names of the attributes that could be passed in the AuthzToken's claims map.
    public static final String USER_NAME = "userName";
    public static final String GATEWAY_ID = "gatewayID";
    public static final String EMAIL = "email";

    public static final String ENABLE_STREAMING_TRANSFER = "airavata.enable-streaming-transfer";
}

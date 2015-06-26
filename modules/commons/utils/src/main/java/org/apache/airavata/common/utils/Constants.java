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

package org.apache.airavata.common.utils;

import java.lang.String;

/**
 * Constants used in Airavata should go here.
 */
public final class Constants {

    public static final String USER_IN_SESSION = "userName";
//    public static final String GATEWAY_NAME = "gateway_id";
    public static final String GFAC_CONFIG_XML = "gfac-config.xml";
    public static final String PUSH = "push";
    public static final String PULL = "pull";
    public static final String API_SERVER_PORT = "apiserver.server.port";
    public static final String API_SERVER_HOST = "apiserver.server.host";
    public static final String REGISTRY_JDBC_URL = "registry.jdbc.url";
    public static final String APPCATALOG_JDBC_URL = "appcatalog.jdbc.url";
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE = "rabbitmq.exchange.name";
    public static final String ORCHESTRATOR_SERVER_HOST = "orchestrator.server.host";
    public static final String ORCHESTRATOR_SERVER_PORT = "orchestrator.server.port";
    public static final String GFAC_SERVER_HOST = "gfac.server.host";
    public static final String GFAC_SERVER_PORT = "gfac.server.port";
    public static final String CREDENTIAL_SERVER_HOST = "credential.store.server.host";
    public static final String CREDENTIAL_SERVER_PORT = "credential.store.server.port";
    public static final String ZOOKEEPER_EXPERIMENT_CATALOG = "experiment-catalog";
    public static final String ZOOKEEPER_APPCATALOG = "app-catalog";
    public static final String ZOOKEEPER_RABBITMQ = "rabbit-mq";
    public static final String ZOOKEEPER_SERVER_HOST = "zookeeper.server.host";
    public static final String ZOOKEEPER_SERVER_PORT = "zookeeper.server.port";
    public static final String ZOOKEEPER_API_SERVER_NODE = "airavata-server";
    public static final String ZOOKEEPER_ORCHESTRATOR_SERVER_NODE = "orchestrator-server";
    public static final String ZOOKEEPER_GFAC_SERVER_NODE = "gfac-server";
    public static final String ZOOKEEPER_GFAC_EXPERIMENT_NODE = "gfac-experiments";
    public static final String ZOOKEEPER_GFAC_SERVER_NAME = "gfac-server-name";
    public static final String ZOOKEEPER_ORCHESTRATOR_SERVER_NAME = "orchestrator-server-name";
    public static final String ZOOKEEPER_API_SERVER_NAME = "api-server-name";
    public static final String STAT = "stat";
    public static final String JOB = "job";
    public static final String ZOOKEEPER_TIMEOUT = "zookeeper.timeout";
    //API security related property names
    public static final String IS_API_SECURED = "api.secured";
    public static final String SECURITY_MANAGER_CLASS = "security.manager.class";
    public static final String REMOTE_OAUTH_SERVER_URL = "remote.oauth.authorization.server";
    public static final String ADMIN_USERNAME = "admin.user.name";
    public static final String ADMIN_PASSWORD = "admin.password";
    public static final String IS_TLS_ENABLED = "TLS.enabled";
    public static final String TLS_SERVER_PORT = "TLS.api.server.port";
    public static final String KEYSTORE_PATH = "keystore.path";
    public static final String KEYSTORE_PASSWORD = "keystore.password";
    public static final String TLS_CLIENT_TIMEOUT = "TLS.client.timeout";
}

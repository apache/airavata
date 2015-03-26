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

package org.apache.airavata.testsuite.multitenantedairavata.utils;

public class TestFrameworkConstants {
    public static final String AIRAVATA_CLIENT_PROPERTIES = "airavata-client.properties";
    public static final String TEST_FREAMEWORK_PROPERTIES = "test-framework.properties";

    public static final class AiravataClientConstants {
        public static final String THRIFT_SERVER_HOST = "thrift.server.host";
        public static final String THRIFT_SERVER_PORT = "thrift.server.port";
    }

    public static final class FrameworkPropertiesConstants {
        public static final String NUMBER_OF_GATEWAYS = "numberof.gateways";
        public static final String USERS_PER_GATEWAY = "users.per.gateway";
        public static final String COMPUTE_RESOURCE_LIST = "compute.resoure.list";
        public static final String LOGIN_USERNAME_LIST = "login.usernames";
        public static final String APPLICATION_LIST = "application.list";
        public static final String SSH_PUBKEY_LOCATION = "ssh.pub.key.location";
        public static final String SSH_PRIKEY_LOCATION = "ssh.private.key.location";
        public static final String SSH_PWD = "ssh.password";
        public static final String SSH_USERNAME = "ssh.username";
    }

    public static final class AppcatalogConstants {
        public static final String AMBER_APP_NAME = "amber_sander";

    }

    public static final class CredentialStoreConstants {

    }


}

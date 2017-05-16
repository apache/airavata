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
package org.apache.airavata.testsuite.multitenantedairavata.utils;

public class TestFrameworkConstants {
    public static final String AIRAVATA_SERVER_PROPERTIES = "airavata-server.properties";
    public static final String TEST_FREAMEWORK_JSON = "test-framework.json";
    public static final String WORK_DIR = System.getProperty("user.dir");
    public static final String SCRATCH_LOCATION = System.getProperty("user.dir") + "/local-exp-resources/scratch/";
    public static final String STORAGE_LOCATION = System.getProperty("user.dir") + "/local-exp-resources/storage/";
    public static final String LOCAL_ECHO_JOB_FILE_PATH = TestFrameworkConstants.WORK_DIR +"/local-exp-resources/wrapper/echo_wrapper.sh";
    public static final class AiravataClientConstants {
        public static final String THRIFT_SERVER_HOST = "apiserver.host";
        public static final String THRIFT_SERVER_PORT = "apiserver.port";
        public static final String CS_JBDC_URL = "credential.store.jdbc.url";
        public static final String CS_JBDC_DRIVER = "credential.store.jdbc.driver";
        public static final String CS_DB_USERNAME = "credential.store.jdbc.user";
        public static final String CS_DB_PWD = "credential.store.jdbc.password";
        public static final String RABBIT_BROKER_URL = "rabbitmq.broker.url";
        public static final String RABBIT_EXCHANGE_NAME = "rabbitmq.status.exchange.name";
    }

    public static final class FrameworkPropertiesConstants {
        public static final String NUMBER_OF_GATEWAYS = "numberof.gateways";
        public static final String USERS_PER_GATEWAY = "users.per.gateway";
        public static final String COMPUTE_RESOURCE_LIST = "compute.resoure.list";
        public static final String LOGIN_USERNAME_LIST = "login.usernames";
        
        public static final String STORAGE_RESOURCE_LIST = "storage.resource.list";
        public static final String STORAGE_RESOURCE_LOGIN_USERBANE_LIST = "storage.resource.login.username";

        public static final String APPLICATION_LIST = "application.list";
        public static final String SSH_PUBKEY_LOCATION = "ssh.pub.key.location";
        public static final String SSH_PRIKEY_LOCATION = "ssh.private.key.location";
        public static final String SSH_PWD = "ssh.password";
        public static final String SSH_USERNAME = "ssh.username";
        public static final String TOKEN_WRITE_LOCATION = "token.file.location";
        public static final String RESULT_WRITE_LOCATION = "result.file.location";
        public static final String TEST_USER = "test.user";
        public static final String TEST_PROJECT = "test.project";
    }

    public static final class ErrorTypeConstants {
        public static final String BADINPUTS = "badinputs";
        public static final String ERROR_CONFIG = "error.configuration";
        public static final String ALLOCATION_PROJECT = "projectId";
        public static final String QUEUE_NAME = "queueName";
        public static final String WALLTIME = "walltime";
        public static final String HOST_NAME = "hostName";

    }

    public static final class GatewayConstants {
        public static final String GENERIC_GATEWAY_NAME = "generic.gateway.name";
        public static final String GENERIC_GATEWAY_DOMAIN = "generic.gateway.domain";
    }

    public static final class AppcatalogConstants {

        public static final String LOCAL_ECHO_NAME = "LocalEcho";

        public static final String LOCAL_ECHO_VERSION= "1.0";

        public static final String LOCAL_ECHO_DESCRIPTION = "A Simple Local Echo Application";

        public static final String LOCAL_RESOURCE_NAME = "localhost";

    }

    public static final class CredentialStoreConstants {
        public static final String TOKEN_FILE_NAME = "testFrameworkTokens";

    }

    public final class LocalEchoProperties {

        public static final String HOST_NAME = "localhost";
        public static final String HOST_DESC = "localhost";
        public static final String LOCAL_ECHO_EXPERIMENT_INPUT = "HelloWorld!!!";
        public static final String LOCAL_ECHO_EXPERIMENT_EXPECTED_OUTPUT = "Echoed_Output=HelloWorld!!!";

        public static final String RESOURCE_NAME = "localhost";
        public static final String LOGIN_USER = "airavata";

        public final class LocalEchoComputeResource{
            public static final String JOB_MANAGER_COMMAND = "/bin/bash";
            public static final String ALLOCATION_PROJECT_NUMBER= "local1.0";
            public static final String BATCH_QUEUE = "CPU";
        }


        public final class LocalApplication{
            public static final String INPUT_NAME = "input";
            public static final String INPUT_VALUE = "LocalEchoTest";
            public static final String INPUT_DESC = "Sample input to Local Echo";

            public static final String STDOUT_NAME = "STDOUT";
            public static final String STDOUT_VALUE = "stdout.txt";

            public static final String STDERR_NAME = "STDERR";
            public static final String STDERR_VALUE = "stderr.txt";
        }

    }

}

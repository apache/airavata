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
package org.apache.airavata.client.secure.client;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureClient {
    private static Logger logger = LoggerFactory.getLogger(SecureClient.class);

    public static void main(String[] args) throws Exception {
        // TODO Implement in keycloak
    }

    public static Airavata.Client createAiravataClient(String serverHost, int serverPort) throws
            AiravataClientException {

        //Airavata.Client client = AiravataClientFactory.createAiravataClient(serverHost, serverPort);
        Airavata.Client client = AiravataClientFactory.createAiravataSecureClient(serverHost, serverPort,
                Properties.TRUST_STORE_PATH, Properties.TRUST_STORE_PASSWORD, 10000);
        return client;
    }
}

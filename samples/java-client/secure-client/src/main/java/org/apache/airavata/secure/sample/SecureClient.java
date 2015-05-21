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
package org.apache.airavata.secure.sample;

import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.api.Airavata;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureClient {
    private static Logger logger = LoggerFactory.getLogger(SecureClient.class);

    public static void main(String[] args) throws AiravataClientConnectException, TException {
        Airavata.Client client = createAiravataClient(Constants.SERVER_HOST, Constants.SERVER_PORT);
        String version = client.getAPIVersion();
        System.out.println("Airavata API version: " + version);
    }

    public static Airavata.Client createAiravataClient(String serverHost, int serverPort) throws
            AiravataClientConnectException {
        try {
            Airavata.Client client = AiravataClientFactory.createAiravataClient(serverHost, serverPort);
            return client;

        } catch (AiravataClientConnectException e) {
            logger.error("Error while creating Airavata Client.");
            throw e;
        }
    }

    public static String getAPIVersion(Airavata.Client client) throws TException {
        try {
            return client.getAPIVersion();
        } catch (InvalidRequestException e) {
            logger.error("Error in retrieving API version.");
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error in retrieving API version.");
            throw new AiravataClientException(e);
        } catch (AiravataSystemException e) {
            logger.error("Error in retrieving API version.");
            throw new AiravataSystemException(e);
        } catch (TException e) {
            logger.error("Error in retrieving API version.");
            throw new TException(e);
        }
    }


}

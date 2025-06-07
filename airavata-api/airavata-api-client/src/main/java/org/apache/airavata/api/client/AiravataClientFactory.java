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
package org.apache.airavata.api.client;

import org.apache.airavata.api.Airavata;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiravataClientFactory {

    private final static Logger logger = LoggerFactory.getLogger(AiravataClientFactory.class);

    public static Airavata.Client createAiravataClient(String serverHost, int serverPort) throws AiravataClientException{
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
//            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, "APIServer");
            return new Airavata.Client(protocol);
        } catch (TTransportException e) {
            AiravataClientException exception = new AiravataClientException();
            exception.setParameter("Unable to connect to the server at "+serverHost+":"+serverPort);
            throw exception;
        }
    }

    /**
     * This method returns a Airavata Client that talks to the API Server exposed over TLS.
     *
     * @param serverHost
     * @param serverPort
     * @param trustStorePath
     * @param trustStorePassword
     * @param clientTimeOut
     * @return
     * @throws AiravataClientConnectException
     */
    public static Airavata.Client createAiravataSecureClient(String serverHost, int serverPort, String trustStorePath,
                                                             String trustStorePassword, int clientTimeOut)
            throws AiravataClientException {
        try {
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            params.setTrustStore(trustStorePath, trustStorePassword);
            TSocket transport = TSSLTransportFactory.getClientSocket(serverHost, serverPort, clientTimeOut, params);
            TProtocol protocol = new TBinaryProtocol(transport);
            return new Airavata.Client(protocol);
        } catch (TTransportException e) {
            logger.error(e.getMessage(), e);
            AiravataClientException clientError = new AiravataClientException();
            clientError.setParameter("Unable to connect to the server at " + serverHost + ":" + serverPort);
            throw clientError;
        }
    }

    public static void main(String a[]) throws TException {
        AuthzToken token = new AuthzToken();
        token.setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJBUGFKRUpERFc4ZEdzMExnc3ozYUdydERsZ2U0eWlQblFibUNsYnpJX2NVIn0.eyJqdGkiOiI1NmMwZDZmYy0yMGVhLTQ1Y2UtODUwNC1kMTY0MTZkYTdkYzEiLCJleHAiOjE2NDc0NTQyNjcsIm5iZiI6MCwiaWF0IjoxNjQ3NDUyNDY3LCJpc3MiOiJodHRwczovL2lhbWRldi5zY2lnYXAub3JnL2F1dGgvcmVhbG1zL3NlYWdyaWQiLCJhdWQiOiJwZ2EiLCJzdWIiOiI3ZGZkYjI4MS1lNWIzLTQ4MjQtOTcxZC00YzQ2ZmNkMzIwYTEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJwZ2EiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI1NWVkODI5OS0xN2FiLTQwNTEtYTBjYy0zMjgzNWQ1MTVlNjUiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiIwMjU2OTljNS1lY2I2LTQ2ZDYtYmYwNy01ZDczOTk1ZTI3YjMiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly9kZXYuc2VhZ3JpZC5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicGdhIjp7InJvbGVzIjpbImdhdGV3YXktdXNlciJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sIm5hbWUiOiJFcm9tYSBBYmV5c2luZ2hlIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiMjAyMXRlc3QxIiwiZ2l2ZW5fbmFtZSI6IkVyb21hIiwiZmFtaWx5X25hbWUiOiJBYmV5c2luZ2hlIiwiZW1haWwiOiJlcm9tYS5hYmV5c2luZ2hlQGdtYWlsLmNvbSJ9.eMIrTzyc43CLkxCauiXIwPV99CmsBDbSbiIVEE9Qd3ASyJKXlzkrWsUVPE-g43i1iBKaHBcnLPkmzVz8Hb0B1wtDA5nKSgipGYjfJfaWdMzBrW1PkpeWMKDZHN3m4OS7YZnzQki0YJFvL1-IZsYf2UCnr_lsOi2M-dnj9xwEJ_VIdvvHl9I6ivhBUywYDU0uL9EoSL3kAes7FvooOhXnZiRxJpZK82VPZZiVAb-nv5xgCwQw0ipbm8b0kIta4cxhjKKDhyINRvGXJjqN3kRNsahYHLnwsRqRjabgvbSfe4vtS5iRoPO-qF-I-rSMf2jZPREMWxdLQ9uPXEk9mFxqbQ");
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.GATEWAY_ID, "seagrid");
        claimsMap.put(Constants.USER_NAME, "2021test1");
        token.setClaimsMap(claimsMap);
        Airavata.Client apiClient = createAiravataSecureClient("apidev.scigap.org", 9930,
                "/Users/eromaabeysinghe/development/local-airavata/airavata/dev-tools/ansible/inventories/scigap/production/files/client_truststore.jks", "airavata",
                10000);



        List<String> outputNames = new ArrayList<>();
        outputNames.add("Gaussian-Application-Output");
        outputNames.add("Gaussian-Standar-Out");
            apiClient.fetchIntermediateOutputs(token,"Clone_of_Gaussian16_on_Mar_16,_2022_1:42_PM_1ad9e887-6ec4-4b1a-9ffb-e028ccb3c86c", outputNames);
    }
}
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

import org.apache.airavata.model.error.AiravataClientException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
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
package org.apache.airavata.thriftapi.client;

import java.io.File;
import org.apache.airavata.common.exception.AiravataClientException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.thriftapi.service.Airavata;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class AiravataServiceClientFactory {

    private static final String AIRAVATA_SERVICE_NAME = "Airavata";

    public static Airavata.Client createAiravataClient(
            String serverHost, int serverPort, boolean secure, AiravataServerProperties properties)
            throws AiravataClientException {
        try {
            TTransport transport;
            if (!secure) {
                transport = new TSocket(serverHost, serverPort);
                transport.open();
            } else {
                // TLS enabled client
                var params = new TSSLTransportFactory.TSSLTransportParameters();
                String configDir =
                        org.apache.airavata.config.AiravataConfigUtils.getConfigDir(); // Will throw if not found
                if (properties.security == null
                        || properties.security.tls == null
                        || properties.security.tls.keystore == null
                        || properties.security.tls.keystore.path == null) {
                    throw new IllegalStateException(
                            "TLS keystore configuration is missing: security.tls.keystore.path is not set in airavata.properties");
                }
                String keystorePath = properties.security.tls.keystore.path;
                // Keystore path is relative to configDir (e.g., "keystores/airavata.p12")
                String keystoreFullPath = new File(configDir, keystorePath).getAbsolutePath();
                params.setKeyStore(keystoreFullPath, properties.security.tls.keystore.password);
                transport = TSSLTransportFactory.getClientSocket(serverHost, serverPort, 10000, params);
            }

            var protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol multiplexedProtocol = new TMultiplexedProtocol(protocol, AIRAVATA_SERVICE_NAME);
            return new Airavata.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            AiravataClientException exception = new AiravataClientException();
            exception.setParameter("Unable to connect to the server at " + serverHost + ":" + serverPort);
            throw exception;
        }
    }
}

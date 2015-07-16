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

package org.apache.airavata.credential.store.client;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class CredentialStoreClientFactory {

    public static CredentialStoreService.Client createAiravataCSClient(String serverHost, int serverPort) throws CredentialStoreException {
        TTransport transport;
        try {
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            String keystorePath = ServerSettings.getCredentialStoreThriftServerKeyStorePath();
            String keystorePWD = ServerSettings.getCredentialStoreThriftServerKeyStorePassword();
            params.setTrustStore(keystorePath, keystorePWD);

            transport = TSSLTransportFactory.getClientSocket(serverHost, serverPort, 10000, params);
            TProtocol protocol = new TBinaryProtocol(transport);

            CredentialStoreService.Client client = new CredentialStoreService.Client(protocol);
            return client;
        } catch (TTransportException e) {
            throw new CredentialStoreException("Unable to connect to the credential store server at " + serverHost + ":" + serverPort);
        } catch (ApplicationSettingsException e) {
            throw new CredentialStoreException("Unable to connect to the credential store server at " + serverHost + ":" + serverPort);
        }
    }
}
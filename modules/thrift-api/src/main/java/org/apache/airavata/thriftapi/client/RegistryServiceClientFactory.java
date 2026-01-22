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

import org.apache.airavata.thriftapi.registry.exception.RegistryServiceException;
import org.apache.airavata.thriftapi.registry.model.RegistryService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

public class RegistryServiceClientFactory {

    private static final String REGISTRY_SERVICE_NAME = "RegistryService";

    public static RegistryService.Client createRegistryClient(String serverHost, int serverPort)
            throws RegistryServiceException {
        try {
            var transport = new TSocket(serverHost, serverPort);
            transport.open();
            var protocol = new TBinaryProtocol(transport);
            var multiplexedProtocol = new TMultiplexedProtocol(protocol, REGISTRY_SERVICE_NAME);
            return new RegistryService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            throw new RegistryServiceException();
        }
    }
}

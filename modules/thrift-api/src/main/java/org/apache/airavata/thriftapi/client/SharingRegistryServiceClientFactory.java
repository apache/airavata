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

import org.apache.airavata.thriftapi.sharing.model.SharingRegistryException;
import org.apache.airavata.thriftapi.sharing.model.SharingRegistryService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharingRegistryServiceClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(SharingRegistryServiceClientFactory.class);
    private static final String SHARING_REGISTRY_SERVICE_NAME = "SharingRegistryService";

    public static SharingRegistryService.Client createSharingRegistryClient(String serverHost, int serverPort)
            throws SharingRegistryException {
        try {
            TSocket transport = new TSocket(serverHost, serverPort);
            transport.open();
            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol multiplexedProtocol =
                    new TMultiplexedProtocol(protocol, SHARING_REGISTRY_SERVICE_NAME);
            return new SharingRegistryService.Client(multiplexedProtocol);
        } catch (TTransportException e) {
            logger.error("failed to create sharing registry client", e);
            throw new SharingRegistryException();
        }
    }
}

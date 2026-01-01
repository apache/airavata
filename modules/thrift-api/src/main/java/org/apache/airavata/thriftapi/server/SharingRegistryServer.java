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
package org.apache.airavata.thriftapi.server;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.sharing.messaging.SharingServiceDBEventMessagingFactory;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.thriftapi.handler.SharingRegistryServerHandler;
import org.apache.airavata.thriftapi.sharing.model.SharingRegistryService;
import org.apache.airavata.thriftapi.util.SharingRegistryConstants;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "services.thrift.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SharingRegistryServer extends ServerLifecycle {

    public static final String SHARING_REG_SERVER_HOST = "sharing.registry.server.host";
    public static final String SHARING_REG_SERVER_PORT = "sharing.registry.server.port";

    private static final String SERVER_NAME = "Sharing Registry Server";
    private static final String SERVER_VERSION = "1.0";

    private TServer server;
    // Unused field - commented out
    // private boolean testMode = false;

    private final AiravataServerProperties properties;
    private final SharingServiceDBEventMessagingFactory messagingFactory;
    private final SharingRegistryServerHandler handler;

    public SharingRegistryServer(
            AiravataServerProperties properties,
            SharingServiceDBEventMessagingFactory messagingFactory,
            SharingRegistryServerHandler handler) {
        this.properties = properties;
        this.messagingFactory = messagingFactory;
        this.handler = handler;
    }

    @Override
    public String getServerName() {
        return SERVER_NAME;
    }

    @Override
    public String getServerVersion() {
        return SERVER_VERSION;
    }

    @Override
    public int getPhase() {
        // Sharing Server starts after Credential Store
        return 40;
    }

    @Override
    public boolean isRunning() {
        return server != null && server.isServing();
    }

    @Override
    protected void doStart() throws Exception {
        try {

            final int serverPort = properties.services.sharing.serverPort;
            var processor = new SharingRegistryService.Processor<>(handler);

            TServerTransport serverTransport;
            TThreadPoolServer.Args options;

            if (!properties.security.tls.enabled) {
                serverTransport = new TServerSocket(serverPort);
                options = new TThreadPoolServer.Args(serverTransport);
            } else {
                TSSLTransportFactory.TSSLTransportParameters TLSParams =
                        new TSSLTransportFactory.TSSLTransportParameters();
                TLSParams.requireClientAuth(true);
                java.io.File configDir = new java.io.File(properties.airavataConfigDir);
                java.io.File keystoreFile = new java.io.File(configDir, properties.security.keystore.path);
                TLSParams.setKeyStore(keystoreFile.getAbsolutePath(), properties.security.keystore.password);
                TServerSocket TLSServerTransport = TSSLTransportFactory.getServerSocket(
                        serverPort, properties.security.tls.clientTimeout, null, TLSParams);
                options = new TThreadPoolServer.Args(TLSServerTransport);
            }
            options.minWorkerThreads = 30;
            server = new TThreadPoolServer(options.processor(processor));

            new Thread(() -> {
                        server.serve();
                        logger.info("Sharing Registry Server Stopped.");
                    })
                    .start();
            new Thread(() -> {
                        while (!server.isServing()) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (server.isServing()) {

                            try {
                                logger.info("Register sharing service with DB Event publishers");
                                messagingFactory.registerSharingServiceWithPublishers(
                                        SharingRegistryConstants.PUBLISHERS);

                                logger.info("Start sharing service DB Event subscriber");
                                messagingFactory.getDBEventSubscriber();
                            } catch (AiravataException | SharingRegistryException e) {
                                logger.error("Error starting sharing service. Error setting up DB event services.");
                                server.stop();
                            }
                            logger.info("Starting Sharing Registry Server on Port " + serverPort);
                            logger.info("Listening to Sharing Registry server clients ....");
                        }
                    })
                    .start();

        } catch (TTransportException e) {
            throw new Exception("Error while starting the Sharing Registry service", e);
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (server != null && server.isServing()) {
            server.stop();
        }
    }

    public TServer getServer() {
        return server;
    }

    public void setServer(TServer server) {
        this.server = server;
    }

    // Unused method - commented out (testMode field also unused)
    /*
     * public void setTestMode(boolean testMode) {
     * this.testMode = testMode;
     * }
     */

    // Unused method - commented out
    /*
     * private SharingRegistryDBInitConfig createSharingRegistryDBInitConfig() {
     * SharingRegistryDBInitConfig sharingRegistryDBInitConfig = new
     * SharingRegistryDBInitConfig();
     * if (this.testMode) {
     * sharingRegistryDBInitConfig.setDBInitScriptPrefix("sharing-registry");
     * }
     * return sharingRegistryDBInitConfig;
     * }
     */
}

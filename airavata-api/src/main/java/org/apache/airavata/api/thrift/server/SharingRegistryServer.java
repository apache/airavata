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
package org.apache.airavata.api.thrift.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.apache.airavata.api.thrift.handler.SharingRegistryServerHandler;
import org.apache.airavata.api.thrift.util.SharingRegistryConstants;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.sharing.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.sharing.messaging.SharingServiceDBEventMessagingFactory;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.service.cpi.SharingRegistryService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SharingRegistryServer implements IServer {
    private static final Logger logger = LoggerFactory.getLogger(SharingRegistryServer.class);

    public static final String SHARING_REG_SERVER_HOST = "sharing.registry.server.host";
    public static final String SHARING_REG_SERVER_PORT = "sharing.registry.server.port";

    private static final String SERVER_NAME = "Sharing Registry Server";
    private static final String SERVER_VERSION = "1.0";

    private IServer.ServerStatus status;
    private TServer server;
    private boolean testMode = false;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiravataServerProperties properties;

    public SharingRegistryServer() {
        setStatus(IServer.ServerStatus.STOPPED);
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }

    @Override
    public void start() throws Exception {
        try {
            setStatus(IServer.ServerStatus.STARTING);

            final int serverPort = properties.getSharing().getServerPort();
            final String serverHost = properties.getSharing().getServerHost();
            // SharingRegistryServerHandler doesn't need DBInitConfig anymore - it's a Spring bean
            SharingRegistryServerHandler handler = applicationContext.getBean(SharingRegistryServerHandler.class);
            SharingRegistryService.Processor<SharingRegistryServerHandler> processor =
                    new SharingRegistryService.Processor<>(handler);

            TServerTransport serverTransport;
            TThreadPoolServer.Args options;

            if (!properties.getSecurity().getTls().isEnabled()) {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
                options = new TThreadPoolServer.Args(serverTransport);
            } else {
                TSSLTransportFactory.TSSLTransportParameters TLSParams =
                        new TSSLTransportFactory.TSSLTransportParameters();
                TLSParams.requireClientAuth(true);
                java.io.File configDir = new java.io.File(properties.getAiravataConfigDir());
                java.io.File keystoreFile = new java.io.File(
                        configDir, properties.getSecurity().getKeystore().getPath());
                TLSParams.setKeyStore(
                        keystoreFile.getAbsolutePath(),
                        properties.getSecurity().getKeystore().getPassword());
                TServerSocket TLSServerTransport = TSSLTransportFactory.getServerSocket(
                        serverPort,
                        properties.getSecurity().getTls().getClientTimeout(),
                        InetAddress.getByName(serverHost),
                        TLSParams);
                options = new TThreadPoolServer.Args(TLSServerTransport);
            }
            options.minWorkerThreads = 30;
            server = new TThreadPoolServer(options.processor(processor));

            new Thread(() -> {
                        server.serve();
                        setStatus(ServerStatus.STOPPED);
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
                                SharingServiceDBEventMessagingFactory.registerSharingServiceWithPublishers(
                                        SharingRegistryConstants.PUBLISHERS);

                                logger.info("Start sharing service DB Event subscriber");
                                SharingServiceDBEventMessagingFactory.getDBEventSubscriber();
                            } catch (AiravataException | SharingRegistryException e) {
                                logger.error("Error starting sharing service. Error setting up DB event services.");
                                server.stop();
                            }
                            setStatus(ServerStatus.STARTED);
                            logger.info("Starting Sharing Registry Server on Port " + serverPort);
                            logger.info("Listening to Sharing Registry server clients ....");
                        }
                    })
                    .start();

        } catch (TTransportException e) {
            setStatus(IServer.ServerStatus.FAILED);
            throw new Exception("Error while starting the Sharing Registry service", e);
        }
    }

    @Override
    public void stop() throws Exception {
        if (server != null && server.isServing()) {
            setStatus(IServer.ServerStatus.STOPING);
            server.stop();
        }
    }

    @Override
    public void restart() throws Exception {
        stop();
        start();
    }

    @Override
    public void configure() throws Exception {}

    @Override
    public IServer.ServerStatus getStatus() throws Exception {
        return status;
    }

    private void setStatus(IServer.ServerStatus stat) {
        status = stat;
        status.updateTime();
    }

    public TServer getServer() {
        return server;
    }

    public void setServer(TServer server) {
        this.server = server;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    private SharingRegistryDBInitConfig createSharingRegistryDBInitConfig() {
        SharingRegistryDBInitConfig sharingRegistryDBInitConfig = new SharingRegistryDBInitConfig();
        if (this.testMode) {
            sharingRegistryDBInitConfig.setDBInitScriptPrefix("sharing-registry");
        }
        return sharingRegistryDBInitConfig;
    }
}

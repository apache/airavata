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
package org.apache.airavata.sharing.registry.server;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.sharing.registry.messaging.SharingServiceDBEventMessagingFactory;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.sharing.registry.utils.Constants;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SharingRegistryServer implements IServer {
    private final static Logger logger = LoggerFactory.getLogger(SharingRegistryServer.class);

    public static final String SHARING_REG_SERVER_HOST = "sharing.registry.server.host";
    public static final String SHARING_REG_SERVER_PORT = "sharing.registry.server.port";

    private static final String SERVER_NAME = "Sharing Registry Server";
    private static final String SERVER_VERSION = "1.0";

    private IServer.ServerStatus status;
    private TServer server;
    private boolean testMode = false;

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

            final int serverPort = Integer.parseInt(ServerSettings.getSetting(SHARING_REG_SERVER_PORT));
            final String serverHost = ServerSettings.getSetting(SHARING_REG_SERVER_HOST);
            SharingRegistryService.Processor processor = new SharingRegistryService.Processor(
                    new SharingRegistryServerHandler(createSharingRegistryDBInitConfig()));

            TServerTransport serverTransport;

            if (!ServerSettings.isSharingTLSEnabled()) {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
                TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
                options.minWorkerThreads = 30;
                server = new TThreadPoolServer(options.processor(processor));
            }else{
                TSSLTransportFactory.TSSLTransportParameters TLSParams =
                        new TSSLTransportFactory.TSSLTransportParameters();
                TLSParams.requireClientAuth(true);
                TLSParams.setKeyStore(ServerSettings.getKeyStorePath(), ServerSettings.getKeyStorePassword());
                if (ServerSettings.isTrustStorePathDefined()) {
                    TLSParams.setTrustStore(ServerSettings.getTrustStorePath(), ServerSettings.getTrustStorePassword());
                }
                TServerSocket TLSServerTransport = TSSLTransportFactory.getServerSocket(
                        serverPort, ServerSettings.getTLSClientTimeout(),
                        InetAddress.getByName(serverHost), TLSParams);
                TThreadPoolServer.Args options = new TThreadPoolServer.Args(TLSServerTransport);
                options.minWorkerThreads = 30;
                server = new TThreadPoolServer(options.processor(processor));
            }

            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(IServer.ServerStatus.STOPPED);
                    logger.info("Sharing Registry Server Stopped.");
                }
            }.start();
            new Thread() {
                public void run() {
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
                            SharingServiceDBEventMessagingFactory.registerSharingServiceWithPublishers(Constants.PUBLISHERS);

                            logger.info("Start sharing service DB Event subscriber");
                            SharingServiceDBEventMessagingFactory.getDBEventSubscriber();
                        } catch (AiravataException | SharingRegistryException e) {
                            logger.error("Error starting sharing service. Error setting up DB event services.");
                            server.stop();
                        }
                        setStatus(IServer.ServerStatus.STARTED);
                        logger.info("Starting Sharing Registry Server on Port " + serverPort);
                        logger.info("Listening to Sharing Registry server clients ....");
                    }
                }
            }.start();

        } catch (TTransportException e) {
            setStatus(IServer.ServerStatus.FAILED);
            throw new Exception("Error while starting the Sharing Registry service", e);
        }
    }

    @Override
    public void stop() throws Exception {
        if (server!=null && server.isServing()){
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
    public void configure() throws Exception {

    }

    @Override
    public IServer.ServerStatus getStatus() throws Exception {
        return status;
    }

    private void setStatus(IServer.ServerStatus stat){
        status=stat;
        status.updateTime();
    }

    public TServer getServer() {
        return server;
    }

    public void setServer(TServer server) {
        this.server = server;
    }

    public boolean isTestMode() {
        return testMode;
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

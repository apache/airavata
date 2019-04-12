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
package org.apache.airavata.registry.api.service;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.registry.api.service.messaging.RegistryServiceDBEventMessagingFactory;
import org.apache.airavata.registry.api.service.util.Constants;
import org.apache.airavata.registry.core.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ReplicaCatalogDBInitConfig;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryAPIServer implements IServer {
    private final static Logger logger = LoggerFactory.getLogger(RegistryAPIServer.class);

    private static final String SERVER_NAME = "Registry API Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

    private TServer server;

    private List<DBInitConfig> dbInitConfigs = Arrays.asList(
        new ExpCatalogDBInitConfig(),
        new AppCatalogDBInitConfig(),
        new ReplicaCatalogDBInitConfig());

    public RegistryAPIServer() {
        setStatus(ServerStatus.STOPPED);
    }

    public void StartRegistryServer(RegistryService.Processor<RegistryServerHandler> orchestratorServerHandlerProcessor)
            throws Exception {

        logger.info("Initializing databases...");
        for (DBInitConfig dbInitConfig : dbInitConfigs) {
            DBInitializer.initializeDB(dbInitConfig);
        }
        logger.info("Databases initialized successfully");

        final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.REGISTRY_SERVER_PORT, "8960"));
        try {
            final String serverHost = ServerSettings.getSetting(Constants.REGISTRY_SERVER_HOST, null);
            TServerTransport serverTransport;
            if(serverHost == null){
                serverTransport = new TServerSocket(serverPort);
            }else{
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
            }

            // thrift server start
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = Integer.parseInt(ServerSettings.getSetting(Constants.REGISTRY_SERVER_MIN_THREADS, "30"));
            server = new TThreadPoolServer(options.processor(orchestratorServerHandlerProcessor));
            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(ServerStatus.STOPPED);
                    logger.info("Registry Server Stopped.");
                }
            }.start();
            new Thread() {
                public void run() {
                    while(!server.isServing()){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (server.isServing()){
                        setStatus(ServerStatus.STARTED);
                        logger.info("Started Registry Server on Port " + serverPort + " ...");

                        // start db event handlers
                        if (!startDatabaseEventHandlers()) {
                            logger.error("Stopping Registry Server as DB event handlers failed to start!");
                            server.stop();
                        }
                    }
                }
            }.start();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            logger.error("Failed to start Registry server on port " + serverPort + " ...");
        }
    }

    private boolean startDatabaseEventHandlers() {
        try {
            // db-event handlers
            logger.info("Registring registry service with publishers for db-events.");
            RegistryServiceDBEventMessagingFactory.registerRegistryServiceWithPublishers(Constants.DB_EVENT_SUBSCRIBERS);

            logger.info("Starting registry service db-event-handler subscriber.");
            RegistryServiceDBEventMessagingFactory.getDBEventSubscriber();
        } catch (Exception ex) {
            logger.error("Failed to start database event handlers, reason: " + ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            new RegistryAPIServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void start() throws Exception {
        setStatus(ServerStatus.STARTING);
        RegistryService.Processor<RegistryServerHandler> orchestratorService =
                new RegistryService.Processor<RegistryServerHandler>(new RegistryServerHandler());
        StartRegistryServer(orchestratorService);
    }

    @Override
    public void stop() throws Exception {
        if (server!=null && server.isServing()){
            setStatus(ServerStatus.STOPING);
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
        // TODO Auto-generated method stub

    }

    @Override
    public ServerStatus getStatus() throws Exception {
        return status;
    }

    private void setStatus(ServerStatus stat){
        status=stat;
        status.updateTime();
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }

}

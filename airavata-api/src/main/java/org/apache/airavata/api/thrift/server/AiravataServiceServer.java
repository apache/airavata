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

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.thrift.handler.AiravataServiceHandler;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
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
public class AiravataServiceServer implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(AiravataServiceServer.class);
    private static final String SERVER_NAME = "Airavata API Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

    private TServer server, TLSServer;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiravataServerProperties properties;

    public AiravataServiceServer() {
        setStatus(ServerStatus.STOPPED);
    }

    public void startAiravataServer(Airavata.Processor<Airavata.Iface> airavataAPIServer)
            throws AiravataSystemException {
        try {
            final int serverPort = properties.services.api.port;

            if (!properties.security.tls.enabled) {
                TServerTransport serverTransport = new TServerSocket(serverPort);
                TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
                options.minWorkerThreads = properties.services.api.minThreads;
                server = new TThreadPoolServer(options.processor(airavataAPIServer));
                new Thread(() -> {
                            server.serve();
                            setStatus(ServerStatus.STOPPED);
                            logger.info("Airavata API Server Stopped.");
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
                                setStatus(ServerStatus.STARTED);
                                logger.info("Starting Airavata API Server on Port " + serverPort);
                                logger.info("Listening to Airavata Clients ....");
                            }
                        })
                        .start();
                logger.info("Started API Server ....");
            } else {
                var TLSParams = new TSSLTransportFactory.TSSLTransportParameters();
                java.io.File configDir = new java.io.File(properties.airavataConfigDir);
                java.io.File keystoreFile = new java.io.File(configDir, properties.security.keystore.path);
                TLSParams.setKeyStore(keystoreFile.getAbsolutePath(), properties.security.keystore.password);
                var TLSServerTransport = TSSLTransportFactory.getServerSocket(
                        serverPort, properties.security.tls.clientTimeout, null, TLSParams);
                TThreadPoolServer.Args settings = new TThreadPoolServer.Args(TLSServerTransport);
                settings.minWorkerThreads = properties.services.api.minThreads;
                TLSServer = new TThreadPoolServer(settings.processor(airavataAPIServer));
                new Thread(() -> {
                            TLSServer.serve();
                            setStatus(ServerStatus.STOPPED);
                            logger.info("Airavata API Server over TLS Stopped.");
                        })
                        .start();
                new Thread(() -> {
                            while (!TLSServer.isServing()) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                            if (TLSServer.isServing()) {
                                setStatus(ServerStatus.STARTED);
                            }
                        })
                        .start();
                logger.info("API server started over TLS on Port: " + serverPort + " ...");
            }

        } catch (TTransportException e) {
            logger.error("Failed to start API server ...", e);
            setStatus(ServerStatus.FAILED);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public void start() throws Exception {
        setStatus(ServerStatus.STARTING);
        // Get AiravataServiceHandler from Spring context
        AiravataServiceHandler handler = applicationContext.getBean(AiravataServiceHandler.class);
        // TODO: Migrate SecurityModule to Spring AOP for security interception
        // For now, we use the handler directly. Security checks are still applied via @SecurityCheck annotations
        Airavata.Processor<Airavata.Iface> airavataAPIServer = new Airavata.Processor<Airavata.Iface>(handler);
        startAiravataServer(airavataAPIServer);
    }

    @Override
    public void stop() throws Exception {
        if ((!properties.security.tls.enabled) && server != null && server.isServing()) {
            setStatus(ServerStatus.STOPING);
            server.stop();
        }
        // stop the Airavata API server hosted over TLS.
        if ((properties.security.tls.enabled) && TLSServer != null && TLSServer.isServing()) {
            TLSServer.stop();
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

    private void setStatus(ServerStatus stat) {
        status = stat;
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

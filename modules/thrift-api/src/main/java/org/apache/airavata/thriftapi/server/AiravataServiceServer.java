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

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.thriftapi.handler.AiravataServiceHandler;
import org.apache.airavata.thriftapi.service.Airavata;
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
public class AiravataServiceServer extends ServerLifecycle {

    private static final String SERVER_NAME = "Airavata API Server";
    private static final String SERVER_VERSION = "1.0";

    private TServer server, TLSServer;

    private final AiravataServerProperties properties;
    private final AiravataServiceHandler handler;

    public AiravataServiceServer(AiravataServerProperties properties, AiravataServiceHandler handler) {
        this.properties = properties;
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
        // API Server starts after Registry, Credential, and Sharing
        return 50;
    }

    @Override
    public boolean isRunning() {
        if (!properties.security.tls.enabled) {
            return server != null && server.isServing();
        } else {
            return TLSServer != null && TLSServer.isServing();
        }
    }

    public void startAiravataServer(Airavata.Processor<Airavata.Iface> airavataAPIServer)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException {
        try {
            final int serverPort = properties.services.api.port;

            if (!properties.security.tls.enabled) {
                TServerTransport serverTransport = new TServerSocket(serverPort);
                TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
                options.minWorkerThreads = properties.services.api.minThreads;
                server = new TThreadPoolServer(options.processor(airavataAPIServer));
                new Thread(() -> {
                            server.serve();
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
                                logger.info("Airavata API Server over TLS started on Port " + serverPort);
                            }
                        })
                        .start();
                logger.info("API server started over TLS on Port: " + serverPort + " ...");
            }

        } catch (TTransportException e) {
            logger.error("Failed to start API server ...", e);
            throw new RuntimeException("Failed to start API server: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        // TODO: Migrate SecurityModule to Spring AOP for security interception
        // For now, we use the handler directly. Security checks are still applied via
        // @SecurityCheck annotations
        Airavata.Processor<Airavata.Iface> airavataAPIServer = new Airavata.Processor<Airavata.Iface>(handler);
        startAiravataServer(airavataAPIServer);
    }

    @Override
    protected void doStop() throws Exception {
        if ((!properties.security.tls.enabled) && server != null && server.isServing()) {
            server.stop();
        }
        // stop the Airavata API server hosted over TLS.
        if ((properties.security.tls.enabled) && TLSServer != null && TLSServer.isServing()) {
            TLSServer.stop();
        }
    }
}

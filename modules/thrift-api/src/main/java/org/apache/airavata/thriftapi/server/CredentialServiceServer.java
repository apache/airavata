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
import org.apache.airavata.thriftapi.credential.model.CredentialStoreService;
import org.apache.airavata.thriftapi.handler.CredentialServiceHandler;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "services.thrift.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CredentialServiceServer extends ServerLifecycle {
    private static final String SERVER_NAME = "Credential Store Server";
    private static final String SERVER_VERSION = "1.0";

    private TServer server;

    private final ApplicationContext applicationContext;
    private final AiravataServerProperties properties;

    public CredentialServiceServer(ApplicationContext applicationContext, AiravataServerProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
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
        // Credential Store starts after Registry
        return 30;
    }

    @Override
    public boolean isRunning() {
        return server != null && server.isServing();
    }

    @Override
    protected void doStart() throws Exception {
        try {
            final int serverPort = properties.services.vault.server.port;
            CredentialServiceHandler handler = applicationContext.getBean(CredentialServiceHandler.class);
            CredentialStoreService.Processor<CredentialServiceHandler> processor =
                    new CredentialStoreService.Processor<>(handler);

            TServerTransport serverTransport = new TServerSocket(serverPort);
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = 30;
            server = new TThreadPoolServer(options.processor(processor));

            new Thread() {
                public void run() {
                    server.serve();
                    logger.info("Credential store Server Stopped.");
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
                        logger.info("Starting Credential store Server on Port " + serverPort);
                        logger.info("Listening to Credential store clients ....");
                    }
                }
            }.start();
        } catch (TTransportException e) {
            throw new Exception("Error while starting the credential store service", e);
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
}

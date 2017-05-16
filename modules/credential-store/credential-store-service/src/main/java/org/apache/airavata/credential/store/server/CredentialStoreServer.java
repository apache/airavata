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
package org.apache.airavata.credential.store.server;


import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class CredentialStoreServer  implements IServer {
    private final static Logger logger = LoggerFactory.getLogger(CredentialStoreServer.class);
    private static final String SERVER_NAME = "Credential Store Server";
    private static final String SERVER_VERSION = "1.0";

    private IServer.ServerStatus status;
    private TServer server;

    public CredentialStoreServer() {
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
            setStatus(ServerStatus.STARTING);
            final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
            final String serverHost = ServerSettings.getCredentialStoreServerHost();
            CredentialStoreService.Processor processor = new CredentialStoreService.Processor(new CredentialStoreServerHandler());

            TServerTransport serverTransport;

            if (serverHost == null) {
                serverTransport = new TServerSocket(serverPort);
            } else {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
            }
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = 30;
            server = new TThreadPoolServer(options.processor(processor));

            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(ServerStatus.STOPPED);
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
                        setStatus(ServerStatus.STARTED);
                        logger.info("Starting Credential store Server on Port " + serverPort);
                        logger.info("Listening to Credential store clients ....");
                    }
                }
            }.start();
        } catch (TTransportException e) {
            setStatus(ServerStatus.FAILED);
            throw new Exception("Error while starting the credential store service", e);
        }
    }

    public static void main(String[] args) {
        try {
            new CredentialStoreServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
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

    }

    @Override
    public ServerStatus getStatus() throws Exception {
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


}

/*
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
 *
*/
package org.apache.airavata.data.manager.server;

import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.data.manager.cpi.DataManagerService;
import org.apache.airavata.data.manager.util.Constants;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class DataManagerServer implements IServer {

    private final static Logger logger = LoggerFactory.getLogger(DataManagerServer.class);
    private static final String SERVER_NAME = "Data Manager Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

    private TServer server;

    public DataManagerServer() {
        setStatus(ServerStatus.STOPPED);
    }

    public void StartDataManagerServer(DataManagerService.Processor<DataManagerServerHandler> orchestratorServerHandlerProcessor)
            throws Exception {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.DATA_MANAGER_SERVER_PORT, "8990"));

            final String serverHost = ServerSettings.getSetting(Constants.DATA_MANAGER_SERVER_HOST, "localhost");

            TServerTransport serverTransport;

            if(serverHost == null){
                serverTransport = new TServerSocket(serverPort);
            }else{
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
            }

            //server = new TSimpleServer(
            //      new TServer.Args(serverTransport).processor(orchestratorServerHandlerProcessor));
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = Integer.parseInt(ServerSettings.getSetting(Constants.DATA_MANAGER_SERVER_MIN_THREADS, "30"));
            server = new TThreadPoolServer(options.processor(orchestratorServerHandlerProcessor));

            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(ServerStatus.STOPPED);
                    logger.info("Data Manager Server Stopped.");
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
                        logger.info("Starting Data Manager Server on Port " + serverPort);
                        logger.info("Listening to Data Manager Clients ....");
                    }
                }
            }.start();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
        }
    }

    public static void main(String[] args) {
        try {
            new DataManagerServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void start() throws Exception {
        setStatus(ServerStatus.STARTING);
        DataManagerService.Processor<DataManagerServerHandler> orchestratorService =
                new DataManagerService.Processor<>(new DataManagerServerHandler());
        StartDataManagerServer(orchestratorService);
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
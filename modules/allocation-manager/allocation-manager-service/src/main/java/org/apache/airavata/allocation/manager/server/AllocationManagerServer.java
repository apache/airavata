package org.apache.airavata.allocation.manager.server;

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

import org.apache.airavata.allocation.manager.cpi.AllocationManagerService;
import org.apache.airavata.allocation.manager.handler.AllocationManagerServerHandler;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class AllocationManagerServer {

    private final static Logger logger = LoggerFactory.getLogger(AllocationManagerServer.class);
    private static final String SERVER_NAME = "Resource Management Server";
    private static final String SERVER_VERSION = "1.0";


    public String getName() {
        return SERVER_NAME;
    }


    public String getVersion() {
        return SERVER_VERSION;
    }

    public static AllocationManagerServerHandler handler;

    public static AllocationManagerService.Processor processor;

    private TServer server;

    public void start() throws Exception {
        try {

            final int serverPort = 9090;
            final String serverHost = "localhost";
            AllocationManagerService.Processor processor = new AllocationManagerService.Processor(handler);

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
                    logger.info("Allocation Manager Server Stopped.");
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
                        logger.info("Starting Allocation Manager Server on Port " + serverPort);
                        logger.info("Listening to Resource Management clients ....");
                    }
                }
            }.start();
        } catch (TTransportException e) {
            throw new Exception("Error while starting the Alocation Manager service", e);
        }
    }



    public static void main(String [] args) {
        try {
            new AllocationManagerServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}

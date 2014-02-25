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

package org.apache.airavata.orchestrator.server;

import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServer {

    private final static Logger logger = LoggerFactory.getLogger(OrchestratorServer.class);

    //FIXME: Read the port from airavata-server.config file
    private static final int ORCHESTRATOT_SERVER_PORT = 8940;

    public static final String TESTARGUMENTTOHANDLER = "testing";


    public static void StartOrchestratorServer(OrchestratorService.Processor<OrchestratorServerHandler> orchestratorServerHandlerProcessor)
            throws Exception {
        try {
            TServerTransport serverTransport = new TServerSocket(ORCHESTRATOT_SERVER_PORT);
            TServer server = new TSimpleServer(
                    new TServer.Args(serverTransport).processor(orchestratorServerHandlerProcessor));
            logger.info("Starting Orchestrator Server on Port " + ORCHESTRATOT_SERVER_PORT);
            logger.info("Listening to Orchestrator Clients ....");
            server.serve();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
        }
    }

    public static void main(String[] args) {
        OrchestratorService.Processor<OrchestratorServerHandler> orchestratorServerHandlerProcessor =
                new OrchestratorService.Processor<OrchestratorServerHandler>(new OrchestratorServerHandler());
        try {
            StartOrchestratorServer(orchestratorServerHandlerProcessor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

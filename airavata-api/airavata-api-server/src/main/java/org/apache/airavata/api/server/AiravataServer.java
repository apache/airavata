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

package org.apache.airavata.api.server;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.error.AiravataErrorType;
import org.apache.airavata.api.error.AiravataSystemException;
import org.apache.airavata.api.server.handler.AiravataServerHandler;
import org.apache.airavata.api.server.util.RegistryInitUtil;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataServer {

    private final static Logger logger = LoggerFactory.getLogger(AiravataServer.class);

    //FIXME: Read the port from airavata-server.config file
    private static final int THRIFT_SERVER_PORT = 8930;


    public static void StartAiravataServer(Airavata.Processor<AiravataServerHandler> mockAiravataServer) throws AiravataSystemException {
        try {
            RegistryInitUtil.initializeDB();
            TServerTransport serverTransport = new TServerSocket(THRIFT_SERVER_PORT);
            TServer server = new TSimpleServer(
                    new TServer.Args(serverTransport).processor(mockAiravataServer));
            logger.info("Starting Airavata Mock Airavata Server on Port " + THRIFT_SERVER_PORT);
            logger.info("Listening to Airavata Clients ....");
            server.serve();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    public static void main(String[] args) {
        Airavata.Processor<AiravataServerHandler> mockAiravataServer =
                new Airavata.Processor<AiravataServerHandler>(new AiravataServerHandler());
        try {
            StartAiravataServer(mockAiravataServer);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        }
    }

}

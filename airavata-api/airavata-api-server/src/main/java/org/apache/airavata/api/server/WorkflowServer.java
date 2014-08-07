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

import java.net.InetSocketAddress;

import org.apache.airavata.api.server.handler.WorkflowServerHandler;
import org.apache.airavata.api.server.util.AppCatalogInitUtil;
import org.apache.airavata.api.server.util.Constants;
import org.apache.airavata.api.workflow.Workflow;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowServer implements IServer{

    private final static Logger logger = LoggerFactory.getLogger(WorkflowServer.class);
	private static final String SERVER_NAME = "Airavata Workflow API Server";
	private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

	private TServer server;

	public WorkflowServer() {
		setStatus(ServerStatus.STOPPED);
	}
	
    public void StartAiravataServer(Workflow.Processor<Workflow.Iface> appCatalogServerHandler) throws AiravataSystemException {
        try {
            AiravataUtils.setExecutionAsServer();
            AppCatalogInitUtil.initializeDB();
            final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.WORKFLOW_SERVER_PORT,"8931"));
            final String serverHost = ServerSettings.getSetting(Constants.WORKFLOW_SERVER_HOST, null);
            
			TServerTransport serverTransport;
			
			if(serverHost == null){
				serverTransport = new TServerSocket(serverPort);
			}else{
				InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
				serverTransport = new TServerSocket(inetSocketAddress);
			}
			
			server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(appCatalogServerHandler));
            new Thread() {
				public void run() {
					server.serve();
					setStatus(ServerStatus.STOPPED);
					logger.info("Airavata Workflow Server Stopped.");
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
			            logger.info("Starting Airavata Workflow Server on Port " + serverPort);
			            logger.info("Listening to Workflow API Clients ....");
					}
				}
			}.start();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    public static void main(String[] args) {
    	try {
			WorkflowServer server = new WorkflowServer();
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	@Override
	public void start() throws Exception {
		setStatus(ServerStatus.STARTING);
		Workflow.Processor<Workflow.Iface> server =
                new Workflow.Processor<Workflow.Iface>(new WorkflowServerHandler());
    	StartAiravataServer(server);
	}

	@Override
	public void stop() throws Exception {
		if (server.isServing()){
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


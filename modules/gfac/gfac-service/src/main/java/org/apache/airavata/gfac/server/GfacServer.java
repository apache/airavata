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
package org.apache.airavata.gfac.server;

import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class GfacServer implements IServer{

    private final static Logger logger = LoggerFactory.getLogger(GfacServer.class);
	private static final String SERVER_NAME = "Gfac Server";
	private static final String SERVER_VERSION = "1.0";

    private IServer.ServerStatus status;

	private TServer server;

	public GfacServer() {
		setStatus(IServer.ServerStatus.STOPPED);
	}

    public void StartGfacServer(GfacService.Processor<GfacServerHandler> gfacServerHandlerProcessor)
            throws Exception {
		final int serverPort = Integer.parseInt(ServerSettings.getGFacServerPort());
		try {
            final String serverHost = ServerSettings.getGfacServerHost();

            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);

			TServerTransport serverTransport = new TServerSocket(inetSocketAddress);

            server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(gfacServerHandlerProcessor));

            new Thread() {
				public void run() {
					server.serve();
					setStatus(ServerStatus.STARTING);
					logger.info("Starting Gfac Server ...");
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
						setStatus(IServer.ServerStatus.STARTED);
			            logger.info("Started Gfac Server on Port " + serverPort + " ...");
					}
				}
			}.start();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(IServer.ServerStatus.FAILED);
			logger.error("Failed to start Gfac server on port " + serverPort + " ...");
        }
    }

    public static void main(String[] args) {
    	try {
			new GfacServer().start();
		} catch (Exception e) {
            logger.error(e.getMessage(), e);
		}
    }

	public void start() throws Exception {
		setStatus(IServer.ServerStatus.STARTING);
        GfacService.Processor<GfacServerHandler> gfacService =
                new GfacService.Processor<GfacServerHandler>(new GfacServerHandler());
		StartGfacServer(gfacService);
	}

	public void stop() throws Exception {
        if (server!=null && server.isServing()){
			setStatus(IServer.ServerStatus.STOPING);
			server.stop();
		}
		GFacThreadPoolExecutor.getCachedThreadPool().shutdownNow();

	}

	public void restart() throws Exception {
		stop();
		start();
	}

	public void configure() throws Exception {
		// TODO Auto-generated method stub

	}

	public IServer.ServerStatus getStatus() throws Exception {
		return status;
	}

	private void setStatus(IServer.ServerStatus stat){
		status=stat;
		status.updateTime();
	}

	public String getName() {
		return SERVER_NAME;
	}

	public String getVersion() {
		return SERVER_VERSION;
	}
}

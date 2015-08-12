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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.server.handler.AiravataServerHandler;
import org.apache.airavata.api.server.util.AppCatalogInitUtil;
import org.apache.airavata.api.server.util.Constants;
import org.apache.airavata.api.server.util.RegistryInitUtil;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataAPIServer implements IServer{

    private final static Logger logger = LoggerFactory.getLogger(AiravataAPIServer.class);
	private static final String SERVER_NAME = "Airavata API Server";
	private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

	private TServer server;

	public AiravataAPIServer() {
		setStatus(ServerStatus.STOPPED);
	}
	
    public void startAiravataServer(Airavata.Processor<Airavata.Iface> airavataAPIServer) throws AiravataSystemException {
        try {
            RegistryInitUtil.initializeDB();
            AppCatalogInitUtil.initializeDB();
            final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.API_SERVER_PORT,"8930"));
            final String serverHost = ServerSettings.getSetting(Constants.API_SERVER_HOST, null);

			TServerTransport serverTransport;

            if(ServerSettings.isAPIServerTLSEnabled()) {
                logger.info("Starting API Server with TLS Security..");

                String keystore = ServerSettings.getApiServerKeystore();
                String keystorePWD = ServerSettings.getApiServerKeystorePasswd();
                TSSLTransportFactory.TSSLTransportParameters tlsParams =
                        new TSSLTransportFactory.TSSLTransportParameters();
                tlsParams.setKeyStore(keystore, keystorePWD);
                serverTransport = TSSLTransportFactory.getServerSocket(serverPort, 10000,
                        InetAddress.getByName(serverHost), tlsParams);
            } else {
                if(serverHost == null){
                    serverTransport = new TServerSocket(serverPort);
                }else{
                    InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                    serverTransport = new TServerSocket(inetSocketAddress);
                }
            }
			
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = Integer.parseInt(ServerSettings.getSetting(Constants.API_SERVER_MIN_THREADS, "50"));
			server = new TThreadPoolServer(options.processor(airavataAPIServer));
            new Thread() {
				public void run() {
					server.serve();
					RegistryInitUtil.stopDerbyInServerMode();
					setStatus(ServerStatus.STOPPED);
					logger.info("Airavata API Server Stopped.");
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
			            logger.info("Starting Airavata API Server on Port " + serverPort);
			            logger.info("Listening to Airavata Clients ....");
					}
				}
			}.start();
//            storeServerConfig();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            RegistryInitUtil.stopDerbyInServerMode();
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (ApplicationSettingsException e) {
            logger.error("Error fetching properties", e);
        } catch (UnknownHostException e) {
            logger.error("API Server Host error", e);
        }
    }
    public static void main(String[] args) {
    	try {
			AiravataAPIServer server = new AiravataAPIServer();
			server.start();
		} catch (Exception e) {
			logger.error("Error while initializing Airavata API server", e);
		}
    }

	@Override
	public void start() throws Exception {
		setStatus(ServerStatus.STARTING);
		Airavata.Processor<Airavata.Iface> airavataAPIServer =
                new Airavata.Processor<Airavata.Iface>(new AiravataServerHandler());
    	startAiravataServer(airavataAPIServer);
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

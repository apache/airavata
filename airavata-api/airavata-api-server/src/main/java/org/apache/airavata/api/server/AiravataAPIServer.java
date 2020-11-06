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
package org.apache.airavata.api.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.server.handler.AiravataServerHandler;
import org.apache.airavata.api.server.util.Constants;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.service.security.AiravataSecurityManager;
import org.apache.airavata.service.security.SecurityManagerFactory;
import org.apache.airavata.service.security.interceptor.SecurityModule;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class AiravataAPIServer implements IServer{

    private final static Logger logger = LoggerFactory.getLogger(AiravataAPIServer.class);
	private static final String SERVER_NAME = "Airavata API Server";
	private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

	private TServer server, TLSServer;

	public AiravataAPIServer() {
		setStatus(ServerStatus.STOPPED);
	}
	
    public void startAiravataServer(Airavata.Processor<Airavata.Iface> airavataAPIServer) throws AiravataSystemException {
        try {
            final String serverHost = ServerSettings.getSetting(Constants.API_SERVER_HOST, null);
            if (!ServerSettings.isTLSEnabled()) {
                final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.API_SERVER_PORT, "8930"));

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
						setStatus(ServerStatus.STOPPED);
						logger.info("Airavata API Server Stopped.");
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
							logger.info("Starting Airavata API Server on Port " + serverPort);
							logger.info("Listening to Airavata Clients ....");
						}
					}
				}.start();
				logger.info("Started API Server ....");
			} else { /**********start thrift server over TLS******************/
                TSSLTransportFactory.TSSLTransportParameters TLSParams =
                        new TSSLTransportFactory.TSSLTransportParameters();
                TLSParams.setKeyStore(ServerSettings.getKeyStorePath(), ServerSettings.getKeyStorePassword());
                TServerSocket TLSServerTransport = TSSLTransportFactory.getServerSocket(
                        ServerSettings.getTLSServerPort(), ServerSettings.getTLSClientTimeout(),
                        InetAddress.getByName(serverHost), TLSParams);
                TThreadPoolServer.Args settings = new TThreadPoolServer.Args(TLSServerTransport);
                settings.minWorkerThreads = Integer.parseInt(ServerSettings.getSetting(
                        Constants.API_SERVER_MIN_THREADS, "50"));
                TLSServer = new TThreadPoolServer(settings.processor(airavataAPIServer));
                new Thread() {
                    public void run() {
                        TLSServer.serve();
                        setStatus(ServerStatus.STOPPED);
                        logger.info("Airavata API Server over TLS Stopped.");
                    }
                }.start();
                new Thread() {
                    public void run() {
                        while (!TLSServer.isServing()) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (TLSServer.isServing()) {
                            setStatus(ServerStatus.STARTED);
                        }
                    }
                }.start();
                logger.info("API server started over TLS on Port: " + ServerSettings.getTLSServerPort() + " ...");
            }

            /*perform any security related initialization at the server startup, according to the underlying security
             manager implementation being used.*/
			AiravataSecurityManager securityManager = SecurityManagerFactory.getSecurityManager();
			securityManager.initializeSecurityInfra();

        } catch (TTransportException e) {
            logger.error(e.getMessage(), e);
            setStatus(ServerStatus.FAILED);
			logger.error("Failed to start API server ...", e);
			throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (AiravataSecurityException e) {
            logger.error(e.getMessage(), e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
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
        //Obtain a AiravataServerHandl
		// er object from Guice which is wrapped with interception logic.
        Injector injector = Guice.createInjector(new SecurityModule());
        Airavata.Processor<Airavata.Iface> airavataAPIServer =
                new Airavata.Processor<Airavata.Iface>(injector.getInstance(AiravataServerHandler.class));
    	startAiravataServer(airavataAPIServer);
	}

	@Override
	public void stop() throws Exception {
		if ((!ServerSettings.isTLSEnabled()) && server.isServing()){
			setStatus(ServerStatus.STOPING);
			server.stop();
		}
        //stop the Airavata API server hosted over TLS.
        if ((ServerSettings.isTLSEnabled()) && TLSServer.isServing()){
            TLSServer.stop();
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

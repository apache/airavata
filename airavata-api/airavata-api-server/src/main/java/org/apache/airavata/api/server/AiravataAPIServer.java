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
import java.net.InetSocketAddress;
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
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataAPIServer implements IServer, Watcher{

    private final static Logger logger = LoggerFactory.getLogger(AiravataAPIServer.class);
	private static final String SERVER_NAME = "Airavata API Server";
	private static final String SERVER_VERSION = "1.0";
    private ZooKeeper zk;
    private static Integer mutex = -1;

    private ServerStatus status;

	private TServer server;

	public AiravataAPIServer() {
		setStatus(ServerStatus.STOPPED);
	}
	
    public void startAiravataServer(Airavata.Processor<Airavata.Iface> airavataAPIServer) throws AiravataSystemException {
        try {
            AiravataUtils.setExecutionAsServer();
            RegistryInitUtil.initializeDB();
            AppCatalogInitUtil.initializeDB();
            final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.API_SERVER_PORT,"8930"));
            final String serverHost = ServerSettings.getSetting(Constants.API_SERVER_HOST, null);
            
			TServerTransport serverTransport;
			
			if(serverHost == null){
				serverTransport = new TServerSocket(serverPort);
			}else{
				InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
				serverTransport = new TServerSocket(inetSocketAddress);
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
            storeServerConfig();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            RegistryInitUtil.stopDerbyInServerMode();
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    public void storeServerConfig() throws AiravataSystemException{
        try {
            String zkhostPort = AiravataZKUtils.getZKhostPort();
            String airavataServerHostPort = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.API_SERVER_HOST)
                    + ":" + ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.API_SERVER_PORT);
            String experimentCatalogJDBCURL = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.REGISTRY_JDBC_URL);
            String appCatalogJDBCURL = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.APPCATALOG_JDBC_URL);
            String rabbitMqBrokerURL = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.RABBITMQ_BROKER_URL);
            String rabbitMqExchange = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.RABBITMQ_EXCHANGE);
            String rabbitMq = rabbitMqBrokerURL + File.separator + rabbitMqExchange;
            zk = new ZooKeeper(zkhostPort, 6000, this);   // no watcher is required, this will only use to store some data
            String apiServer = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_API_SERVER_NODE, "/airavata-server");
            String OrchServer = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_ORCHESTRATOR_SERVER_NODE, "/orchestrator-server");
            String gfacServer = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server");
            String gfacExperiments = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            String experimentCatalog = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_EXPERIMENT_CATALOG, "/experiment-catalog");
            String appCatalog = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_APPCATALOG, "/app-catalog");
            String rabbitMQ = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_RABBITMQ, "/rabbitMq");
            Stat zkStat = zk.exists(experimentCatalog, false);
            if (zkStat == null) {
                zk.create(experimentCatalog, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
            String exCatalogInstantNode = experimentCatalog + File.separator + String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            zkStat = zk.exists(exCatalogInstantNode, false);
            if (zkStat == null) {
                zk.create(exCatalogInstantNode,
                        experimentCatalogJDBCURL.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
                logger.info("Successfully created experiment-catalog node");
            }
            zkStat = zk.exists(appCatalog, false);
            if (zkStat == null) {
                zk.create(appCatalog, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
            String appCatalogInstantNode = appCatalog + File.separator + String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            zkStat = zk.exists(appCatalogInstantNode, false);
            if (zkStat == null) {
                zk.create(appCatalogInstantNode,
                        appCatalogJDBCURL.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
                logger.info("Successfully created app-catalog node");
            }
            if (getStatus().equals(ServerStatus.STARTED)) {
                zkStat = zk.exists(apiServer, false);
                if (zkStat == null) {
                    zk.create(apiServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
                String instantNode = apiServer + File.separator + String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
                zkStat = zk.exists(instantNode, false);
                if (zkStat == null) {
                    zk.create(instantNode,
                            airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
                    logger.info("Successfully created airavata-server node");
                }

                zkStat = zk.exists(OrchServer, false);
                if (zkStat == null) {
                    zk.create(OrchServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                    logger.info("Successfully created orchestrator-server node");
                }
                zkStat = zk.exists(gfacServer, false);
                if (zkStat == null) {
                    zk.create(gfacServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                    logger.info("Successfully created gfac-server node");
                }
                zkStat = zk.exists(gfacServer, false);
                if (zkStat == null) {
                    zk.create(gfacExperiments, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                    logger.info("Successfully created gfac-server node");
                }
                zkStat = zk.exists(rabbitMQ, false);
                if (zkStat == null) {
                    zk.create(rabbitMQ, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
                if (ServerSettings.isRabbitMqPublishEnabled()) {
                    String rabbitMqInstantNode = rabbitMQ + File.separator + String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
                    zkStat = zk.exists(rabbitMqInstantNode, false);
                    if (zkStat == null) {
                        zk.create(rabbitMqInstantNode,
                                rabbitMq.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
                        logger.info("Successfully created rabbitMQ node");
                    }
                }
                logger.info("Finished starting ZK: " + zk);
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            RegistryInitUtil.stopDerbyInServerMode();
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (IOException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            RegistryInitUtil.stopDerbyInServerMode();
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            RegistryInitUtil.stopDerbyInServerMode();
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (KeeperException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            RegistryInitUtil.stopDerbyInServerMode();
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (Exception e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            RegistryInitUtil.stopDerbyInServerMode();
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

    @Override
    synchronized public void process(WatchedEvent watchedEvent) {
        synchronized (mutex) {
            Event.KeeperState state = watchedEvent.getState();
            logger.info(state.name());
            switch(state){
                case SyncConnected:
                    mutex.notify();
                case Expired:case Disconnected:
                    try {
                        mutex = -1;
                        zk = new ZooKeeper(AiravataZKUtils.getZKhostPort(), 6000, this);
                        synchronized (mutex) {
                            mutex.wait();  // waiting for the syncConnected event
                        }
                        storeServerConfig();
                    } catch (IOException e) {
                        logger.error("Error while synchronizing with zookeeper", e);
                    } catch (ApplicationSettingsException e) {
                        logger.error("Error while synchronizing with zookeeper", e);
                    } catch (InterruptedException e) {
                        logger.error("Error while synchronizing with zookeeper", e);
                    } catch (AiravataSystemException e) {
                        logger.error("Error while synchronizing with zookeeper", e);
                    }
            }
        }
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.orchestrator.server;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.compute.resource.monitoring.ComputationalResourceMonitoringService;
import org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ProcessReschedulingService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.util.Constants;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class OrchestratorServer implements IServer {

    private final static Logger logger = LoggerFactory.getLogger(OrchestratorServer.class);
    private static final String SERVER_NAME = "Orchestrator Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

    private TServer server;

    private static ComputationalResourceMonitoringService monitoringService;

    private static ProcessReschedulingService metaschedulerService;

//	private ClusterStatusMonitorJobScheduler clusterStatusMonitorJobScheduler;

    public OrchestratorServer() {
        setStatus(ServerStatus.STOPPED);
    }

    public void StartOrchestratorServer(OrchestratorService.Processor<OrchestratorServerHandler> orchestratorServerHandlerProcessor)
            throws Exception {
        final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.ORCHESTRATOT_SERVER_PORT, "8940"));
        try {
            final String serverHost = ServerSettings.getSetting(Constants.ORCHESTRATOT_SERVER_HOST, null);
            TServerTransport serverTransport;
            if (serverHost == null) {
                serverTransport = new TServerSocket(serverPort);
            } else {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
            }
            //server = new TSimpleServer(
            //      new TServer.Args(serverTransport).processor(orchestratorServerHandlerProcessor));
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = Integer.parseInt(ServerSettings.getSetting(Constants.ORCHESTRATOT_SERVER_MIN_THREADS, "30"));
            server = new TThreadPoolServer(options.processor(orchestratorServerHandlerProcessor));
            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(ServerStatus.STARTING);
                    logger.info("Starting Orchestrator Server ... ");
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
                        logger.info("Started Orchestrator Server on Port " + serverPort + " ...");


                    }
                }
            }.start();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            logger.error("Failed to start Orchestrator server on port " + serverPort + " ...");
        }
    }

    public void startClusterStatusMonitoring() throws SchedulerException, ApplicationSettingsException {
//        clusterStatusMonitorJobScheduler = new ClusterStatusMonitorJobScheduler();
//        clusterStatusMonitorJobScheduler.scheduleClusterStatusMonitoring();

        try {
            if (monitoringService == null) {
                monitoringService = new ComputationalResourceMonitoringService();
                monitoringService.setServerStatus(ServerStatus.STARTING);
            }
            if (monitoringService != null && !monitoringService.getStatus().equals(ServerStatus.STARTED)) {
                monitoringService.start();
                monitoringService.setServerStatus(ServerStatus.STARTED);
                logger.info("Airavata compute resource monitoring service started ....");
            }
        } catch (Exception ex) {
            logger.error("Airavata compute resource monitoring service failed ....",ex);
        }
    }

    public void startMetaschedulerJobScanning() throws SchedulerException, ApplicationSettingsException {
        try {
            if (metaschedulerService == null) {
                metaschedulerService = new ProcessReschedulingService();
                metaschedulerService.setServerStatus(ServerStatus.STARTING);
            }
            if (metaschedulerService != null && !metaschedulerService.getStatus().equals(ServerStatus.STARTED)) {
                metaschedulerService.start();
                metaschedulerService.setServerStatus(ServerStatus.STARTED);
                logger.info("Airavata metascheduler job scanning service started ....");
            }
        } catch (Exception ex) {
            logger.error("Airavata metascheduler job scanning service failed ....",ex);
        }
    }

    public static void main(String[] args) {
        try {
            new OrchestratorServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void start() throws Exception {
        if (ServerSettings.enableClusterStatusMonitoring()) {
            //starting cluster status monitoring
            startClusterStatusMonitoring();
        }

        if (ServerSettings.enableMetaschedulerJobScanning()) {
            //starting cluster status monitoring
            startMetaschedulerJobScanning();
        }


        setStatus(ServerStatus.STARTING);
        OrchestratorService.Processor<OrchestratorServerHandler> orchestratorService =
                new OrchestratorService.Processor<OrchestratorServerHandler>(new OrchestratorServerHandler());
        StartOrchestratorServer(orchestratorService);
    }

    @Override
    public void stop() throws Exception {
        if (server != null && server.isServing()) {
            setStatus(ServerStatus.STOPING);
            server.stop();
        }
        if (monitoringService != null) {
            monitoringService.stop();
            monitoringService.setServerStatus(ServerStatus.STOPPED);
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

    private void setStatus(ServerStatus stat) {
        status = stat;
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

/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.api.thrift.server;

import java.net.InetSocketAddress;
import org.apache.airavata.api.thrift.handler.OrchestratorServiceHandler;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.metascheduler.metadata.analyzer.DataInterpreterService;
import org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ProcessReschedulingService;
import org.apache.airavata.monitor.compute.ComputationalResourceMonitoringService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorServiceServer implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceServer.class);
    private static final String SERVER_NAME = "Orchestrator Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

    private TServer server;

    private static ComputationalResourceMonitoringService monitoringService;

    private static ProcessReschedulingService metaschedulerService;

    private static DataInterpreterService dataInterpreterService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiravataServerProperties properties;

    //	private ClusterStatusMonitorJobScheduler clusterStatusMonitorJobScheduler;

    public OrchestratorServiceServer() {
        setStatus(ServerStatus.STOPPED);
    }

    public void StartOrchestratorServer(
            OrchestratorService.Processor<OrchestratorServiceHandler> orchestratorServerHandlerProcessor)
            throws Exception {
        final int serverPort = properties.getOrchestrator().getServerPort();
        try {
            final String serverHost = properties.getOrchestrator().getServerHost();
            TServerTransport serverTransport;
            if (serverHost == null || serverHost.isEmpty()) {
                serverTransport = new TServerSocket(serverPort);
            } else {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
            }
            // server = new TSimpleServer(
            //      new TServer.Args(serverTransport).processor(orchestratorServerHandlerProcessor));
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = properties.getOrchestrator().getServerMinThreads();
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

    public void startClusterStatusMonitoring() throws SchedulerException {
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
            logger.error("Airavata compute resource monitoring service failed ....", ex);
        }
    }

    public void startMetaschedulerJobScanning() throws SchedulerException {
        try {
            if (metaschedulerService == null) {
                metaschedulerService = new ProcessReschedulingService();
                metaschedulerService.setServerStatus(ServerStatus.STARTING);
            }
            if (metaschedulerService != null
                    && !metaschedulerService.getStatus().equals(ServerStatus.STARTED)) {
                metaschedulerService.start();
                metaschedulerService.setServerStatus(ServerStatus.STARTED);
                logger.info("Airavata metascheduler job scanning service started ....");
            }
        } catch (Exception ex) {
            logger.error("Airavata metascheduler job scanning service failed ....", ex);
        }
    }

    public void startMetadataDataAnalyzer() throws SchedulerException {
        try {
            if (dataInterpreterService == null) {
                dataInterpreterService = new DataInterpreterService();
                dataInterpreterService.setServerStatus(ServerStatus.STARTING);
            }
            if (dataInterpreterService != null
                    && !dataInterpreterService.getStatus().equals(ServerStatus.STARTED)) {
                dataInterpreterService.start();
                dataInterpreterService.setServerStatus(ServerStatus.STARTED);
                logger.info("Airavata data interpreter job scanning service started ....");
            }
        } catch (Exception ex) {
            logger.error("Airavata data interpreter job scanning service failed ....", ex);
        }
    }

    @Override
    public void start() throws Exception {
        if (properties.getMonitoring().getClusterStatusMonitoring().isEnable()) {
            // starting cluster status monitoring
            startClusterStatusMonitoring();
        }

        if (properties.getMetascheduler().isJobScanningEnable()) {
            // starting cluster status monitoring
            startMetaschedulerJobScanning();
        }

        if (properties.getDataAnalyzer().isJobScanningEnable()) {
            // starting metadata analyzer
            startMetadataDataAnalyzer();
        }

        setStatus(ServerStatus.STARTING);
        OrchestratorServiceHandler handler = applicationContext.getBean(OrchestratorServiceHandler.class);
        OrchestratorService.Processor<OrchestratorServiceHandler> orchestratorService =
                new OrchestratorService.Processor<OrchestratorServiceHandler>(handler);
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

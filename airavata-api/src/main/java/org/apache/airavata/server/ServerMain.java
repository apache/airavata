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
package org.apache.airavata.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.api.server.AiravataAPIServer;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.*;
import org.apache.airavata.common.utils.ApplicationSettings.ShutdownStrategy;
import org.apache.airavata.common.utils.IServer.ServerStatus;
import org.apache.airavata.credential.store.server.CredentialStoreServer;
import org.apache.airavata.db.event.manager.DBEventManagerRunner;
import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.airavata.monitor.email.EmailBasedMonitor;
import org.apache.airavata.monitor.realtime.RealtimeMonitor;
import org.apache.airavata.orchestrator.server.OrchestratorServer;
import org.apache.airavata.patform.monitoring.MonitoringServer;
import org.apache.airavata.registry.api.service.RegistryAPIServer;
import org.apache.airavata.service.profile.server.ProfileServiceServer;
import org.apache.airavata.sharing.registry.server.SharingRegistryServer;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {
    private static List<IServer> servers;
    private static List<Class<?>> additionalServers;
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
    private static boolean serversLoaded = false;
    private static boolean systemShutDown = false;

    static {
        servers = new ArrayList<>();
        additionalServers = new ArrayList<>();
    }

    private static void loadServers() {
        servers.clear();
        additionalServers.clear();

        servers.addAll(Arrays.asList(
          new DBEventManagerRunner(),
          new RegistryAPIServer(),
          new CredentialStoreServer(),
          new SharingRegistryServer(),
          new AiravataAPIServer(),
          new OrchestratorServer(),
          new ProfileServiceServer()
        ));

        additionalServers.addAll(Arrays.asList(
        HelixController.class, 
          GlobalParticipant.class,
          EmailBasedMonitor.class,
          RealtimeMonitor.class,
          PreWorkflowManager.class,
          PostWorkflowManager.class
        ));
        serversLoaded = true;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                setSystemShutDown();
                stopAllServers();
            }
        });
    }

    public static void main(String[] args) throws IOException, AiravataException, ParseException {
        ServerSettings.mergeSettingsCommandLineArgs(args);
        if (ServerSettings.getBooleanSetting("api.server.monitoring.enabled")) {
            MonitoringServer monitoringServer = new MonitoringServer(
                    ServerSettings.getSetting("api.server.monitoring.host"),
                    ServerSettings.getIntSetting("api.server.monitoring.port"));
            monitoringServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
        }

        logger.info("Airavata server instance starting...");
        startAllServers();

        // Wait until SIGTERM or KeyboardInterrupt (Ctrl+C) triggers shutdown hook.
        try {
            while (!isSystemShutDown()) {
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            logger.info("Interrupted, shutting down servers...");
            setSystemShutDown();
        }

        if (isSystemShutDown()) {
            ServerSettings.setStopAllThreads(true);
            stopAllServers();
            ShutdownStrategy shutdownStrategy;
            try {
                shutdownStrategy = ServerSettings.getShutdownStrategy();
            } catch (Exception e) {
                String strategies = "";
                for (ShutdownStrategy s : ShutdownStrategy.values()) {
                    strategies += "/" + s.toString();
                }
                logger.warn(e.getMessage());
                logger.warn("Valid shutdown options are : " + strategies.substring(1));
                shutdownStrategy = ShutdownStrategy.SELF_TERMINATE;
            }
            if (shutdownStrategy == ShutdownStrategy.SELF_TERMINATE) {
                System.exit(0);
            }
        }
    }

    public static void stopAllServers() {
        // stopping should be done in reverse order of starting the servers
        for (int i = servers.size() - 1; i >= 0; i--) {
            try {
                servers.get(i).stop();
                waitForServerToStop(servers.get(i), null);
            } catch (Exception e) {
                logger.error("Server Stop Error:", e);
            }
        }
    }

    public static void startAllServers() {
        if (!serversLoaded) {
            loadServers();
        }
        for (IServer server : servers) {
            try {
                server.configure();
                server.start();
                waitForServerToStart(server, null);
            } catch (Exception e) {
                logger.error("Server Start Error:", e);
            }
        }
    }

    private static final int SERVER_STATUS_CHANGE_WAIT_INTERVAL = 500;

    private static void waitForServerToStart(IServer server, Integer maxWait) throws Exception {
        int count = 0;
        while (server.getStatus() == ServerStatus.STARTING && (maxWait == null || count < maxWait)) {
            Thread.sleep(SERVER_STATUS_CHANGE_WAIT_INTERVAL);
            count += SERVER_STATUS_CHANGE_WAIT_INTERVAL;
        }
        if (server.getStatus() != ServerStatus.STARTED) {
            logger.error("The " + server.getName() + " did not start!!!");
        }
    }

    private static void waitForServerToStop(IServer server, Integer maxWait) throws Exception {
        int count = 0;
        if (server.getStatus() == ServerStatus.STOPING) {
            logger.info("Waiting for " + server.getName() + " to stop...");
        }
        while (server.getStatus() == ServerStatus.STOPING && (maxWait == null || count < maxWait)) {
            Thread.sleep(SERVER_STATUS_CHANGE_WAIT_INTERVAL);
            count += SERVER_STATUS_CHANGE_WAIT_INTERVAL;
        }
        if (server.getStatus() != ServerStatus.STOPPED) {
            logger.error("Error stopping the " + server.getName() + "!!!");
        }
    }

    private static boolean isSystemShutDown() {
        return systemShutDown;
    }

    private static void setSystemShutDown() {
        ServerMain.systemShutDown = true;
    }
}

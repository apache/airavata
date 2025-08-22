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
package org.apache.airavata;

import org.apache.airavata.api.AiravataAPIServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.db.event.manager.DBEventManagerRunner;
import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.airavata.metascheduler.metadata.analyzer.DataInterpreterService;
import org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ProcessReschedulingService;
import org.apache.airavata.monitor.cluster.ClusterStatusMonitorJobScheduler;
import org.apache.airavata.monitor.compute.ComputationalResourceMonitoringService;
import org.apache.airavata.monitor.email.EmailBasedMonitor;
import org.apache.airavata.monitor.platform.MonitoringServer;
import org.apache.airavata.monitor.realtime.RealtimeMonitor;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("Starting Airavata API Server .......");
        var airavataApiServer = new AiravataAPIServer();
        airavataApiServer.start();

        System.out.println("Starting DB Event Manager Runner .......");
        var dbEventManagerRunner = new DBEventManagerRunner();
        dbEventManagerRunner.start();

        System.out.println("Starting Helix Controller .......");
        var helixController = new HelixController();
        helixController.start();

        System.out.println("Starting Helix Participant .......");
        var participant = new GlobalParticipant();
        participant.run();

        System.out.println("Starting Pre Workflow Manager .......");
        var preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.run();

        System.out.println("Starting Post Workflow Manager .......");
        var postWorkflowManager = new PostWorkflowManager();
        postWorkflowManager.run();

        if (ServerSettings.getBooleanSetting("data.interpreter.enabled")) {
            System.out.println("Starting Data Interpreter .......");
            var dataInterpreter = new DataInterpreterService();
            dataInterpreter.start();
        }

        if (ServerSettings.getBooleanSetting("process.rescheduler.enabled")) {
            System.out.println("Starting Process Rescheduler .......");
            var processRescheduler = new ProcessReschedulingService();
            processRescheduler.start();
        }

        if (ServerSettings.getBooleanSetting("monitor.email.enabled")) {
            System.out.println("Starting Email Monitor .......");
            var emailMonitor = new EmailBasedMonitor();
            emailMonitor.run();
        }

        if (ServerSettings.getBooleanSetting("monitor.job.realtime.enabled")) {
            System.out.println("Starting Realtime Monitor .......");
            var realTimeMonitor = new RealtimeMonitor();
            realTimeMonitor.run();
        }

        if (ServerSettings.getBooleanSetting("monitor.job.submission.enabled")) {
            System.out.println("Starting Job Submission Monitor .......");
            var clusterMonitor = new ClusterStatusMonitorJobScheduler();
            clusterMonitor.scheduleClusterStatusMonitoring();
        }

        if (ServerSettings.getBooleanSetting("monitor.compute.resource.enabled")) {
            System.out.println("Starting Cluster Resource Monitor .......");
            var resourceMonitor = new ComputationalResourceMonitoringService();
            resourceMonitor.start();
        }

        if (ServerSettings.getBooleanSetting("monitor.prometheus.enabled")) {
            System.out.println("Starting Prometheus Monitor .......");
            var monitoringServer = new MonitoringServer(
                    ServerSettings.getSetting("monitor.prometheus.host"),
                    ServerSettings.getIntSetting("monitor.prometheus.port"));
            monitoringServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
        }

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            System.out.println("Main thread is interrupted! reason: " + ex);
            ServerSettings.setStopAllThreads(true);
        }
    }
}

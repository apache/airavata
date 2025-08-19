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
import org.apache.airavata.db.event.manager.DBEventManagerRunner;
import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.airavata.monitor.cluster.ClusterStatusMonitorJobScheduler;
import org.apache.airavata.monitor.email.EmailBasedMonitor;
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
        var globalParticipant = new GlobalParticipant();
        globalParticipant.run();

        System.out.println("Starting Pre Workflow Manager .......");
        var preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.run();

        System.out.println("Starting Post Workflow Manager .......");
        var postWorkflowManager = new PostWorkflowManager();
        postWorkflowManager.run();

        System.out.println("Starting Email Based Monitor .......");
        var emailBasedMonitor = new EmailBasedMonitor();
        emailBasedMonitor.run();

        System.out.println("Starting RealTime Monitor .......");
        var realTimeMonitor = new RealtimeMonitor();
        realTimeMonitor.run();

        System.out.println("Starting Cluster Status Monitor .......");
        var jobScheduler = new ClusterStatusMonitorJobScheduler();
        assert jobScheduler != null;
        // jobScheduler.scheduleClusterStatusMonitoring();
    }
}

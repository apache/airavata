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
package org.apache.airavata.ide.integration;

import java.util.ArrayList;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class JobEngineStarter {

    @Autowired
    private static GlobalParticipant globalParticipant;

    private static final Logger logger = LoggerFactory.getLogger(JobEngineStarter.class);

    public static void main(String args[]) throws Exception {

        ZkClient zkClient = new ZkClient(
                ServerSettings.getZookeeperConnection(),
                ZkClient.DEFAULT_SESSION_TIMEOUT,
                ZkClient.DEFAULT_CONNECTION_TIMEOUT,
                new ZNRecordSerializer());
        ZKHelixAdmin zkHelixAdmin = new ZKHelixAdmin(zkClient);

        zkHelixAdmin.addCluster(ServerSettings.getSetting("helix.cluster-name"), true);

        logger.info("Starting Helix Controller .......");
        // Starting helix controller
        HelixController controller = new HelixController();
        controller.startServer();

        ArrayList<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();

        for (String taskClassName : GlobalParticipant.TASK_CLASS_NAMES) {
            taskClasses.add(Class.forName(taskClassName).asSubclass(AbstractTask.class));
        }

        logger.info("Starting Helix Participant .......");

        // Starting helix participant
        globalParticipant.startServer();

        logger.info("Starting Pre Workflow Manager .......");

        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.startServer();

        logger.info("Starting Post Workflow Manager .......");

        PostWorkflowManager postWorkflowManager = new PostWorkflowManager();
        postWorkflowManager.startServer();
    }
}

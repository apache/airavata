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
 */
package org.apache.airavata.helix.impl.controller;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.TaskDriver;
import org.apache.helix.task.WorkflowConfig;
import org.apache.helix.task.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WorkflowCleanupAgent implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowCleanupAgent.class);

    private TaskDriver taskDriver;

    public void init() throws Exception {
        logger.info("Initializing cleanup agent");
        final HelixManager helixManager;
        try {
            helixManager = HelixManagerFactory.getZKHelixManager(
                        ServerSettings.getSetting("helix.cluster.name"),
                    ServerSettings.getSetting("helix.controller.name") + "-Cleanup-Agent",
                    InstanceType.SPECTATOR,
                    ServerSettings.getZookeeperConnection());
        } catch (ApplicationSettingsException e) {
            logger.error("Failed to fetch settings to initialize cleanup agent", e);
            throw new Exception("Failed to fetch settings to initialize cleanup agent", e);
        }

        try {
            helixManager.connect();
        } catch (Exception e) {
            logger.error("Failed to connect cleanup agent to helix", e);
            throw new Exception("Failed to connect cleanup agent to helix", e);
        }

        Runtime.getRuntime().addShutdownHook(
                new Thread(helixManager::disconnect)
        );

        taskDriver = new TaskDriver(helixManager);
    }

    @Override
    public void run() {
        logger.info("Cleaning up stale workflows");
        Map<String, WorkflowConfig> workflows = taskDriver.getWorkflows();

        workflows.keySet().forEach(id -> {
            WorkflowContext workflowContext = taskDriver.getWorkflowContext(id);

            if (workflowContext == null) {
                logger.warn("Context for workflow " + id + " is null");

            } else {
                logger.debug(id + " " + workflowContext.getWorkflowState().name());

                switch (workflowContext.getWorkflowState()) {
                    case COMPLETED:
                    //case FAILED:
                    case STOPPED:
                    case TIMED_OUT:
                    case ABORTED:
                        logger.info("Deleting workflow " + id + " with status " + workflowContext.getWorkflowState().name());
                        taskDriver.delete(id);
                        break;
                }
            }
        });
    }
}

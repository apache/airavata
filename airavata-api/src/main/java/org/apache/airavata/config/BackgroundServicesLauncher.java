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
package org.apache.airavata.config;

import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.ParserWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.airavata.monitor.email.EmailBasedMonitor;
import org.apache.airavata.monitor.realtime.RealtimeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Configuration class to launch all background services when Spring Boot starts.
 *
 * <p>Services are started in the correct order based on dependencies using {@code @Order}:
 * <ol>
 *   <li>Helix Controller - Manages Helix cluster</li>
 *   <li>Global Participant - Executes workflow tasks</li>
 *   <li>Pre Workflow Manager - Handles process launch events</li>
 *   <li>Parser Workflow Manager - Handles data parsing workflows</li>
 *   <li>Post Workflow Manager - Handles job completion events</li>
 *   <li>Realtime Monitor - Monitors job status in real-time</li>
 *   <li>Email Monitor - Monitors job status via email</li>
 * </ol>
 *
 * <p>All services are started in daemon threads and are non-blocking.
 * Each service can be enabled/disabled via configuration properties.
 */
@Configuration
public class BackgroundServicesLauncher {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundServicesLauncher.class);

    @Autowired
    private AiravataServerProperties properties;

    @Bean
    @Order(1)
    public CommandLineRunner startHelixController() {
        return args -> {
            if (properties.getHelix().isControllerEnabled()) {
                logger.info("Starting Helix Controller...");
                Thread controllerThread = new Thread(() -> {
                    try {
                        HelixController controller = new HelixController();
                        controller.start();
                    } catch (Exception e) {
                        logger.error("Failed to start Helix Controller", e);
                    }
                });
                controllerThread.setName("HelixController");
                controllerThread.setDaemon(true);
                controllerThread.start();
                logger.info("Helix Controller thread started");
            }
        };
    }

    @Bean
    @Order(2)
    public CommandLineRunner startGlobalParticipant() {
        return args -> {
            if (properties.getHelix().isParticipantEnabled()) {
                logger.info("Starting Global Participant...");
                Thread participantThread = new Thread(() -> {
                    try {
                        GlobalParticipant participant = GlobalParticipant.create();
                        participant.start();
                    } catch (Exception e) {
                        logger.error("Failed to start Global Participant", e);
                    }
                });
                participantThread.setName("GlobalParticipant");
                participantThread.setDaemon(true);
                participantThread.start();
                logger.info("Global Participant thread started");
            }
        };
    }

    @Bean
    @Order(3)
    public CommandLineRunner startPreWorkflowManager() {
        return args -> {
            if (properties.getWorkflow().isPreEnabled()) {
                logger.info("Starting Pre Workflow Manager...");
                Thread preWmThread = new Thread(() -> {
                    try {
                        PreWorkflowManager manager = new PreWorkflowManager();
                        manager.start();
                    } catch (Exception e) {
                        logger.error("Failed to start Pre Workflow Manager", e);
                    }
                });
                preWmThread.setName("PreWorkflowManager");
                preWmThread.setDaemon(true);
                preWmThread.start();
                logger.info("Pre Workflow Manager thread started");
            }
        };
    }

    @Bean
    @Order(4)
    public CommandLineRunner startParserWorkflowManager() {
        return args -> {
            if (properties.getWorkflow().isParserEnabled()) {
                logger.info("Starting Parser Workflow Manager...");
                Thread parserWmThread = new Thread(() -> {
                    try {
                        ParserWorkflowManager manager = new ParserWorkflowManager();
                        manager.start();
                    } catch (Exception e) {
                        logger.error("Failed to start Parser Workflow Manager", e);
                    }
                });
                parserWmThread.setName("ParserWorkflowManager");
                parserWmThread.setDaemon(true);
                parserWmThread.start();
                logger.info("Parser Workflow Manager thread started");
            }
        };
    }

    @Bean
    @Order(5)
    public CommandLineRunner startPostWorkflowManager() {
        return args -> {
            if (properties.getWorkflow().isPostEnabled()) {
                logger.info("Starting Post Workflow Manager...");
                Thread postWmThread = new Thread(() -> {
                    try {
                        PostWorkflowManager manager = new PostWorkflowManager();
                        manager.start();
                    } catch (Exception e) {
                        logger.error("Failed to start Post Workflow Manager", e);
                    }
                });
                postWmThread.setName("PostWorkflowManager");
                postWmThread.setDaemon(true);
                postWmThread.start();
                logger.info("Post Workflow Manager thread started");
            }
        };
    }

    @Bean
    @Order(6)
    public CommandLineRunner startRealtimeMonitor() {
        return args -> {
            if (properties.getMonitoring().getRealtimeMonitor().isMonitorEnabled()) {
                logger.info("Starting Realtime Monitor...");
                Thread monitorThread = new Thread(() -> {
                    try {
                        RealtimeMonitor monitor = new RealtimeMonitor();
                        monitor.start();
                    } catch (Exception e) {
                        logger.error("Failed to start Realtime Monitor", e);
                    }
                });
                monitorThread.setName("RealtimeMonitor");
                monitorThread.setDaemon(true);
                monitorThread.start();
                logger.info("Realtime Monitor thread started");
            }
        };
    }

    @Bean
    @Order(7)
    public CommandLineRunner startEmailMonitor() {
        return args -> {
            if (properties.getMonitoring().getEmailBasedMonitor().isMonitorEnabled()) {
                logger.info("Starting Email Monitor...");
                Thread emailThread = new Thread(() -> {
                    try {
                        EmailBasedMonitor monitor = new EmailBasedMonitor();
                        monitor.start();
                    } catch (Exception e) {
                        logger.error("Failed to start Email Monitor", e);
                    }
                });
                emailThread.setName("EmailMonitor");
                emailThread.setDaemon(true);
                emailThread.start();
                logger.info("Email Monitor thread started");
            }
        };
    }
}

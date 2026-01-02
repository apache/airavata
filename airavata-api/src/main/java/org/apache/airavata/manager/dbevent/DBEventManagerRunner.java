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
package org.apache.airavata.manager.dbevent;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.manager.dbevent.messaging.DBEventManagerMessagingFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * DB Event Manager service that handles database event publishing and subscription.
 *
 * <p>This service starts messaging utilities for database events when enabled.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(name = "services.thrift.enabled", havingValue = "true", matchIfMissing = true)
public class DBEventManagerRunner extends ServerLifecycle {

    private static final String SERVER_NAME = "DB Event Manager";
    private static final String SERVER_VERSION = "1.0";

    private final AiravataServerProperties properties;
    private final DBEventManagerMessagingFactory messagingFactory;

    public DBEventManagerRunner(AiravataServerProperties properties, DBEventManagerMessagingFactory messagingFactory) {
        this.properties = properties;
        this.messagingFactory = messagingFactory;
    }

    @Override
    public String getServerName() {
        return SERVER_NAME;
    }

    @Override
    public String getServerVersion() {
        return SERVER_VERSION;
    }

    @Override
    public int getPhase() {
        // Start before other Thrift servers
        return 10;
    }

    /**
     * Start required messaging utilities
     */
    private void startDBEventManagerRunner() {
        try {
            logger.info("Starting DB Event manager publisher");
            messagingFactory.getDBEventPublisher();
            logger.debug("DB Event manager publisher is running");

            logger.info("Starting DB Event manager subscriber");
            messagingFactory.getDBEventSubscriber();
            logger.debug("DB Event manager subscriber is listening");
        } catch (AiravataException e) {
            logger.error("Error starting DB Event Manager.", e);
            throw new RuntimeException("Failed to start DB Event Manager", e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        logger.info("Starting the DB Event Manager runner.");
        new Thread(() -> this.startDBEventManagerRunner()).start();
    }

    @Override
    protected void doStop() throws Exception {
        // TODO: implement stopping the DBEventManager
        logger.info("Stopping DB Event Manager (shutdown not yet implemented)");
    }
}

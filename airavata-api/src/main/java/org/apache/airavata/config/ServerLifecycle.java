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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * Base abstract class for Airavata server lifecycle management.
 *
 * <p>Provides common functionality for servers that need lifecycle management
 * through Spring's SmartLifecycle interface. Subclasses should implement
 * the actual start/stop logic.
 */
public abstract class ServerLifecycle implements SmartLifecycle {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile boolean running = false;

    /**
     * Get the server name for logging purposes.
     * @return server name
     */
    public abstract String getServerName();

    /**
     * Get the server version.
     * @return server version
     */
    public abstract String getServerVersion();

    /**
     * Perform the actual server startup logic.
     * This is called by Spring's lifecycle management.
     * @throws Exception if startup fails
     */
    protected abstract void doStart() throws Exception;

    /**
     * Perform the actual server shutdown logic.
     * This is called by Spring's lifecycle management.
     * @throws Exception if shutdown fails
     */
    protected abstract void doStop() throws Exception;

    @Override
    public void start() {
        if (isRunning()) {
            logger.debug("{} is already running", getServerName());
            return;
        }

        try {
            logger.info("Starting {} (version: {})...", getServerName(), getServerVersion());
            doStart();
            running = true;
            logger.info("{} started successfully", getServerName());
        } catch (Exception e) {
            logger.error("Failed to start " + getServerName(), e);
            running = false;
            throw new RuntimeException("Failed to start " + getServerName(), e);
        }
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            logger.debug("{} is not running", getServerName());
            return;
        }

        try {
            logger.info("Stopping {}...", getServerName());
            doStop();
            running = false;
            logger.info("{} stopped successfully", getServerName());
        } catch (Exception e) {
            logger.error("Error stopping " + getServerName(), e);
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        if (callback != null) {
            callback.run();
        }
    }

    @Override
    public int getPhase() {
        // Default phase - subclasses should override for ordering
        return 0;
    }
}

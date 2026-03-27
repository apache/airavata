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
package org.apache.airavata.common.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.airavata.common.server.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks background {@link IServer} instances and their threads, exposing
 * lifecycle operations (status query, restart, stop-all) and error recording.
 */
public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private static class Entry {
        final IServer server;
        Thread thread;
        final Supplier<IServer> factory;
        final long startedAt;
        String lastError;

        Entry(IServer server, Thread thread, Supplier<IServer> factory) {
            this.server = server;
            this.thread = thread;
            this.factory = factory;
            this.startedAt = System.currentTimeMillis();
        }
    }

    private final Map<String, Entry> entries = new LinkedHashMap<>();

    /**
     * Registers a service with an optional restart factory.
     *
     * @param label   human-readable service identifier
     * @param service the IServer instance
     * @param thread  the thread running the service
     * @param factory optional supplier to create a fresh IServer for restart; may be null
     */
    public synchronized void register(String label, IServer service, Thread thread, Supplier<IServer> factory) {
        entries.put(label, new Entry(service, thread, factory));
    }

    /**
     * Registers a service without a restart factory.
     */
    public synchronized void register(String label, IServer service, Thread thread) {
        register(label, service, thread, null);
    }

    /**
     * Returns a snapshot of statuses keyed by label.
     * A service is "UP" if its IServer status is STARTED (preferred) or its thread is alive.
     * Some services (e.g. PostWorkflowManager) spawn sub-threads and let their main thread
     * exit — IServer.getStatus() remains STARTED in that case.
     */
    public synchronized Map<String, ServiceStatus> getStatuses() {
        Map<String, ServiceStatus> result = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Entry> e : entries.entrySet()) {
            Entry entry = e.getValue();
            boolean up = entry.server.getStatus() == IServer.ServerStatus.STARTED || entry.thread.isAlive();
            String status = up ? "UP" : "DOWN";
            long uptimeMs = up ? now - entry.startedAt : 0L;
            result.put(e.getKey(), new ServiceStatus(status, uptimeMs, entry.lastError));
        }
        return result;
    }

    /**
     * Attempts to restart the named service using its registered factory.
     *
     * @param name the service label
     * @throws IllegalArgumentException if no service with that name exists
     * @throws IllegalStateException    if the service has no restart factory
     * @throws Exception                if the new service thread fails to start
     */
    public synchronized void restart(String name) throws Exception {
        Entry entry = entries.get(name);
        if (entry == null) {
            throw new IllegalArgumentException("No service registered with name: " + name);
        }
        if (entry.factory == null) {
            throw new IllegalStateException("Service '" + name + "' has no restart factory");
        }
        // Stop the old instance
        try {
            entry.server.stop();
        } catch (Exception e) {
            logger.warn("Error stopping '{}' before restart: {}", name, e.getMessage());
        }
        entry.thread.interrupt();

        // Start a fresh instance
        IServer newServer = entry.factory.get();
        Thread newThread = new Thread(newServer, "airavata-" + name);
        newThread.setDaemon(true);
        newThread.start();

        Entry newEntry = new Entry(newServer, newThread, entry.factory);
        entries.put(name, newEntry);
        logger.info("Service '{}' restarted", name);
    }

    /**
     * Records an error message against the named service.
     * Silently ignores unknown names.
     */
    public synchronized void recordError(String name, String errorMessage) {
        Entry entry = entries.get(name);
        if (entry != null) {
            entry.lastError = errorMessage;
        }
    }

    /**
     * Stops all registered services and interrupts their threads.
     */
    public synchronized void stopAll() {
        for (Map.Entry<String, Entry> e : entries.entrySet()) {
            String label = e.getKey();
            Entry entry = e.getValue();
            try {
                entry.server.stop();
            } catch (Exception ex) {
                logger.warn("Error stopping '{}': {}", label, ex.getMessage());
            }
            entry.thread.interrupt();
        }
    }
}

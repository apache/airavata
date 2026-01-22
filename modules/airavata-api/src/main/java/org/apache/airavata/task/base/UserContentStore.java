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
package org.apache.airavata.task.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserContentStore implementation using Dapr State Store.
 *
 * <p>Replaces org.apache.helix.task.UserContentStore as part of the migration from Helix to Dapr.
 * This class provides the same API surface as Helix's UserContentStore for compatibility
 * with existing task implementations. Now uses Dapr State Store for persistent state management.
 */
public class UserContentStore {

    public enum Scope {
        WORKFLOW,
        JOB,
        TASK
    }

    // Fallback in-memory storage when Dapr is not available
    private static final Map<String, Map<String, String>> workflowContent = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> jobContent = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> taskContent = new ConcurrentHashMap<>();

    // StateManager will be injected by subclasses that have access to Spring context
    // For now, we'll use a static holder pattern or fallback to in-memory
    private static org.apache.airavata.orchestrator.state.StateManager stateManager;

    /**
     * Set the StateManager for use by UserContentStore.
     * Called during application initialization.
     */
    public static void setStateManager(org.apache.airavata.orchestrator.state.StateManager manager) {
        stateManager = manager;
    }

    protected String getUserContent(String key, Scope scope) {
        String contextKey = getContextKey();
        if (contextKey == null) {
            return null;
        }

        // Try State Store first
        if (stateManager != null && stateManager.isAvailable()) {
            try {
                String stateKey = buildStateKey(contextKey, scope, key);
                java.util.Optional<String> value = stateManager.getState(stateKey, String.class);
                if (value.isPresent()) {
                    return value.get();
                }
            } catch (org.apache.airavata.common.exception.AiravataException e) {
                org.slf4j.LoggerFactory.getLogger(UserContentStore.class)
                        .warn("Failed to get content from State Store, falling back to in-memory: {}", e.getMessage());
            }
        }

        // Fallback to in-memory storage
        Map<String, Map<String, String>> contentMap = getContentMap(scope);
        Map<String, String> contextContent = contentMap.get(contextKey);
        return contextContent != null ? contextContent.get(key) : null;
    }

    protected void putUserContent(String key, String value, Scope scope) {
        String contextKey = getContextKey();
        if (contextKey == null) {
            return;
        }

        // Try State Store first
        if (stateManager != null && stateManager.isAvailable()) {
            try {
                String stateKey = buildStateKey(contextKey, scope, key);
                stateManager.saveState(stateKey, value);
                return;
            } catch (org.apache.airavata.common.exception.AiravataException e) {
                org.slf4j.LoggerFactory.getLogger(UserContentStore.class)
                        .warn("Failed to save content to State Store, falling back to in-memory: {}", e.getMessage());
            }
        }

        // Fallback to in-memory storage
        Map<String, Map<String, String>> contentMap = getContentMap(scope);
        contentMap.computeIfAbsent(contextKey, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    /**
     * Build a state key for Dapr State Store.
     * Format: "usercontent:{scope}:{contextKey}:{key}"
     */
    private String buildStateKey(String contextKey, Scope scope, String key) {
        return String.format("usercontent:%s:%s:%s", scope.name().toLowerCase(), contextKey, key);
    }

    private Map<String, Map<String, String>> getContentMap(Scope scope) {
        return switch (scope) {
            case WORKFLOW -> workflowContent;
            case JOB -> jobContent;
            case TASK -> taskContent;
        };
    }

    /**
     * Get the context key for the current workflow/job/task.
     * This should be overridden by subclasses to provide the actual context.
     */
    protected String getContextKey() {
        // Default implementation - subclasses should override
        return null;
    }
}

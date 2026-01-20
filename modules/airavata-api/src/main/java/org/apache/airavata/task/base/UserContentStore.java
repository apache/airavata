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
 * Stub class for UserContentStore.
 *
 * <p>Replaces org.apache.helix.task.UserContentStore as part of the migration from Helix to Dapr.
 * This class provides the same API surface as Helix's UserContentStore for compatibility
 * with existing task implementations. In the future, this will be replaced with Dapr Workflow state.
 */
public class UserContentStore {

    public enum Scope {
        WORKFLOW,
        JOB,
        TASK
    }

    // In-memory storage for workflow state (will be replaced with Dapr Workflow state)
    private static final Map<String, Map<String, String>> workflowContent = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> jobContent = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> taskContent = new ConcurrentHashMap<>();

    protected String getUserContent(String key, Scope scope) {
        Map<String, Map<String, String>> contentMap = getContentMap(scope);
        String contextKey = getContextKey();
        if (contextKey == null) {
            return null;
        }
        Map<String, String> contextContent = contentMap.get(contextKey);
        return contextContent != null ? contextContent.get(key) : null;
    }

    protected void putUserContent(String key, String value, Scope scope) {
        Map<String, Map<String, String>> contentMap = getContentMap(scope);
        String contextKey = getContextKey();
        if (contextKey == null) {
            return;
        }
        contentMap.computeIfAbsent(contextKey, k -> new ConcurrentHashMap<>()).put(key, value);
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

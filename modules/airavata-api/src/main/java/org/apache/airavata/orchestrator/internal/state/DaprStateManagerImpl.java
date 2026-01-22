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
package org.apache.airavata.orchestrator.internal.state;

import io.dapr.client.DaprClient;
import java.util.Optional;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.orchestrator.internal.config.DaprConfigConstants;
import org.apache.airavata.orchestrator.state.StateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Dapr-based implementation of StateManager.
 */
@Component
@ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DaprStateManagerImpl implements StateManager {

    private static final Logger logger = LoggerFactory.getLogger(DaprStateManagerImpl.class);

    private final DaprClient daprClient;
    private final String stateStoreName;
    private final boolean daprEnabled;

    @Autowired
    public DaprStateManagerImpl(
            @Autowired(required = false) DaprClient daprClient,
            @Value("${" + DaprConfigConstants.DAPR_ENABLED + ":false}") boolean daprEnabled,
            @Value("${" + DaprConfigConstants.DAPR_STATE_NAME + ":" + DaprConfigConstants.DEFAULT_STATE_NAME + "}")
                    String stateStoreName) {
        this.daprClient = daprClient;
        this.daprEnabled = daprEnabled;
        this.stateStoreName = stateStoreName;
    }

    @Override
    public <T> void saveState(String key, T value) throws AiravataException {
        if (!isAvailable()) {
            throw new AiravataException("State management not available");
        }
        try {
            daprClient.saveState(stateStoreName, key, value).block();
            logger.debug("Saved state: {}", key);
        } catch (Exception e) {
            throw new AiravataException("Failed to save state: " + key, e);
        }
    }

    @Override
    public <T> Optional<T> getState(String key, Class<T> type) throws AiravataException {
        if (!isAvailable()) {
            throw new AiravataException("State management not available");
        }
        try {
            var state = daprClient.getState(stateStoreName, key, type).block();
            if (state == null || state.getValue() == null) {
                return Optional.empty();
            }
            return Optional.of(state.getValue());
        } catch (Exception e) {
            throw new AiravataException("Failed to get state: " + key, e);
        }
    }

    @Override
    public void deleteState(String key) throws AiravataException {
        if (!isAvailable()) {
            throw new AiravataException("State management not available");
        }
        try {
            daprClient.deleteState(stateStoreName, key).block();
            logger.debug("Deleted state: {}", key);
        } catch (Exception e) {
            throw new AiravataException("Failed to delete state: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) throws AiravataException {
        if (!isAvailable()) {
            return false;
        }
        try {
            var state = daprClient.getState(stateStoreName, key, String.class).block();
            return state != null && state.getValue() != null;
        } catch (Exception e) {
            throw new AiravataException("Failed to check state existence: " + key, e);
        }
    }

    @Override
    public boolean isAvailable() {
        return daprEnabled && daprClient != null;
    }
}

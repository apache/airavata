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
package org.apache.airavata.dapr.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.apache.airavata.dapr.config.DaprConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Dapr-based implementation of {@link DaprStateManager}.
 *
 * <p>Wraps Dapr client state operations with consistent error handling and
 * JSON serialization/deserialization.
 */
@Component
@ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true", matchIfMissing = false)
public class DaprStateManagerImpl implements DaprStateManager {

    private static final Logger logger = LoggerFactory.getLogger(DaprStateManagerImpl.class);

    private final io.dapr.client.DaprClient daprClient;
    private final String stateStoreName;
    private final ObjectMapper objectMapper;
    private final boolean daprEnabled;

    @Autowired
    public DaprStateManagerImpl(
            @Autowired(required = false) io.dapr.client.DaprClient daprClient,
            @Value("${" + DaprConfigConstants.DAPR_ENABLED + ":false}") boolean daprEnabled,
            @Value("${" + DaprConfigConstants.DAPR_STATE_NAME + ":" + DaprConfigConstants.DEFAULT_STATE_NAME + "}")
                    String stateStoreName,
            ObjectMapper objectMapper) {
        this.daprClient = daprClient;
        this.daprEnabled = daprEnabled;
        this.stateStoreName = stateStoreName;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void saveState(String key, T value) throws DaprStateException {
        if (!isAvailable()) {
            throw new DaprStateException("Dapr state management is not available");
        }
        try {
            daprClient.saveState(stateStoreName, key, value).block();
            logger.debug("Saved state for key: {}", key);
        } catch (Exception e) {
            String msg = "Failed to save state for key: " + key;
            logger.error(msg, e);
            throw new DaprStateException(msg, e);
        }
    }

    @Override
    public <T> Optional<T> getState(String key, Class<T> type) throws DaprStateException {
        if (!isAvailable()) {
            throw new DaprStateException("Dapr state management is not available");
        }
        try {
            io.dapr.client.domain.State<T> state =
                    daprClient.getState(stateStoreName, key, type).block();
            if (state == null || state.getValue() == null) {
                logger.debug("No state found for key: {}", key);
                return Optional.empty();
            }
            logger.debug("Retrieved state for key: {}", key);
            return Optional.of(state.getValue());
        } catch (Exception e) {
            String msg = "Failed to get state for key: " + key;
            logger.error(msg, e);
            throw new DaprStateException(msg, e);
        }
    }

    @Override
    public void deleteState(String key) throws DaprStateException {
        if (!isAvailable()) {
            throw new DaprStateException("Dapr state management is not available");
        }
        try {
            daprClient.deleteState(stateStoreName, key).block();
            logger.debug("Deleted state for key: {}", key);
        } catch (Exception e) {
            String msg = "Failed to delete state for key: " + key;
            logger.error(msg, e);
            throw new DaprStateException(msg, e);
        }
    }

    @Override
    public boolean exists(String key) throws DaprStateException {
        if (!isAvailable()) {
            return false;
        }
        try {
            io.dapr.client.domain.State<String> state =
                    daprClient.getState(stateStoreName, key, String.class).block();
            boolean exists = state != null && state.getValue() != null;
            logger.debug("Checked existence for key: {} - {}", key, exists);
            return exists;
        } catch (Exception e) {
            String msg = "Failed to check existence for key: " + key;
            logger.error(msg, e);
            throw new DaprStateException(msg, e);
        }
    }

    @Override
    public boolean isAvailable() {
        return daprEnabled && daprClient != null;
    }
}

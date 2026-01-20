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

import java.util.Optional;

/**
 * Abstraction layer for Dapr state store operations.
 *
 * <p>Provides a consistent interface for state management operations, hiding
 * the details of Dapr client usage. This abstraction enables:
 * <ul>
 *   <li>Consistent error handling</li>
 *   <li>Easier testing (can be mocked)</li>
 *   <li>Future flexibility (can swap implementations)</li>
 * </ul>
 *
 * <p>State keys should be generated using {@link DaprStateKeys} to ensure
 * consistent naming conventions.
 */
public interface DaprStateManager {

    /**
     * Save a value to the Dapr state store.
     *
     * @param key the state key (should be generated using {@link DaprStateKeys})
     * @param value the value to store
     * @param <T> the type of the value
     * @throws DaprStateException if the operation fails
     */
    <T> void saveState(String key, T value) throws DaprStateException;

    /**
     * Retrieve a value from the Dapr state store.
     *
     * @param key the state key
     * @param type the expected type of the value
     * @param <T> the type of the value
     * @return Optional containing the value if found, empty otherwise
     * @throws DaprStateException if the operation fails
     */
    <T> Optional<T> getState(String key, Class<T> type) throws DaprStateException;

    /**
     * Delete a value from the Dapr state store.
     *
     * @param key the state key
     * @throws DaprStateException if the operation fails
     */
    void deleteState(String key) throws DaprStateException;

    /**
     * Check if a key exists in the Dapr state store.
     *
     * @param key the state key
     * @return true if the key exists, false otherwise
     * @throws DaprStateException if the operation fails
     */
    boolean exists(String key) throws DaprStateException;

    /**
     * Check if Dapr state management is available.
     *
     * @return true if Dapr is enabled and the state store is available
     */
    boolean isAvailable();
}

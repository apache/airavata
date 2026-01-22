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
package org.apache.airavata.orchestrator.state;

import java.util.Optional;
import org.apache.airavata.common.exception.AiravataException;

/**
 * Public interface for workflow state operations.
 */
public interface StateManager {

    <T> void saveState(String key, T value) throws AiravataException;

    <T> Optional<T> getState(String key, Class<T> type) throws AiravataException;

    void deleteState(String key) throws AiravataException;

    boolean exists(String key) throws AiravataException;

    boolean isAvailable();
}

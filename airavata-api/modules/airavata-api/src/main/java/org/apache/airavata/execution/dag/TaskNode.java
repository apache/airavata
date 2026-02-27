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
package org.apache.airavata.execution.dag;

import java.util.Map;

/**
 * A node in a {@link ProcessDAG}, binding a logical step ID to a
 * Spring bean name that implements {@link DagTask}.
 *
 * @param id           unique node identifier within the DAG (e.g. "provision", "submit")
 * @param taskBeanName Spring bean name to resolve via {@code ApplicationContext.getBean()}
 * @param onSuccess    node ID to execute on success, or {@code null} if terminal
 * @param onFailure    node ID to execute on failure, or {@code null} if terminal
 * @param metadata     arbitrary key-value pairs available to interceptors
 *                     (e.g. "processState" for status publishing)
 */
public record TaskNode(
        String id, String taskBeanName, String onSuccess, String onFailure, Map<String, String> metadata) {

    public TaskNode(String id, String taskBeanName, String onSuccess, String onFailure) {
        this(id, taskBeanName, onSuccess, onFailure, Map.of());
    }
}

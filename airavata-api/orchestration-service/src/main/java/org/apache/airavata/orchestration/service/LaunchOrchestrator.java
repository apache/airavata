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
package org.apache.airavata.orchestration.service;

/**
 * Bridge from experiment launch to in-process orchestration. The implementation
 * synchronously creates the PROCESS and ordered TASK rows for an experiment and
 * advances status, replacing the legacy RabbitMQ/Helix launch path.
 *
 * <p>Lives in {@code orchestration-service} (the job/task/process layer). The
 * application layer ({@code research-service}) depends on this module and injects
 * the implementation at runtime.
 */
public interface LaunchOrchestrator {

    void launchExperiment(String experimentId, String gatewayId) throws Exception;
}

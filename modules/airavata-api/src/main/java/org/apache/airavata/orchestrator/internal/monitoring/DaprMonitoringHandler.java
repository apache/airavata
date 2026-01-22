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
package org.apache.airavata.orchestrator.internal.monitoring;

/**
 * Handler for monitoring messages delivered via Dapr Pub/Sub to the
 * monitoring-data-topic. Used when the payload is raw JSON (e.g. {jobName, status, task})
 * rather than {@link org.apache.airavata.orchestrator.internal.messaging.MessageContext}.
 */
@FunctionalInterface
public interface DaprMonitoringHandler {

    /**
     * Process a monitoring message from Dapr.
     *
     * @param key    optional key (e.g. from CloudEvent or metadata); may be empty
     * @param payload JSON string payload
     */
    void onMonitoringMessage(String key, String payload);
}

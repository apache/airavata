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
package org.apache.airavata.orchestrator.config;

import org.apache.airavata.orchestrator.internal.config.DaprConfigConstants;

/**
 * Public configuration constants for orchestrator.
 *
 * <p>Provides access to orchestrator configuration property keys.
 * These constants should be used with Spring's {@code @Value} annotation:
 * <pre>
 * {@code @Value("${" + OrchestratorConfig.ENABLED + ":false}")}
 * </pre>
 */
public final class OrchestratorConfig {

    /** Configuration key for enabling/disabling durable execution: {@value} */
    public static final String ENABLED = DaprConfigConstants.DAPR_ENABLED;

    /** Configuration key for Pub/Sub component name: {@value} */
    public static final String PUBSUB_NAME = DaprConfigConstants.DAPR_PUBSUB_NAME;

    /** Configuration key for State Store component name: {@value} */
    public static final String STATE_NAME = DaprConfigConstants.DAPR_STATE_NAME;

    /** Default value for Pub/Sub component name: {@value} */
    public static final String DEFAULT_PUBSUB_NAME = DaprConfigConstants.DEFAULT_PUBSUB_NAME;

    /** Default value for State Store component name: {@value} */
    public static final String DEFAULT_STATE_NAME = DaprConfigConstants.DEFAULT_STATE_NAME;

    private OrchestratorConfig() {
        // Utility class - prevent instantiation
    }
}

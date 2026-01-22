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
package org.apache.airavata.orchestrator.internal.config;

/**
 * Centralized constants for Dapr configuration property keys.
 *
 * <p>All Dapr-related configuration property keys should be defined here to ensure
 * consistency and make configuration usage discoverable.
 *
 * <p>These constants should be used with Spring's {@code @Value} annotation:
 * <pre>
 * {@code @Value("${" + DaprConfigConstants.DAPR_ENABLED + ":false}")}
 * </pre>
 */
public final class DaprConfigConstants {

    /** Configuration key for enabling/disabling Dapr: {@value} */
    public static final String DAPR_ENABLED = "airavata.dapr.enabled";

    /** Configuration key for Dapr Pub/Sub component name: {@value} */
    public static final String DAPR_PUBSUB_NAME = "airavata.dapr.pubsub.name";

    /** Configuration key for Dapr State Store component name: {@value} */
    public static final String DAPR_STATE_NAME = "airavata.dapr.state.name";

    /** Default value for Dapr Pub/Sub component name: {@value} */
    public static final String DEFAULT_PUBSUB_NAME = "redis-pubsub";

    /** Default value for Dapr State Store component name: {@value} */
    public static final String DEFAULT_STATE_NAME = "redis-state";

    private DaprConfigConstants() {
        // Utility class - prevent instantiation
    }
}

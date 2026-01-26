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
package org.apache.airavata.common.model;

/**
 * Enum representing the type of resource a preference applies to.
 *
 * <p>This enum supports hierarchical preference resolution where preferences can be set
 * at GATEWAY, GROUP, or USER levels with USER > GROUP > GATEWAY precedence.
 *
 * <h3>Resource ID Conventions:</h3>
 * <ul>
 *   <li>COMPUTE: computeResourceId (e.g., "stampede2.tacc.utexas.edu")</li>
 *   <li>STORAGE: storageResourceId (e.g., "jetstream-storage-01")</li>
 *   <li>PROFILE: profileId (e.g., gatewayId, groupId, or userId@gatewayId)</li>
 *   <li>BATCH_QUEUE: computeResourceId:queueName (e.g., "stampede2.tacc.utexas.edu:normal")</li>
 *   <li>APPLICATION: applicationInterfaceId (e.g., "Gaussian_Interface_01")</li>
 *   <li>GATEWAY: gatewayId (e.g., "seagrid")</li>
 *   <li>SYSTEM: "GLOBAL" for system-wide, or gatewayId for gateway overrides</li>
 * </ul>
 */
public enum PreferenceResourceType {
    /**
     * Preferences for compute resources (HPC clusters, cloud instances, etc.)
     * Resource ID: computeResourceId
     */
    COMPUTE(0),

    /**
     * Preferences for storage resources (file systems, object stores, etc.)
     * Resource ID: storageResourceId
     */
    STORAGE(1),

    /**
     * Preferences for profile-level metadata (e.g., profile name).
     * Resource ID: profileId
     */
    PROFILE(2),

    /**
     * Preferences for batch queue policies and limits.
     * Resource ID: computeResourceId:queueName (colon-separated)
     */
    BATCH_QUEUE(3),

    /**
     * Preferences for application defaults and settings.
     * Resource ID: applicationInterfaceId
     */
    APPLICATION(4),

    /**
     * Preferences for gateway-level configuration.
     * Resource ID: gatewayId
     */
    GATEWAY(5),

    /**
     * Preferences for system-wide settings with optional gateway overrides.
     * Resource ID: "GLOBAL" for system-wide, or gatewayId for gateway-specific overrides
     */
    SYSTEM(6);

    /** Special resource ID for system-wide preferences */
    public static final String GLOBAL_RESOURCE_ID = "GLOBAL";

    /** Separator for composite resource IDs (e.g., BATCH_QUEUE) */
    public static final String RESOURCE_ID_SEPARATOR = ":";

    private final int value;

    PreferenceResourceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PreferenceResourceType findByValue(int value) {
        switch (value) {
            case 0:
                return COMPUTE;
            case 1:
                return STORAGE;
            case 2:
                return PROFILE;
            case 3:
                return BATCH_QUEUE;
            case 4:
                return APPLICATION;
            case 5:
                return GATEWAY;
            case 6:
                return SYSTEM;
            default:
                return null;
        }
    }

    /**
     * Create a batch queue resource ID from compute resource and queue name.
     *
     * @param computeResourceId the compute resource ID
     * @param queueName the queue name
     * @return the combined resource ID
     */
    public static String batchQueueResourceId(String computeResourceId, String queueName) {
        return computeResourceId + RESOURCE_ID_SEPARATOR + queueName;
    }

    /**
     * Parse a batch queue resource ID into its components.
     *
     * @param resourceId the combined resource ID
     * @return array of [computeResourceId, queueName], or null if invalid format
     */
    public static String[] parseBatchQueueResourceId(String resourceId) {
        if (resourceId == null || !resourceId.contains(RESOURCE_ID_SEPARATOR)) {
            return null;
        }
        int idx = resourceId.indexOf(RESOURCE_ID_SEPARATOR);
        return new String[] {resourceId.substring(0, idx), resourceId.substring(idx + 1)};
    }
}

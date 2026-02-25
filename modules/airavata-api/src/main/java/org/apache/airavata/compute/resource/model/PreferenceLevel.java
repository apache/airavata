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
package org.apache.airavata.compute.resource.model;

/**
 * Enum representing the level at which a preference is set.
 * Used for hierarchical preference resolution (Zanzibar-like model).
 *
 * <p>Resolution order (lower number = higher authority): SYSTEM > GATEWAY > GROUP
 *
 * <p>When resolving effective preferences for a user:
 * <ol>
 *   <li>SYSTEM preferences (cross-gateway root authority)</li>
 *   <li>GATEWAY preferences (gateway-level defaults)</li>
 *   <li>GROUP preferences (all groups including personal groups - equal at this level)</li>
 * </ol>
 *
 * <p>User-specific preferences are stored at GROUP level under the user's personal group.
 */
public enum PreferenceLevel {
    /**
     * System-level preferences. Cross-gateway root authority.
     * Highest priority. Set only by system admins.
     */
    SYSTEM(0),

    /**
     * Gateway-level preferences. Default settings for all users in a gateway.
     */
    GATEWAY(1),

    /**
     * Group-level preferences. All groups (personal, admin, custom) are equal at this level.
     * When multiple groups have the same preference key, user must explicitly select.
     */
    GROUP(2),

    /**
     * User-level preferences. Deprecated: use GROUP level with ownerId = user's personal group ID.
     * Kept for backward compatibility when reading pre-migration data.
     *
     * @deprecated Map to personal group and use GROUP level instead.
     */
    @Deprecated
    USER(2),

    /**
     * Project-level access. Used for RESOURCE_ACCESS when credential access is scoped to a project.
     * ownerId = projectId. Users with project access can use the project's credentials for the resource.
     */
    PROJECT(3);

    private final int priority;

    PreferenceLevel(int priority) {
        this.priority = priority;
    }

    /**
     * Get the priority value. Lower values indicate higher authority.
     *
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Check if this level has higher authority than another level.
     *
     * @param other the other level to compare against
     * @return true if this level should override the other level
     */
    public boolean overrides(PreferenceLevel other) {
        return this.priority < other.priority;
    }

    /**
     * Find a PreferenceLevel by its priority value.
     *
     * @param priority the priority value
     * @return the corresponding PreferenceLevel, or null if not found
     */
    public static PreferenceLevel findByPriority(int priority) {
        switch (priority) {
            case 0:
                return SYSTEM;
            case 1:
                return GATEWAY;
            case 2:
                return GROUP;
            case 3:
                return PROJECT;
            default:
                return null;
        }
    }

    /**
     * Whether this level represents a group-level preference (GROUP or deprecated USER).
     */
    public boolean isGroupLevel() {
        return this == GROUP || this == USER;
    }

    /**
     * Whether this level represents project-scoped access.
     */
    public boolean isProjectLevel() {
        return this == PROJECT;
    }
}

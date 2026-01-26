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
 * Enum representing the level at which a preference is set.
 * Used for hierarchical preference resolution where more specific levels
 * override less specific ones.
 *
 * <p>Resolution order (most specific wins): USER > GROUP > GATEWAY
 *
 * <p>When resolving effective preferences for a user:
 * <ol>
 *   <li>Start with GATEWAY preferences as the base/default</li>
 *   <li>Overlay GROUP preferences (if user belongs to groups with preferences)</li>
 *   <li>Overlay USER preferences (highest priority, user's personal settings)</li>
 * </ol>
 */
public enum PreferenceLevel {
    /**
     * Gateway-level preferences. These are the default settings that apply
     * to all users in a gateway unless overridden at a more specific level.
     */
    GATEWAY(0),

    /**
     * Group-level preferences. These override gateway defaults for users
     * who belong to the group. Multiple group preferences may apply if
     * a user belongs to multiple groups.
     */
    GROUP(1),

    /**
     * User-level preferences. These are the most specific and always take
     * precedence over group and gateway preferences.
     */
    USER(2);

    private final int priority;

    PreferenceLevel(int priority) {
        this.priority = priority;
    }

    /**
     * Get the priority value. Higher values indicate higher priority
     * in preference resolution.
     *
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Check if this level has higher priority than another level.
     *
     * @param other the other level to compare against
     * @return true if this level should override the other level
     */
    public boolean overrides(PreferenceLevel other) {
        return this.priority > other.priority;
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
                return GATEWAY;
            case 1:
                return GROUP;
            case 2:
                return USER;
            default:
                return null;
        }
    }
}

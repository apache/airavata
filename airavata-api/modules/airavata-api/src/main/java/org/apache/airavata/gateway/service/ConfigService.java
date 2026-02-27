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
package org.apache.airavata.gateway.service;

import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.resource.model.PreferenceLevel;

/**
 * Unified configuration service for both gateway-level and system-level preferences.
 *
 * <p>Both layers delegate to {@link PreferenceResolutionService} and differ only
 * in the {@code PreferenceResourceType} enum value used.
 */
public interface ConfigService {

    // ========== Gateway Configuration ==========

    Map<String, String> getEffectiveConfig(String gatewayId, String userId, List<String> groupIds);

    String getConfigValue(String gatewayId, String userId, List<String> groupIds, String key);

    boolean isFeatureEnabled(String gatewayId, String userId, List<String> groupIds, String featureName);

    Map<String, Boolean> getFeatureFlags(String gatewayId, String userId, List<String> groupIds);

    boolean isMaintenanceMode(String gatewayId, String userId, List<String> groupIds);

    String getMaintenanceMessage(String gatewayId, String userId, List<String> groupIds);

    void setGatewayConfig(String ownerId, PreferenceLevel level, String gatewayId, String key, String value);

    void setMaintenanceMode(String ownerId, PreferenceLevel level, String gatewayId, boolean enabled);

    void setMaintenanceMessage(String ownerId, PreferenceLevel level, String gatewayId, String message);

    void setFeatureFlag(String ownerId, PreferenceLevel level, String gatewayId, String featureName, boolean enabled);

    void deleteGatewayConfig(String ownerId, PreferenceLevel level, String gatewayId, String key);

    // ========== System Configuration ==========

    Map<String, String> getEffectiveSystemConfig(String gatewayId, String userId, List<String> groupIds);

    String getSystemConfig(String gatewayId, String userId, List<String> groupIds, String key);

    String getGlobalDefault(String key);

    void setGlobalConfig(String key, String value);

    void setGatewayOverride(String gatewayId, String key, String value);

    void deleteGlobalConfig(String key);

    void deleteGatewayOverride(String gatewayId, String key);
}

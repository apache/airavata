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
package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.apache.airavata.config.ServerProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * System endpoints: health check and public portal config (no authentication required).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "System")
public class SystemController {

    private final ServerProperties properties;

    @Value("${airavata.portal.assume-root-when-no-gateways:false}")
    private boolean assumeRootWhenNoGateways;

    @Value("${airavata.portal.app-version:}")
    private String appVersion;

    public SystemController(ServerProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "airavata-api"));
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        String defaultGatewayId = properties.defaultGateway();
        if (defaultGatewayId == null || defaultGatewayId.isEmpty()) {
            defaultGatewayId = "default";
        }
        return ResponseEntity.ok(Map.of(
                "defaultGatewayId", defaultGatewayId,
                "assumeRootWhenNoGateways", assumeRootWhenNoGateways,
                "appVersion", appVersion != null ? appVersion : ""));
    }
}

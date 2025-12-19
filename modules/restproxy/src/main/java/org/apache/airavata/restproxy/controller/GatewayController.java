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
package org.apache.airavata.restproxy.controller;

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.GatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gateways")
public class GatewayController {
    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping
    public ResponseEntity<?> getAllGateways() {
        try {
            List<Gateway> gateways = gatewayService.getAllGateways();
            return ResponseEntity.ok(gateways);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{gatewayId}")
    public ResponseEntity<?> getGateway(@PathVariable String gatewayId) {
        try {
            Gateway gateway = gatewayService.getGateway(gatewayId);
            if (gateway == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(gateway);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createGateway(@RequestBody Gateway gateway) {
        try {
            String gatewayId = gatewayService.addGateway(gateway);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("gatewayId", gatewayId));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{gatewayId}")
    public ResponseEntity<?> updateGateway(@PathVariable String gatewayId, @RequestBody Gateway gateway) {
        try {
            gateway.setGatewayId(gatewayId);
            gatewayService.updateGateway(gatewayId, gateway);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{gatewayId}")
    public ResponseEntity<?> deleteGateway(@PathVariable String gatewayId) {
        try {
            gatewayService.removeGateway(gatewayId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.apache.airavata.agent.UserContext;
import org.apache.airavata.agent.entity.PlanEntity;
import org.apache.airavata.agent.service.PlanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plans")
public class PlanController {

    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    private final PlanHandler planHandler;
    private final ObjectMapper objectMapper;

    public PlanController(PlanHandler planHandler, ObjectMapper objectMapper) {
        this.planHandler = planHandler;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<PlanEntity> savePlan(@RequestBody JsonNode incomingData) throws JsonProcessingException {
        var planId = incomingData.get("id").asText();

        var dataAsString = objectMapper.writeValueAsString(incomingData);

        var plan = new PlanEntity();
        plan.setId(planId);
        plan.setUserId(UserContext.username());
        plan.setGatewayId(UserContext.gatewayId());
        plan.setData(dataAsString);

        var savedPlan = planHandler.savePlan(plan);
        return ResponseEntity.ok(savedPlan);
    }

    @GetMapping("/user")
    public ResponseEntity<List<PlanEntity>> getPlansByUserId() {
        var plans = planHandler.getAllPlansByUserId(UserContext.username(), UserContext.gatewayId());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanEntity> getPlanById(@PathVariable("planId") String planId) {
        var plan = planHandler.getPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    @PutMapping("/{planId}")
    public ResponseEntity<PlanEntity> updatePlan(
            @PathVariable("planId") String planId, @RequestBody JsonNode incomingData) throws JsonProcessingException {
        var existingPlan = planHandler.getPlanById(planId);

        if (existingPlan == null) {
            logger.error("Couldn't find a plan with id: {} to update", planId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var dataAsString = objectMapper.writeValueAsString(incomingData);
        existingPlan.setData(dataAsString);
        var updatedPlan = planHandler.savePlan(existingPlan);
        return ResponseEntity.ok(updatedPlan);
    }
}

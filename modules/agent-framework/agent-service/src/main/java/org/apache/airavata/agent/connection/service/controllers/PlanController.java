/*
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
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.airavata.agent.connection.service.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.agent.connection.service.db.entity.Plan;
import org.apache.airavata.agent.connection.service.handlers.PlanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plan")
public class PlanController {

    private final static Logger logger = LoggerFactory.getLogger(PlanController.class);

    private final PlanHandler planHandler;
    private final ObjectMapper objectMapper;

    public PlanController(PlanHandler planHandler, ObjectMapper objectMapper) {
        this.planHandler = planHandler;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<Plan> savePlan(@RequestBody JsonNode incomingData) {
        try {
            String planId = incomingData.get("id").asText();

            String dataAsString = objectMapper.writeValueAsString(incomingData);

            Plan plan = new Plan();
            plan.setId(planId);
            plan.setUserId(UserContext.username());
            plan.setGatewayId(UserContext.gatewayId());
            plan.setData(dataAsString);

            Plan savedPlan = planHandler.savePlan(plan);
            return ResponseEntity.ok(savedPlan);

        } catch (Exception e) {
            logger.error("Error while generating the plan data string", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Plan>> getPlansByUserId() {
        List<Plan> plans = planHandler.getAllPlansByUserId(UserContext.username(), UserContext.gatewayId());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<Plan> getPlanById(@PathVariable("planId") String planId) {
        Plan plan = planHandler.getPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    @PutMapping("/{planId}")
    public ResponseEntity<Plan> updatePlan(@PathVariable("planId") String planId, @RequestBody JsonNode incomingData) {
        try {
            Plan existingPlan = planHandler.getPlanById(planId);

            if (existingPlan == null) {
                logger.error("Couldn't find a plan with id: {} to update", planId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String dataAsString = objectMapper.writeValueAsString(incomingData);
            existingPlan.setData(dataAsString);
            Plan updatedPlan = planHandler.savePlan(existingPlan);
            return ResponseEntity.ok(updatedPlan);

        } catch (Exception e) {
            logger.error("Error while generating the plant data to update the plan with the id: {}", planId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

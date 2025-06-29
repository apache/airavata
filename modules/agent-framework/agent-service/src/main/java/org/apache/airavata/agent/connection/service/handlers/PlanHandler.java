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
package org.apache.airavata.agent.connection.service.handlers;

import java.util.List;
import org.apache.airavata.agent.connection.service.db.entity.Plan;
import org.apache.airavata.agent.connection.service.db.repo.PlanRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PlanHandler {

    private static final Logger logger = LoggerFactory.getLogger(PlanHandler.class);

    private final PlanRepo planRepo;

    public PlanHandler(PlanRepo planRepo) {
        this.planRepo = planRepo;
    }

    public Plan savePlan(Plan plan) {
        Plan savedPlan = planRepo.save(plan);
        logger.info("Created the plan with the id: {}", plan.getId());
        return savedPlan;
    }

    public List<Plan> getAllPlansByUserId(String userId, String gatewayId) {
        return planRepo.findAllByUserIdAndGatewayId(userId, gatewayId);
    }

    public Plan getPlanById(String planId) {
        return planRepo.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
    }
}

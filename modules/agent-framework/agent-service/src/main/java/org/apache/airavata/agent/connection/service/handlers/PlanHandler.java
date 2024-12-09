package org.apache.airavata.agent.connection.service.handlers;

import org.apache.airavata.agent.connection.service.db.entity.Plan;
import org.apache.airavata.agent.connection.service.db.repo.PlanRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanHandler {

    private final static Logger logger = LoggerFactory.getLogger(PlanHandler.class);

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

package org.apache.airavata.agent.connection.service.db.repo;

import org.apache.airavata.agent.connection.service.db.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepo extends JpaRepository<Plan, String> {

    List<Plan> findAllByUserIdAndGatewayId(String userId, String gatewayId);
}

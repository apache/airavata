package org.apache.airavata.agent.connection.service.db.repo;

import org.apache.airavata.agent.connection.service.db.entity.AgentExecutionStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentExecutionStatusRepo extends CrudRepository<AgentExecutionStatus, String> {
}

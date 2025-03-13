package org.apache.airavata.agent.connection.service.db.repo;

import org.apache.airavata.agent.connection.service.db.entity.AgentExecution;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentExecutionRepo extends CrudRepository<AgentExecution, String> {
}

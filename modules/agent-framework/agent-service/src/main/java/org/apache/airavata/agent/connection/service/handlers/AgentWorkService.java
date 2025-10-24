/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.agent.connection.service.handlers;

import org.apache.airavata.agent.connection.service.db.entity.AgentBatchAssignmentEntity;
import org.apache.airavata.agent.connection.service.db.repo.AgentBatchAssignmentRepo;
import org.apache.airavata.agent.connection.service.db.repo.JobUnitRepo;
import org.apache.airavata.agent.connection.service.models.AssignResult;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AgentWorkService {

    private final AgentBatchAssignmentRepo assignmentRepo;
    private final JobUnitAllocator allocator;
    private final JobUnitRepo jobUnitRepo;

    public AgentWorkService(AgentBatchAssignmentRepo assignmentRepo, JobUnitAllocator allocator, JobUnitRepo jobUnitRepo) {
        this.assignmentRepo = assignmentRepo;
        this.allocator = allocator;
        this.jobUnitRepo = jobUnitRepo;
    }

    public AssignResult assignNextForAgent(String agentId) {
        Optional<AgentBatchAssignmentEntity> absOp = assignmentRepo.findByAgentId(agentId);
        if (absOp.isEmpty()) {
            return AssignResult.noAssignment();
        }

        var batchId = absOp.get().getBatchId();
        Optional<JobUnitAllocator.JobUnitRow> next = allocator.allocateNext(batchId, agentId);
        if (next.isPresent()) {
            JobUnitAllocator.JobUnitRow row = next.get();
            return AssignResult.assigned(row.id(), row.resolvedCommand());
        }

        int remaining = jobUnitRepo.countRemaining(batchId);
        return (remaining == 0) ? AssignResult.emptyAllDone() : AssignResult.empty();
    }

    public void markCompleted(String jobUnitId) {
        allocator.markCompleted(jobUnitId);
    }
}


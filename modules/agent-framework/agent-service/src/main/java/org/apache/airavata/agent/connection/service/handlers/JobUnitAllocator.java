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

import jakarta.transaction.Transactional;
import org.apache.airavata.agent.connection.service.db.repo.JobUnitRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JobUnitAllocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobUnitAllocator.class);

    private final JobUnitRepo jobUnitRepo;

    public record JobUnitRow(String id, String resolvedCommand) {
    }

    public JobUnitAllocator(JobUnitRepo jobUnitRepo) {
        this.jobUnitRepo = jobUnitRepo;
    }

    @Transactional
    public Optional<JobUnitRow> allocateNext(String batchId, String agentId) {
        String id = jobUnitRepo.lockNextPending(batchId);
        LOGGER.info("Job unit {} allocated to agent {}", id, agentId);
        if (id == null) {
            return Optional.empty();
        }

        if (jobUnitRepo.markInProgress(id, agentId) != 1) {
            // If another agent grabbed the same job unit
            LOGGER.warn("Job unit {} is already in progress by an agent", id);
            return Optional.empty();
        }

        String cmd = jobUnitRepo.getResolvedCommand(id);
        return Optional.of(new JobUnitRow(id, cmd));
    }

    @Transactional
    public void markCompleted(String jobUnitId) {
        int result = jobUnitRepo.markCompleted(jobUnitId);
        LOGGER.info("Job unit {} marked as completed with result {}", jobUnitId, result);
    }
}

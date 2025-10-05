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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.agent.connection.service.db.entity.AgentBatchAssignmentEntity;
import org.apache.airavata.agent.connection.service.db.entity.JobBatchEntity;
import org.apache.airavata.agent.connection.service.db.repo.AgentBatchAssignmentRepo;
import org.apache.airavata.agent.connection.service.db.repo.JobBatchRepo;
import org.apache.airavata.agent.connection.service.models.JobBatchSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobBatchHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobBatchHandler.class);

    private final JobBatchWorker jobBatchWorker;
    private final JobBatchRepo jobBatchRepo;
    private final AgentBatchAssignmentRepo agentJobAssignmentRepo;
    private final ObjectMapper objectMapper;

    public JobBatchHandler(JobBatchWorker jobBatchWorker, JobBatchRepo jobBatchRepo, AgentBatchAssignmentRepo agentJobAssignmentRepo, ObjectMapper objectMapper) {
        this.jobBatchWorker = jobBatchWorker;
        this.jobBatchRepo = jobBatchRepo;
        this.agentJobAssignmentRepo = agentJobAssignmentRepo;
        this.objectMapper = objectMapper;
    }

    public String handleJobWorkload(String experimentId, String agentId, JobBatchSpec spec) {

        String batchId = null;
        if (spec != null) {
            if (spec.getApplicationCommand() == null || spec.getApplicationCommand().isBlank()) {
                LOGGER.warn("application_command is required for experiment with id: {}", experimentId);
                throw new IllegalArgumentException("application_command is required for experiment with id: " + experimentId);
            }

            if (spec.getParameterGrid() == null || spec.getParameterGrid().isEmpty()) {
                LOGGER.warn("parameter_grid is required for experiment with id: {}", experimentId);
                throw new IllegalArgumentException("parameter_grid is required for experiment with id: " + experimentId);
            }

            batchId = UUID.randomUUID().toString();
            JobBatchEntity batch = new JobBatchEntity();
            batch.setId(batchId);
            batch.setExperimentId(experimentId);
            batch.setCommandTemplate(spec.getApplicationCommand());
            batch.setPayloadJson(objectMapper.valueToTree(spec));
            jobBatchRepo.save(batch);

            AgentBatchAssignmentEntity assign = new AgentBatchAssignmentEntity();
            assign.setAgentId(agentId);
            assign.setExperimentId(experimentId);
            assign.setBatchId(batchId);
            agentJobAssignmentRepo.save(assign);

            jobBatchWorker.expandAndPersistUnitsAsync(experimentId, batchId, spec.getApplicationCommand(), spec.getParameterGrid());
        }
        return batchId;
    }
}

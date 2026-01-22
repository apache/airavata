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
package org.apache.airavata.orchestrator.internal.workflow;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.orchestrator.ProcessStatusUpdater;
import org.apache.airavata.orchestrator.state.ProcessStateValidator;
import org.apache.airavata.orchestrator.state.StateTransitionService;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry-only process status updates. No pub-sub.
 * Used by workflow/scheduling components (e.g. ReScheduler) instead of publish-based Utils.
 */
@Component
public class ProcessStatusUpdateHelper implements ProcessStatusUpdater {

    private static final Logger logger = LoggerFactory.getLogger(ProcessStatusUpdateHelper.class);

    private final RegistryService registryService;

    public ProcessStatusUpdateHelper(RegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * Validates transition and updates process status in registry only.
     */
    public void updateProcessStatus(String processId, String experimentId, String gatewayId, ProcessState state)
            throws AiravataException {
        try {
            var currentStatus = registryService.getProcessStatus(processId);
            var currentState = currentStatus != null ? currentStatus.getState() : null;
            if (!StateTransitionService.validateAndLog(
                    ProcessStateValidator.INSTANCE, currentState, state, processId, "process")) {
                throw new AiravataException(String.format(
                        "Invalid process state transition rejected: processId=%s, %s -> %s",
                        processId, currentState != null ? currentState.name() : "(initial)", state.name()));
            }
            var status = new ProcessStatus();
            status.setState(state);
            status.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp().getTime());
            registryService.updateProcessStatus(status, processId);
        } catch (RegistryException e) {
            logger.error("Failed to update process status " + processId, e);
            throw new AiravataException("Failed to update process status " + processId, e);
        }
    }

    /**
     * Adds process status in registry only. No transition validation.
     */
    public void addProcessStatus(String processId, String experimentId, String gatewayId, ProcessState state)
            throws AiravataException {
        try {
            var status = new ProcessStatus(state);
            status.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp().getTime());
            registryService.addProcessStatus(status, processId);
        } catch (RegistryException e) {
            logger.error("Failed to add process status " + processId, e);
            throw new AiravataException("Failed to add process status " + processId, e);
        }
    }
}

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
package org.apache.airavata.storage.client.sftp;

import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.exception.TaskFailureException;
import org.apache.airavata.execution.dag.TaskContext;
import org.apache.airavata.protocol.AdapterSupport;
import org.apache.airavata.protocol.AgentAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnParticipant
public class SftpClient {

    private static final Logger logger = LoggerFactory.getLogger(SftpClient.class);

    /**
     * Resolve a storage adapter, using the given override storage resource ID if non-blank,
     * otherwise falling back to the default storage resource from the task context.
     *
     * @param overrideStorageId a specific storage resource ID (e.g. input or output), or null/blank to use default
     * @param label             a label for logging (e.g. "input", "output")
     * @param adapterSupport    the adapter support instance
     * @param taskContext       the current task context
     * @param taskId            the task identifier for error messages
     * @return the resolved storage resource adapter
     * @throws TaskFailureException if the adapter cannot be obtained
     */
    public AgentAdapter resolveStorageAdapter(
            String overrideStorageId,
            String label,
            AdapterSupport adapterSupport,
            TaskContext taskContext,
            String taskId)
            throws TaskFailureException {
        String storageId;
        if (overrideStorageId != null && !overrideStorageId.isBlank()) {
            storageId = overrideStorageId;
            logger.info("Fetching {} storage adapter for storage resource {}", label, storageId);
        } else {
            storageId = taskContext.getComputeResourceId();
            label = "default";
        }
        try {
            return createStorageAdapter(adapterSupport, storageId, label, taskContext.getGatewayId(), taskId);
        } catch (TaskFailureException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to obtain adapter for {} storage resource {} in task {}", label, storageId, taskId, e);
            throw new TaskFailureException(
                    "Failed to obtain adapter for " + label + " storage resource " + storageId + " in task " + taskId,
                    false,
                    e);
        }
    }

    public AgentAdapter getComputeResourceAdapter(AdapterSupport adapterSupport, TaskContext taskContext, String taskId)
            throws TaskFailureException {
        String computeId = null;
        try {
            computeId = taskContext.getComputeResourceId();
            return adapterSupport.fetchAdapter(
                    taskContext.getGatewayId(),
                    computeId,
                    taskContext.getJobSubmissionProtocol(),
                    taskContext.getComputeResourceCredentialToken(),
                    taskContext.getComputeResourceLoginUserName());
        } catch (Exception e) {
            throw new TaskFailureException(
                    "Failed to obtain adapter for compute resource " + computeId + " in task " + taskId, false, e);
        }
    }

    private AgentAdapter createStorageAdapter(
            AdapterSupport adapterSupport, String storageId, String adapterType, String gatewayId, String taskId)
            throws TaskFailureException {
        try {
            var adapter = adapterSupport.fetchStorageAdapter(gatewayId, storageId, null, null);

            if (adapter == null) {
                throw new TaskFailureException(
                        adapterType + " storage resource adapter for " + storageId + " can not be null", true, null);
            }
            return adapter;

        } catch (Exception e) {
            throw new TaskFailureException(
                    "Failed to obtain adapter for " + adapterType.toLowerCase() + " storage resource " + storageId
                            + " in task " + taskId,
                    false,
                    e);
        }
    }
}

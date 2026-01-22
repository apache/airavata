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
package org.apache.airavata.activities.process.post;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.activities.shared.TaskExecutorHelper;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.task.factory.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobVerificationActivity implements WorkflowActivity {

    private static final Logger logger = LoggerFactory.getLogger(JobVerificationActivity.class);

    @Override
    public String run(WorkflowActivityContext ctx) {
        var input = ctx.getInput(BaseActivityInput.class);
        logger.info("JobVerificationActivity for process {}", input.processId());

        try {
            var registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);
            var taskFactory = WorkflowRuntimeHolder.getBean(TaskFactory.class);

            var processModel = registryService.getProcess(input.processId());
            var crId = processModel.getComputeResourceId();
            var grpId = processModel.getGroupResourceProfileId();
            var gcrPref = registryService.getGroupComputeResourcePreference(crId, grpId);
            var factory = taskFactory.getFactory(gcrPref.getResourceType());

            var task = factory.createJobVerificationTask(input.processId());
            return TaskExecutorHelper.executeTask(task, input);
        } catch (Exception e) {
            logger.error("JobVerificationActivity failed for process {}", input.processId(), e);
            throw new RuntimeException("JobVerificationActivity failed: " + e.getMessage(), e);
        }
    }
}

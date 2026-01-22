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
import io.dapr.workflows.client.DaprWorkflowClient;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.task.parsing.ProcessCompletionMessage;
import org.apache.airavata.workflow.process.parsing.ParsingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParsingTriggeringActivity implements WorkflowActivity {

    private static final Logger logger = LoggerFactory.getLogger(ParsingTriggeringActivity.class);

    @Override
    public String run(WorkflowActivityContext ctx) {
        var input = ctx.getInput(BaseActivityInput.class);
        logger.info("ParsingTriggeringActivity for process {}", input.processId());

        try {
            var client = WorkflowRuntimeHolder.getBean(DaprWorkflowClient.class);
            var msg = new ProcessCompletionMessage();
            msg.setProcessId(input.processId());
            msg.setExperimentId(input.experimentId());
            msg.setGatewayId(input.gatewayId());

            var instanceId = client.scheduleNewWorkflow(ParsingWorkflow.class, msg);
            logger.info("Scheduled ParsingWorkflow {} for process {}", instanceId, input.processId());
            return instanceId;
        } catch (Exception e) {
            logger.error("ParsingTriggeringActivity failed for process {}", input.processId(), e);
            throw new RuntimeException("ParsingTriggeringActivity failed: " + e.getMessage(), e);
        }
    }
}

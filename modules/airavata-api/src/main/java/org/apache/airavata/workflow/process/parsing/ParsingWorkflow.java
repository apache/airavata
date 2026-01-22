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
package org.apache.airavata.workflow.process.parsing;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import org.apache.airavata.activities.parsing.DataParsingActivity;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.task.parsing.ProcessCompletionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dapr Workflow for data parsing.
 *
 * <p>This workflow orchestrates the parsing of process output data:
 * 1. Data Parsing (extracts output values from job output files)
 *
 * <p>Replaces the Helix-based parsing workflow in ParserWorkflowManager.
 */
public class ParsingWorkflow implements Workflow {

    private static final Logger logger = LoggerFactory.getLogger(ParsingWorkflow.class);

    @Override
    public WorkflowStub create() {
        return ctx -> {
            ProcessCompletionMessage input = ctx.getInput(ProcessCompletionMessage.class);
            String processId = input.getProcessId();
            String experimentId = input.getExperimentId();
            String gatewayId = input.getGatewayId();

            logger.info(
                    "Starting ParsingWorkflow for process {} of experiment {} in gateway {}",
                    processId,
                    experimentId,
                    gatewayId);

            try {
                // Data Parsing
                BaseActivityInput dataParsingInput =
                        new BaseActivityInput(processId, experimentId, gatewayId, null, false, false, false, false);

                String dataParsingResult = ctx.callActivity(
                                DataParsingActivity.class.getName(), dataParsingInput, String.class)
                        .await();

                logger.info("Data parsing completed for process {}: {}", processId, dataParsingResult);

                String workflowId = ctx.getInstanceId();
                logger.info(
                        "ParsingWorkflow completed successfully for process {} with workflow ID {}",
                        processId,
                        workflowId);

                ctx.complete(workflowId);

            } catch (Exception e) {
                logger.error("ParsingWorkflow failed for process {}: {}", input.getProcessId(), e.getMessage(), e);
                throw new RuntimeException("ParsingWorkflow failed for process " + input.getProcessId(), e);
            }
        };
    }
}

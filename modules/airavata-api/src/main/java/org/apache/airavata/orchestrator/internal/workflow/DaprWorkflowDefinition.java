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

/**
 * Base interface for Dapr workflow definitions.
 *
 * <p>This interface defines the contract for workflow implementations.
 * While full Dapr Workflows integration is pending (see TODOs in workflow managers),
 * this interface establishes the structure for future workflow definitions.
 *
 * <p>Workflow naming should follow conventions defined in {@link WorkflowNaming}.
 *
 * @param <TInput> the input type for the workflow
 * @param <TOutput> the output type for the workflow
 */
public interface DaprWorkflowDefinition<TInput, TOutput> {

    /**
     * Get the workflow name/identifier.
     *
     * @return workflow name following {@link WorkflowNaming} conventions
     */
    String getWorkflowName();

    /**
     * Get the input type class.
     *
     * @return the Class object for the input type
     */
    Class<TInput> getInputType();

    /**
     * Execute the workflow with the given input.
     *
     * @param input the workflow input
     * @return the workflow output
     * @throws Exception if workflow execution fails
     */
    TOutput execute(TInput input) throws Exception;

    /**
     * Handle workflow cancellation.
     * Called when the workflow is cancelled before completion.
     */
    void onCancel();
}

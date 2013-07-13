/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

package org.apache.airavata.gfac.provider;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.HadoopHostType;

import java.util.Map;

public interface GFacProvider {

    void initProperties(Map<String,String> properties) throws GFacProviderException,GFacException;
    /**
     * Initialize environment required for invoking the execute method of the provider. If environment setup is
     * done during the in handler execution, validation of environment will go here.
     * @param jobExecutionContext containing job execution related information.
     * @throws GFacProviderException in case of a error initializing the environment.
     */
    void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException,GFacException;

    /**
     * Invoke the providers intended functionality using information and data in job execution context.
     * @param jobExecutionContext containing job execution related information.
     * @throws GFacProviderException in case of a error executing the job.
     */
    void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException;

    /**
     * Cleans up the acquired resources during initialization and execution of the job.
     * @param jobExecutionContext containing job execution related information.
     * @throws GFacProviderException in case of a error cleaning resources.
     */
    void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException,GFacException;

    /**
     * Cancels all jobs relevant to an experiment.
     * @param experimentId The experiment id
     * @param jobExecutionContext The job execution context, contains runtime information.
     * @throws GFacException If an error occurred while cancelling the job.
     */
    void cancelJob(String experimentId, JobExecutionContext jobExecutionContext) throws GFacException;

    /**
     * Cancels all jobs relevant to a workflow in an experiment.
     * @param experimentId The experiment id
     * @param workflowId The workflow id.
     * @param jobExecutionContext The job execution context, contains runtime information.
     * @throws GFacException If an error occurred while cancelling the job.
     */
    void cancelJob(String experimentId, String workflowId,
                   JobExecutionContext jobExecutionContext) throws GFacException;

    /**
     * Cancels the job for a given a workflow id and node id in an experiment.
     * @param experimentId The experiment id.
     * @param workflowId The workflow id.
     * @param nodeId The node id.
     * @param jobExecutionContext The job execution context relevant to cancel job operation.
     * @throws GFacException If an error occurred while cancelling the job.
     */
    void cancelJob(String experimentId, String workflowId, String nodeId,
                   JobExecutionContext jobExecutionContext) throws GFacException;

}

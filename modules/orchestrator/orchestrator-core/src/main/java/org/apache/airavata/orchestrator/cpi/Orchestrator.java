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
package org.apache.airavata.orchestrator.cpi;

import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.model.ExperimentRequest;
import org.apache.airavata.registry.api.JobRequest;

/*
   This is the interface for orchestrator functionality exposed to the out side of the
   module
*/
public interface Orchestrator {


    /**
     * This method will initialize the Orchestrator, during restart this will
     * get called and do init tasks
     * @return
     * @throws OrchestratorException
     */
    boolean initialize() throws OrchestratorException;


    /**
     *  This method is the very first method which create an entry in
     * database for a given experiment, this return the experiment ID, so
     * user have full control for the experiment
     * @param request
     * @return
     * @throws OrchestratorException
     */
    String createExperiment(ExperimentRequest request) throws OrchestratorException;

    /**
     * After creating the experiment user has the experimentID, then user
     * can create the JobRequest and send the Job input parameters to Orchestrator
     * @param request
     * @return
     * @throws OrchestratorException
     */
    boolean launchExperiment(JobRequest request) throws OrchestratorException;

    /**
     * This method can be used to cancel a running experiment, if job is already finished it
     * throws an exception. If job is not yet submitted it will just change the status to cancelled,
     * if Job is running it will be killed from the resource and make the status to cancelled
     * @param experimentID
     * @return
     * @throws OrchestratorException
     */
    boolean terminateExperiment(String experimentID)throws OrchestratorException;

    /**
     * This is like a cron job which runs continuously and take available jobs to
     * submit to GFAC and submit them to GFAC
     * @throws OrchestratorException
     */
    void startJobSubmitter() throws OrchestratorException;

    /**
     * This method will get called during graceful shutdown of Orchestrator
     * This can be used to handle the shutdown of orchestrator gracefully.
     * @return
     * @throws OrchestratorException
     */
    void shutdown() throws OrchestratorException;

    /**
     * This method can be used to parse the current job data configured in
     * Registry and validate its status, if it has minimum required parameters to
     * submit the job this method returns true otherwise this returns false
     * @param experimentID
     * @return
     */
    boolean validateExperiment(String experimentID);
}

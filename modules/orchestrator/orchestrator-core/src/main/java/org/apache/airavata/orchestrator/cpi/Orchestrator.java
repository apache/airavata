/**
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
 */
package org.apache.airavata.orchestrator.cpi;

import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;

/*
   This is the interface for orchestrator functionality exposed to the out side of the
   module
*/
public interface Orchestrator {

    /**
     * This method can be used to run all custom validators plugged in to the orchestrator and make
     * sure the experiment is ready to launch and if its not this will return false
     *
     * @param experiment
     * @return boolean if the experiment is valid after executing all the validators return true otherwise it will return false
     * @throws OrchestratorException
     */
    ValidationResults validateExperiment(ExperimentModel experiment) throws OrchestratorException, LaunchValidationException;

    /**
     * This method can be used to run all custom validators plugged in to the orchestrator and make
     * sure the experiment is ready to launch and if its not this will return false
     *
     * @param experiment
     * @param processModel
     * @return boolean if the process is valid after executing all the validators return true otherwise it will return false
     * @throws OrchestratorException
     */
    ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel) throws OrchestratorException, LaunchValidationException;

    /**
     * After creating the experiment Data user have the
     * experimentID as the handler to the experiment, during the launchProcess
     * We just have to give the experimentID
     *
     * @param processModel - Process model created for this process.
     * @param tokenId      - token id for this request.
     * @return launchProcess status
     * @throws OrchestratorException
     */
    boolean launchProcess(ProcessModel processModel, String tokenId) throws OrchestratorException;


    /**
     * After creating the experiment Data user have the
     * experimentID as the handler to the experiment, during the launchProcess
     * We just have to give the experimentID
     *
     * @param experiment
     * @param tokenId
     * @throws OrchestratorException
     */
    void cancelExperiment(ExperimentModel experiment, String tokenId) throws OrchestratorException;
    //todo have to add another method to handle failed or jobs to be recovered by orchestrator
    //todo if you don't add these this is not an orchestrator, its just an intemediate component which invoke gfac


    void initialize() throws OrchestratorException;
}

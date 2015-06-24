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

import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;

import java.util.List;

/*
   This is the interface for orchestrator functionality exposed to the out side of the
   module
*/
public interface Orchestrator {

    /**
     * This method can be used to run all custom validators plugged in to the orchestrator and make
     * sure the experiment is ready to launch and if its not this will return false
     * @param experiment
     * @param processModel
     * @return boolean if the experiments are valids after executing all the validators return true otherwise it iwll return false
     * @throws OrchestratorException
     */
     ValidationResults validateExperiment(ExperimentModel experiment, ProcessModel processModel) throws OrchestratorException,LaunchValidationException;
    /**
     * After creating the experiment Data user have the
     * experimentID as the handler to the experiment, during the launchExperiment
     * We just have to give the experimentID
     *
     * @param experiment
     * @return launchExperiment status
     * @throws OrchestratorException
     */
    boolean launchExperiment(ExperimentModel experiment, ProcessModel processModel,String tokenId) throws OrchestratorException;


    /**
     * This method will parseSingleJob the ExperimentConfiguration and based on the configuration
     * we create a single or multiple tasks for the experiment.
     * @param experimentId
     * @return
     * @throws OrchestratorException
     */
    public List<ProcessModel> createProcesses(String experimentId) throws OrchestratorException;

    /**
     * After creating the experiment Data user have the
     * experimentID as the handler to the experiment, during the launchExperiment
     * We just have to give the experimentID
     *
     * @param experiment
     * @param processModel
     * @param tokenId
     * @throws OrchestratorException
     */
    void cancelExperiment(ExperimentModel experiment, ProcessModel processModel, String tokenId) throws OrchestratorException;
    //todo have to add another method to handle failed or jobs to be recovered by orchestrator
    //todo if you don't add these this is not an orchestrator, its just an intemediate component which invoke gfac


    void initialize() throws OrchestratorException;
}

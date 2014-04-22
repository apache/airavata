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

import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;

import java.util.List;

/*
   This is the interface for orchestrator functionality exposed to the out side of the
   module
*/
public interface Orchestrator {

    /**
     * After creating the experiment Data user have the
     * experimentID as the handler to the experiment, during the launchExperiment
     * We just have to give the experimentID
     *
     * @param experimentID
     * @return jobID
     * @throws OrchestratorException
     */
    String launchExperiment(String experimentID, String taskID) throws OrchestratorException;


    /**
     * This method will parse the ExperimentConfiguration and based on the configuration
     * we create a single or multiple tasks for the experiment.
     * @param experimentId
     * @return
     * @throws OrchestratorException
     */
    public List<TaskDetails> createTasks(String experimentId) throws OrchestratorException;

    /**
     * After creating the experiment Data user have the
     * experimentID as the handler to the experiment, during the launchExperiment
     * We just have to give the experimentID
     *
     * @param experimentID
     * @throws OrchestratorException
     */
    void cancelExperiment(String experimentID) throws OrchestratorException;
    //todo have to add another method to handle failed or jobs to be recovered by orchestrator
    //todo if you don't add these this is not an orchestrator, its just an intemediate component which invoke gfac

}

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
package org.apache.airavata.orchestrator.task;

import java.util.List;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.registry.exception.RegistryException;

/**
 * Factory interface for creating orchestration tasks.
 */
public interface TaskFactory {
    /**
     * Creates and saves environment setup tasks for a process.
     */
    List<String> createAndSaveEnvSetupTasks(ProcessModel processModel) throws OrchestratorException, RegistryException;

    /**
     * Creates archive data staging tasks.
     */
    void createArchiveDataStagingTasks(ProcessModel processModel) throws OrchestratorException, RegistryException;

    /**
     * Creates output data staging tasks.
     */
    void createOutputDataStagingTasks(ProcessModel processModel) throws OrchestratorException, RegistryException;

    /**
     * Creates intermediate output data staging tasks.
     */
    void createIntermediateOutputDataStagingTasks(ProcessModel processModel)
            throws OrchestratorException, RegistryException;

    /**
     * Creates and saves job submission tasks.
     */
    List<String> createAndSaveSubmissionTasks(ProcessModel processModel)
            throws OrchestratorException, RegistryException;

    /**
     * Creates monitor tasks.
     */
    void createMonitorTasks(ProcessModel processModel) throws OrchestratorException, RegistryException;
}

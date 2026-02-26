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
package org.apache.airavata.file.server.service;

import java.util.Map;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.protocol.AdapterSupport;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight helper that resolves the SSH adapter and working directory
 * for a given process.  Does not extend any task class — it is only used
 * by the file-server module to browse and transfer files.
 */
public class ProcessDataManager {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDataManager.class);

    private final ProcessModel process;
    private final ExperimentModel experiment;
    private final AdapterSupport adapterSupport;

    public ProcessDataManager(
            ProcessService processService,
            ExperimentService experimentService,
            String processId,
            AdapterSupport adapterSupport)
            throws Exception {
        this.adapterSupport = adapterSupport;
        try {
            this.process = processService.getProcess(processId);
            this.experiment = experimentService.getExperiment(process.getExperimentId());
        } catch (Exception e) {
            logger.error("Failed to initialize ProcessDataManager for process {}", processId, e);
            throw e;
        }
    }

    public AgentAdapter getAgentAdapter() throws Exception {
        String loginUserName = null;
        Map<String, Object> schedule = process.getProcessResourceSchedule();
        if (schedule != null) {
            loginUserName = (String) schedule.get("overrideLoginUserName");
        }
        return adapterSupport.fetchComputeSSHAdapter(
                experiment.getGatewayId(),
                process.getComputeResourceId(),
                process.getComputeResourceCredentialToken(),
                process.getUserName(),
                loginUserName);
    }

    public String getBaseDir() {
        Map<String, Object> schedule = process.getProcessResourceSchedule();
        if (schedule != null) {
            String scratch = (String) schedule.get("overrideScratchLocation");
            if (scratch != null && !scratch.isEmpty()) {
                return scratch;
            }
        }
        return "/tmp";
    }
}

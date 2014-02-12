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
package org.apache.airavata.orchestrator.core.utils;

import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.orchestrator.core.NewJobWorker;
import org.apache.airavata.orchestrator.core.OrchestratorConfiguration;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.schemas.gfac.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * This contains orchestrator specific utilities
 */
public class OrchestratorUtils {
    private final static Logger logger = LoggerFactory.getLogger(OrchestratorUtils.class);

    public static OrchestratorConfiguration loadOrchestratorConfiguration() throws OrchestratorException, IOException {
        //FIXME: (MEP) why are you using the NewJobWorker class to get the properties file here?
        URL resource =
                NewJobWorker.class.getClassLoader().getResource(OrchestratorConstants.ORCHESTRATOR_PROPERTIES);
        if (resource == null) {
            String error = "orchestrator.properties cannot be found, Failed to initialize Orchestrator";
            logger.error(error);
            throw new OrchestratorException(error);
        }
        OrchestratorConfiguration orchestratorConfiguration = new OrchestratorConfiguration();
        Properties orchestratorProps = new Properties();
        orchestratorProps.load(resource.openStream());
        orchestratorConfiguration.setNewJobSubmitterClass((String) orchestratorProps.get(OrchestratorConstants.JOB_SUBMITTER));
        orchestratorConfiguration.setSubmitterInterval(Integer.parseInt((String) orchestratorProps.get(OrchestratorConstants.SUBMIT_INTERVAL)));
        orchestratorConfiguration.setThreadPoolSize(Integer.parseInt((String) orchestratorProps.get(OrchestratorConstants.THREAD_POOL_SIZE)));
        orchestratorConfiguration.setStartSubmitter(Boolean.valueOf(orchestratorProps.getProperty(OrchestratorConstants.START_SUBMITTER)));
        orchestratorConfiguration.setEmbeddedMode(Boolean.valueOf(orchestratorProps.getProperty(OrchestratorConstants.EMBEDDED_MODE)));
        orchestratorConfiguration.setEnableValidation(Boolean.valueOf(orchestratorProps.getProperty(OrchestratorConstants.ENABLE_VALIDATION)));
        return orchestratorConfiguration;
    }


    public static Map<String, Object> getMessageContext(Map<String, String> experimentData,
                                                        Parameter[] parameters) throws OrchestratorException {
        HashMap<String, Object> stringObjectHashMap = new HashMap<String, Object>();

        for (int i = 0; i < parameters.length; i++) {
            String input = experimentData.get(parameters[i].getParameterName());
            if (input != null) {
                stringObjectHashMap.put(parameters[i].getParameterName(), GFacUtils.getInputActualParameter(parameters[i], input));
            } else {
                throw new OrchestratorException("Parameter:" + input + "is missing");
            }
        }
        return stringObjectHashMap;
    }
}

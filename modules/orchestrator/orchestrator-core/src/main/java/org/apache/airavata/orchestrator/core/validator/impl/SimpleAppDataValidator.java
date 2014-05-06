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
package org.apache.airavata.orchestrator.core.validator.impl;

import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAppDataValidator extends AbstractJobMetadataValidator {
    private final static Logger logger = LoggerFactory.getLogger(SimpleAppDataValidator.class);

    private Registry registry;

    public SimpleAppDataValidator() {
        this.registry = RegistryFactory.getDefaultRegistry();
    }

    public boolean runAppSpecificValidation(String experimentID) throws OrchestratorException{
        // implement simple application specific validator to be used for
        // all the applications.
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean validate(String experimentID) throws OrchestratorException {
        boolean result = false;
        if (super.runBasicValidation(experimentID)) {

            Experiment experiment = null;
            try {
                experiment = (Experiment) registry.get(org.apache.airavata.registry.cpi.RegistryModelType.EXPERIMENT, experimentID);
            } catch (Exception e) {
                throw new OrchestratorException(e);
            }
            if (experiment.getUserConfigurationData().isAiravataAutoSchedule()) {
                logger.error("We dont' support auto scheduling at this point, We will simply use user data as it is");
            }

            /* todo like this do more validation and if they are suppose to fail return false otherwise give some
               log messages in server side logs
             */
            if (runAppSpecificValidation(experimentID)) {
                return true;
            }
            String error = "Application data validation steps failed";
            logger.error(error);
            return false;
        }
        String error = "Basic validation steps failed";
        logger.error(error);
        return false;
    }
}

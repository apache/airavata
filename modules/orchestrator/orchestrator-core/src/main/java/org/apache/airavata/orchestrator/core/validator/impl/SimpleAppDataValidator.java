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

import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAppDataValidator extends AbstractJobMetadataValidator {
    private final static Logger logger = LoggerFactory.getLogger(SimpleAppDataValidator.class);

    public boolean runAppSpecificValidation(String experimentID) throws OrchestratorException{
        // implement simple application specific validator to be used for
        // all the applications.
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean validate(String experimentID) throws OrchestratorException {
        boolean result = false;
        if (super.runBasicValidation(experimentID)) {

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

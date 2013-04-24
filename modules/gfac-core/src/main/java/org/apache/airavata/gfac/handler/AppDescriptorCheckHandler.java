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
package org.apache.airavata.gfac.handler;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AppDescriptorCheckHandler implements GFacHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppDescriptorCheckHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        logger.info("Invoking ApplicationDescriptorCheckHandler ...");
        ApplicationDescription app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType appDesc = app.getType();
        if (appDesc.getScratchWorkingDirectory() == null) {
            appDesc.setScratchWorkingDirectory("/tmp");
        }

        /*
        * Working dir
        */
        if (appDesc.getStaticWorkingDirectory() == null || "null".equals(appDesc.getStaticWorkingDirectory())) {
            String date = new Date().toString();
            date = date.replaceAll(" ", "_");
            date = date.replaceAll(":", "_");

            String tmpDir = appDesc.getScratchWorkingDirectory() + File.separator
                    + jobExecutionContext.getServiceName() + "_" + date + "_" + UUID.randomUUID();

            appDesc.setStaticWorkingDirectory(tmpDir);
        }

        /*
        * Input and Output Directory
        */
        if (appDesc.getInputDataDirectory() == null || "".equals(appDesc.getInputDataDirectory())) {
            appDesc.setInputDataDirectory(appDesc.getStaticWorkingDirectory() + File.separator + "inputData");
        }
        if (appDesc.getOutputDataDirectory() == null || "".equals(appDesc.getOutputDataDirectory())) {
            appDesc.setOutputDataDirectory(appDesc.getStaticWorkingDirectory() + File.separator + "outputData");
        }

        /*
        * Stdout and Stderr for Shell
        */
        if (appDesc.getStandardOutput() == null || "".equals(appDesc.getStandardOutput())) {
            appDesc.setStandardOutput(appDesc.getStaticWorkingDirectory() + File.separator
                    + appDesc.getApplicationName().getStringValue() + ".stdout");
        }
        if (appDesc.getStandardError() == null || "".equals(appDesc.getStandardError())) {
            appDesc.setStandardError(appDesc.getStaticWorkingDirectory() + File.separator
                    + appDesc.getApplicationName().getStringValue() + ".stderr");
        }
        jobExecutionContext.getApplicationContext().setApplicationDeploymentDescription(app);
    }

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}

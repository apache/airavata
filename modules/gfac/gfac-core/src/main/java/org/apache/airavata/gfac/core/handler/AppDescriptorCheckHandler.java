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
package org.apache.airavata.gfac.core.handler;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.states.GfacPluginState;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class AppDescriptorCheckHandler implements GFacRecoverableHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppDescriptorCheckHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        logger.info("Invoking ApplicationDescriptorCheckHandler ...");
        try {
            GFacUtils.updatePluginState(jobExecutionContext.getZk(), jobExecutionContext, this.getClass().getName(), GfacPluginState.INVOKED);
        } catch (Exception e) {
            logger.info("Error saving plugin status to ZK");
        }
        StringBuffer data = new StringBuffer();
        ApplicationDescription app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType appDesc = app.getType();

        if (appDesc.getScratchWorkingDirectory() == null) {
            appDesc.setScratchWorkingDirectory("/tmp");
        }
        /*
        * Working dir
        */
        if (appDesc.getStaticWorkingDirectory() == null || "null".equals(appDesc.getStaticWorkingDirectory())) {
            String tmpDir = appDesc.getScratchWorkingDirectory() + File.separator
                    + jobExecutionContext.getExperimentID();

            appDesc.setStaticWorkingDirectory(tmpDir);
        }
        data.append(appDesc.getScratchWorkingDirectory());
        data.append(",").append(appDesc.getStaticWorkingDirectory());
        //FIXME: Move this input/output to application descrpitor
        /*
        * Input and Output Directory
        */
        if (appDesc.getInputDataDirectory() == null || "".equals(appDesc.getInputDataDirectory())) {
            appDesc.setInputDataDirectory(appDesc.getStaticWorkingDirectory() + File.separator + Constants.INPUT_DATA_DIR_VAR_NAME);
        }
        if (appDesc.getOutputDataDirectory() == null || "".equals(appDesc.getOutputDataDirectory())) {
            appDesc.setOutputDataDirectory(appDesc.getStaticWorkingDirectory() + File.separator + Constants.OUTPUT_DATA_DIR_VAR_NAME);
        }

        data.append(",").append(appDesc.getInputDataDirectory()).append(",").append(appDesc.getOutputDataDirectory());
        /*
        * Stdout and Stderr for Shell
        */
        if (appDesc.getStandardOutput() == null || "".equals(appDesc.getStandardOutput())) {
            appDesc.setStandardOutput(appDesc.getStaticWorkingDirectory() + File.separator
                    + appDesc.getApplicationName().getStringValue().replaceAll("\\s+","") + ".stdout");
        }
        if (appDesc.getStandardError() == null || "".equals(appDesc.getStandardError())) {
            appDesc.setStandardError(appDesc.getStaticWorkingDirectory() + File.separator
                    + appDesc.getApplicationName().getStringValue().replaceAll("\\s+","") + ".stderr");
        }
        data.append(",").append(appDesc.getStandardOutput()).append(",").append(appDesc.getStandardError());


        logger.info("Recoverable data is saving to zk: " + data.toString());
        GFacUtils.savePluginData(jobExecutionContext, data,this.getClass().getName());
    }



    public void initProperties(Properties properties) throws GFacHandlerException {

    }

    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        ApplicationDescription app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType appDesc = app.getType();
        try {
            String s = GFacUtils.getPluginData(jobExecutionContext, this.getClass().getName());
            String[] split = s.split(",");                   // this is ugly code but nobody else is saving or reading this data, so this is the fastest way
            appDesc.setScratchWorkingDirectory(split[0]);
            appDesc.setStaticWorkingDirectory(split[1]);
            appDesc.setInputDataDirectory(split[2]);
            appDesc.setOutputDataDirectory(split[3]);
            appDesc.setStandardOutput(split[4]);
            appDesc.setStandardError(split[5]);
        } catch (Exception e) {
            throw new GFacHandlerException(e);
        }
    }
}

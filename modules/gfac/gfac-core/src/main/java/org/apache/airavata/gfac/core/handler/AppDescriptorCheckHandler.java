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

import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.states.GfacHandlerState;
import org.apache.airavata.gfac.core.GFacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class AppDescriptorCheckHandler implements GFacHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppDescriptorCheckHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        logger.info("Invoking ApplicationDescriptorCheckHandler ...");
        try {
            GFacUtils.updateHandlerState(jobExecutionContext.getCuratorClient(), jobExecutionContext, this.getClass().getName(), GfacHandlerState.INVOKED);
        } catch (Exception e) {
            logger.info("Error saving plugin status to ZK");
        }
        StringBuffer data = new StringBuffer();

        data.append(jobExecutionContext.getScratchLocation());
        data.append(",").append(jobExecutionContext.getWorkingDir());

        /*
        * Input and Output Directory
        */
        data.append(",").append(jobExecutionContext.getInputDir()).append(",").append(jobExecutionContext.getOutputDir());

        /*
        * Stdout and Stderr for Shell
        */
        data.append(",").append(jobExecutionContext.getStandardOutput()).append(",").append(jobExecutionContext.getStandardError());


        logger.info("Recoverable data is saving to zk: " + data.toString());
        GFacUtils.saveHandlerData(jobExecutionContext, data, this.getClass().getName());
    }



    public void initProperties(Properties properties) throws GFacHandlerException {

    }

    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        try {
            String s = GFacUtils.getHandlerData(jobExecutionContext, this.getClass().getName());
            String[] split = s.split(",");                   // this is ugly code but nobody else is saving or reading this data, so this is the fastest way
            jobExecutionContext.getApplicationContext().getComputeResourcePreference().setScratchLocation(split[0]);
            jobExecutionContext.setWorkingDir(split[1]);
            jobExecutionContext.setInputDir(split[2]);
            jobExecutionContext.setOutputDir(split[3]);
            jobExecutionContext.setStandardOutput(split[4]);
            jobExecutionContext.setStandardError(split[5]);
        } catch (Exception e) {
            throw new GFacHandlerException(e);
        }
    }
}

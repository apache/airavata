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

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.handler.GFacHandler;
import org.apache.airavata.gfac.handler.GFacHandlerException;
import org.apache.airavata.gfac.provider.utils.HadoopUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.HadoopApplicationDeploymentDescriptionType;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class HDFSDataMovementHandler implements GFacHandler {
    private static final Logger logger = LoggerFactory.getLogger(HDFSDataMovementHandler.class);

    private boolean isWhirrBasedDeployment = false;
    private File hadoopConfigDir;

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        MessageContext inMessageContext = jobExecutionContext.getInMessageContext();
        if(inMessageContext.getParameter("HADOOP_DEPLOYMENT_TYPE").equals("WHIRR")){
            isWhirrBasedDeployment = true;
        } else {
            String hadoopConfigDirPath = (String)inMessageContext.getParameter("HADOOP_CONFIG_DIR");
            File hadoopConfigDir = new File(hadoopConfigDirPath);
            if (!hadoopConfigDir.exists()){
                throw new GFacHandlerException("Specified hadoop configuration directory doesn't exist.");
            } else if (FileUtils.listFiles(hadoopConfigDir, null, null).size() <= 0){
                throw new GFacHandlerException("Cannot find any hadoop configuration files inside specified directory.");
            }

            this.hadoopConfigDir = hadoopConfigDir;
        }

        if(jobExecutionContext.isInPath()){
            try {
                handleInPath(jobExecutionContext);
            } catch (IOException e) {
                throw new GFacHandlerException("Error while copying input data from local file system to HDFS.",e);
            }
        } else {
            handleOutPath(jobExecutionContext);
        }
    }

    private void handleInPath(JobExecutionContext jobExecutionContext) throws GFacHandlerException, IOException {
        ApplicationDeploymentDescriptionType appDepDesc =
                jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
        HadoopApplicationDeploymentDescriptionType hadoopAppDesc =
                (HadoopApplicationDeploymentDescriptionType)appDepDesc;
        if(appDepDesc.isSetInputDataDirectory() && isInputDataDirectoryLocal(appDepDesc)){
            Configuration hadoopConf = HadoopUtils.createHadoopConfiguration(jobExecutionContext, isWhirrBasedDeployment, hadoopConfigDir);
            FileSystem hdfs = FileSystem.get(hadoopConf);
            hdfs.copyFromLocalFile(new Path(appDepDesc.getInputDataDirectory()),
                    new Path(hadoopAppDesc.getHadoopJobConfiguration().getHdfsInputDirectory()));
        }
    }

    private boolean isInputDataDirectoryLocal(ApplicationDeploymentDescriptionType appDepDesc){
        String inputDataDirectoryPath = appDepDesc.getInputDataDirectory();
        File inputDataDirectory = new File(inputDataDirectoryPath);
        if(inputDataDirectory.exists() && FileUtils.listFiles(inputDataDirectory, null, null).size() > 0){
            return true;
        }

        return false;
    }

    private void handleOutPath(JobExecutionContext jobExecutionContext){}

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}
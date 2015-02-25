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
package org.apache.airavata.gsi.ssh.api.job;

import org.apache.airavata.gsi.ssh.impl.RawCommandInfo;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LSFJobConfiguration implements JobManagerConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(LSFJobConfiguration.class);

    private String jobDescriptionTemplateName;

    private String scriptExtension;

    private String installedPath;

    private OutputParser parser;

    public LSFJobConfiguration(){
        // this can be used to construct and use setter methods to set all the params in order
    }
    public LSFJobConfiguration(String jobDescriptionTemplateName,
                                 String scriptExtension,String installedPath,OutputParser parser) {
        this.jobDescriptionTemplateName = jobDescriptionTemplateName;
        this.scriptExtension = scriptExtension;
        this.parser = parser;
        if (installedPath.endsWith("/") || installedPath.isEmpty()) {
            this.installedPath = installedPath;
        } else {
            this.installedPath = installedPath + "/";
        }
    }

    @Override
    public RawCommandInfo getCancelCommand(String jobID) {
        return new RawCommandInfo(this.installedPath + "bkill " + jobID);
    }

    @Override
    public String getJobDescriptionTemplateName() {
        return jobDescriptionTemplateName;
    }

    @Override
    public RawCommandInfo getMonitorCommand(String jobID) {
        return new RawCommandInfo(this.installedPath + "bjobs " + jobID);
    }

    @Override
    public RawCommandInfo getUserBasedMonitorCommand(String userName) {
        return new RawCommandInfo(this.installedPath + "bjobs -u " + userName);
    }

    @Override
    public String getScriptExtension() {
        return scriptExtension;
    }

    @Override
    public RawCommandInfo getSubmitCommand(String workingDirectory, String pbsFilePath) {
        return new RawCommandInfo(this.installedPath + "bsub <" +
                workingDirectory + File.separator + FilenameUtils.getName(pbsFilePath));
    }

    @Override
    public OutputParser getParser() {
        return parser;
    }

    public void setParser(OutputParser parser) {
        this.parser = parser;
    }

    @Override
    public String getInstalledPath() {
        return installedPath;
    }
}

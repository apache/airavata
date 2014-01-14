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
package org.apache.airavata.registry.api;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JobRequest {
    private final static Logger logger = LoggerFactory.getLogger(JobRequest.class);

    private String userName;

    private String systemExperimentID;

    private String userExperimentID;

    private HostDescription hostDescription;

    private ApplicationDescription applicationDescription;

    private ServiceDescription serviceDescription;

    private Map<String,Object> inputParameters;

    private Map<String,Object> outputParameters;

    private ContextHeaderDocument.ContextHeader contextHeader;

    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, Object> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(Map<String, Object> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public Map<String, Object> getOutputParameters() {
        return outputParameters;
    }

    public void setOutputParameters(Map<String, Object> outputParameters) {
        this.outputParameters = outputParameters;
    }

    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public HostDescription getHostDescription() {
        return hostDescription;
    }

    public void setHostDescription(HostDescription hostDescription) {
        this.hostDescription = hostDescription;
    }

    public ApplicationDescription getApplicationDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(ApplicationDescription applicationDescription) {
        this.applicationDescription = applicationDescription;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSystemExperimentID() {
        return systemExperimentID;
    }

    public void setSystemExperimentID(String systemExperimentID) {
        this.systemExperimentID = systemExperimentID;
    }

    public String getUserExperimentID() {
        return userExperimentID;
    }

    public void setUserExperimentID(String userExperimentID) {
        this.userExperimentID = userExperimentID;
    }

    public ContextHeaderDocument.ContextHeader getContextHeader() {
        return contextHeader;
    }

    public void setContextHeader(ContextHeaderDocument.ContextHeader contextHeader) {
        this.contextHeader = contextHeader;
    }
}

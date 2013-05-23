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

package org.apache.airavata.xbaya.model.registrybrowser;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
//import org.apache.airavata.registry.api.AiravataRegistry2;

public class ApplicationDeploymentDescriptionWrap {
    private ApplicationDescription applicationDescription;
    private String service;
    private String host;
//    private AiravataRegistry2 registry;
    private AiravataAPI airavataAPI;

    public ApplicationDeploymentDescriptionWrap(AiravataAPI airavataAPI,
            ApplicationDescription applicationDescription, String service, String host) {
        setApplicationDescription(applicationDescription);
        setService(service);
        setHost(host);
        setAiravataAPI(airavataAPI);
    }

    public ApplicationDescription getDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(ApplicationDescription applicationDescription) {
        this.applicationDescription = applicationDescription;
    }

    public String getService() {
        return service;
    }

    public ServiceDescription getServiceDescription() throws AiravataAPIInvocationException {
        ServiceDescription desc = getAiravataAPI().getApplicationManager().getServiceDescription(getService());
        if(desc!=null){
        	return desc;
        }
        throw new AiravataAPIInvocationException(new Exception("Service Description not found in registry."));
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHost() {
        return host;
    }

    public HostDescription getHostDescription() throws AiravataAPIInvocationException {
        return getAiravataAPI().getApplicationManager().getHostDescription(getHost());
    }

    public void setHost(String host) {
        this.host = host;
    }

//    public AiravataRegistry2 getRegistry() {
//        return registry;
//    }
//
//    public void setRegistry(AiravataRegistry2 registry) {
//        this.registry = registry;
//    }


    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }
}

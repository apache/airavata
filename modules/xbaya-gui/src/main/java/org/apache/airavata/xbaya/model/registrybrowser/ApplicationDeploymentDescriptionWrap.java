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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataRegistry;

public class ApplicationDeploymentDescriptionWrap {
    private ApplicationDeploymentDescription applicationDeploymentDescription;
    private String service;
    private String host;
    private AiravataRegistry registry;

    public ApplicationDeploymentDescriptionWrap(AiravataRegistry registry,
            ApplicationDeploymentDescription applicationDeploymentDescription, String service, String host) {
        setApplicationDeploymentDescription(applicationDeploymentDescription);
        setService(service);
        setHost(host);
        setRegistry(registry);
    }

    public ApplicationDeploymentDescription getDescription() {
        return applicationDeploymentDescription;
    }

    public void setApplicationDeploymentDescription(ApplicationDeploymentDescription applicationDeploymentDescription) {
        this.applicationDeploymentDescription = applicationDeploymentDescription;
    }

    public String getService() {
        return service;
    }

    public ServiceDescription getServiceDescription() throws RegistryException{
        return getRegistry().getServiceDescription(getService());
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHost() {
        return host;
    }

    public HostDescription getHostDescription() throws RegistryException{
        return getRegistry().getHostDescription(getHost());
    }

    public void setHost(String host) {
        this.host = host;
    }

    public AiravataRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry registry) {
        this.registry = registry;
    }
}

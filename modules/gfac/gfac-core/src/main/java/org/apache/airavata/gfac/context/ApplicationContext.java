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

package org.apache.airavata.gfac.context;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

public class ApplicationContext extends AbstractContext {

    private ApplicationDescription applicationDeploymentDescription;
    private ServiceDescription serviceDescription;
    private HostDescription hostDescription;

    public ApplicationDescription getApplicationDeploymentDescription() {
        return applicationDeploymentDescription;
    }

    public <T extends ApplicationDescription> void setApplicationDeploymentDescription(T applicationDeploymentDescription) {
        this.applicationDeploymentDescription = applicationDeploymentDescription;
    }

    public <T extends ServiceDescription> void setServiceDescription(T serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public <T extends HostDescription> void setHostDescription(T hostDescription) {
        this.hostDescription = hostDescription;
    }

    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    public HostDescription getHostDescription() {
        return hostDescription;
    }
}

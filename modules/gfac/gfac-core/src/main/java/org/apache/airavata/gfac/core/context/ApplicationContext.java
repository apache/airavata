/**
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
 */
package org.apache.airavata.gfac.core.context;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;

public class ApplicationContext extends AbstractContext {
    private ApplicationDeploymentDescription applicationDeploymentDescription;
    private ComputeResourceDescription computeResourceDescription;
    private ApplicationInterfaceDescription applicationInterfaceDescription;
    private ComputeResourcePreference computeResourcePreference;

    public ApplicationDeploymentDescription getApplicationDeploymentDescription() {
        return applicationDeploymentDescription;
    }

    public void setApplicationDeploymentDescription(ApplicationDeploymentDescription applicationDeploymentDescription) {
        this.applicationDeploymentDescription = applicationDeploymentDescription;
    }

    public ComputeResourceDescription getComputeResourceDescription() {
        return computeResourceDescription;
    }

    public void setComputeResourceDescription(ComputeResourceDescription computeResourceDescription) {
        this.computeResourceDescription = computeResourceDescription;
    }

    public ApplicationInterfaceDescription getApplicationInterfaceDescription() {
        return applicationInterfaceDescription;
    }

    public void setApplicationInterfaceDescription(ApplicationInterfaceDescription applicationInterfaceDescription) {
        this.applicationInterfaceDescription = applicationInterfaceDescription;
    }

    public ComputeResourcePreference getComputeResourcePreference() {
        return computeResourcePreference;
    }

    public void setComputeResourcePreference(ComputeResourcePreference computeResourcePreference) {
        this.computeResourcePreference = computeResourcePreference;
    }
}

/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.service;

import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Spring-managed implementation of {@link RegistryProvider} that
 * delegates to {@link RegistryServerHandler}.
 */
@Component
@Primary
public class RegistryProviderImpl implements RegistryProvider {

    @Override
    public Gateway getGateway(String gatewayId) throws Exception {
        return new RegistryServerHandler().getGateway(gatewayId);
    }

    @Override
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayId) throws Exception {
        return new RegistryServerHandler().getGatewayResourceProfile(gatewayId);
    }

    @Override
    public boolean isGatewayGroupsExists(String gatewayId) throws Exception {
        return new RegistryServerHandler().isGatewayGroupsExists(gatewayId);
    }

    @Override
    public GatewayGroups getGatewayGroups(String gatewayId) throws Exception {
        return new RegistryServerHandler().getGatewayGroups(gatewayId);
    }

    @Override
    public void createGatewayGroups(GatewayGroups gatewayGroups) throws Exception {
        new RegistryServerHandler().createGatewayGroups(gatewayGroups);
    }

    @Override
    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayId, String computeResourceId)
            throws Exception {
        return new RegistryServerHandler().getGatewayComputeResourcePreference(gatewayId, computeResourceId);
    }

    @Override
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws Exception {
        return new RegistryServerHandler().getComputeResource(computeResourceId);
    }

    @Override
    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws Exception {
        return new RegistryServerHandler().getSSHJobSubmission(jobSubmissionId);
    }
}

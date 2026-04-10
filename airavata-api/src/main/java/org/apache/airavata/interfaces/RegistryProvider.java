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
package org.apache.airavata.interfaces;

import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.workspace.proto.Gateway;

/**
 * SPI contract for registry operations required by the security module.
 *
 * <p>This interface decouples the profile/security module from the compute-service's
 * {@code RegistryServerHandler} implementation, avoiding circular Maven dependencies.
 * Implementations are expected to be provided by the compute module and discovered
 * at runtime via Spring dependency injection.
 */
public interface RegistryProvider {

    Gateway getGateway(String gatewayId) throws Exception;

    GatewayResourceProfile getGatewayResourceProfile(String gatewayId) throws Exception;

    boolean isGatewayGroupsExists(String gatewayId) throws Exception;

    GatewayGroups getGatewayGroups(String gatewayId) throws Exception;

    void createGatewayGroups(GatewayGroups gatewayGroups) throws Exception;

    ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayId, String computeResourceId)
            throws Exception;

    ComputeResourceDescription getComputeResource(String computeResourceId) throws Exception;

    SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws Exception;
}

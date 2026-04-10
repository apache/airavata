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

import java.util.Map;
import org.apache.airavata.model.appcatalog.computeresource.proto.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;

/**
 * SPI contract for compute resource operations required by the execution engine.
 *
 * <p>This interface decouples the execution module from the compute module's repository
 * and service implementations. Implementations are expected to be provided by the compute
 * module and injected into execution components.
 */
public interface ComputeResourceProvider {

    /**
     * Retrieve a compute resource description by its identifier.
     *
     * @param computeResourceId the unique identifier of the compute resource
     * @return the compute resource description
     * @throws Exception if the resource cannot be found or a data access error occurs
     */
    ComputeResourceDescription getComputeResource(String computeResourceId) throws Exception;

    /**
     * Retrieve all registered compute resource names.
     *
     * @return a map of compute resource id to hostname
     * @throws Exception if a data access error occurs
     */
    Map<String, String> getAllComputeResourceNames() throws Exception;

    /**
     * Retrieve a LOCAL job submission configuration.
     *
     * @param jobSubmissionId the job submission interface identifier
     * @return the local submission description
     * @throws Exception if not found or a data access error occurs
     */
    LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws Exception;

    /**
     * Retrieve an SSH job submission configuration.
     *
     * @param jobSubmissionId the job submission interface identifier
     * @return the SSH submission description
     * @throws Exception if not found or a data access error occurs
     */
    SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws Exception;

    /**
     * Retrieve a UNICORE job submission configuration.
     *
     * @param jobSubmissionId the job submission interface identifier
     * @return the UNICORE submission description
     * @throws Exception if not found or a data access error occurs
     */
    UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws Exception;

    /**
     * Retrieve a cloud job submission configuration.
     *
     * @param jobSubmissionId the job submission interface identifier
     * @return the cloud submission description
     * @throws Exception if not found or a data access error occurs
     */
    CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws Exception;

    /**
     * Retrieve a resource job manager by its identifier.
     *
     * @param resourceJobManagerId the resource job manager identifier
     * @return the resource job manager
     * @throws Exception if not found or a data access error occurs
     */
    ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws Exception;

    /**
     * Retrieve the gateway resource profile for a given gateway.
     *
     * @param gatewayId the gateway identifier
     * @return the gateway resource profile
     * @throws Exception if not found or a data access error occurs
     */
    GatewayResourceProfile getGatewayResourceProfile(String gatewayId) throws Exception;

    /**
     * Retrieve the group compute resource preference for a specific compute resource
     * within a group resource profile.
     *
     * @param computeResourceId      the compute resource identifier
     * @param groupResourceProfileId the group resource profile identifier
     * @return the group compute resource preference
     * @throws Exception if not found or a data access error occurs
     */
    GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws Exception;
}

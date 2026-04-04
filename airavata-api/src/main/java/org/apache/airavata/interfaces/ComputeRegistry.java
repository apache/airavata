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
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.UnicoreJobSubmission;

/**
 * Registry operations for compute resources and job submissions.
 */
public interface ComputeRegistry {

    ComputeResourceDescription getComputeResource(String computeResourceId) throws Exception;

    LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws Exception;

    SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws Exception;

    UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws Exception;

    CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws Exception;

    Map<String, String> getAllComputeResourceNames() throws Exception;
}

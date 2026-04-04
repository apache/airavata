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

import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;

/**
 * SPI contract for obtaining job manager configurations and resource job managers.
 *
 * <p>This decouples execution tasks from the compute module's JobFactory implementation.
 * Implementations are provided by the compute module.
 *
 * @deprecated No implementation exists. Slated for removal during gRPC migration.
 */
@Deprecated(forRemoval = true)
public interface JobConfigurationProvider {

    /**
     * Resolve the resource job manager for a given submission protocol and interface.
     */
    ResourceJobManager getResourceJobManager(
            JobSubmissionProtocol submissionProtocol, JobSubmissionInterface jobSubmissionInterface) throws Exception;

    /**
     * Create a job manager configuration for the given resource job manager.
     */
    JobManagerConfiguration getJobManagerConfiguration(ResourceJobManager resourceJobManager) throws Exception;
}

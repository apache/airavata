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
package org.apache.airavata.server.config;

import org.apache.airavata.agent.service.AiravataFileService;
import org.apache.airavata.compute.service.GroupResourceProfileService;
import org.apache.airavata.research.service.AgentExperimentService;
import org.apache.airavata.research.service.ExperimentService;
import org.springframework.context.annotation.Configuration;

/**
 * Wires cross-module dependencies that cannot be expressed as direct Maven
 * dependencies without creating cycles (e.g. execution -> compute-service).
 */
@Configuration(proxyBeanMethods = false)
public class ServiceWiringConfig {

    public ServiceWiringConfig(
            ExperimentService experimentService,
            GroupResourceProfileService groupResourceProfileService,
            AiravataFileService airavataFileService,
            AgentExperimentService agentExperimentService) {
        experimentService.setGroupResourceProfileListProvider(groupResourceProfileService::getGroupResourceList);
        airavataFileService.setUserExperimentIdsProvider(agentExperimentService::getUserExperimentIDs);
    }
}

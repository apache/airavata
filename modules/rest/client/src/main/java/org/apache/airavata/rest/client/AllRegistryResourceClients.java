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

package org.apache.airavata.rest.client;

public class AllRegistryResourceClients {
    private BasicRegistryResourceClient basicRegistryResourceClient = new BasicRegistryResourceClient();
    private ConfigurationResourceClient configurationResourceClient = new ConfigurationResourceClient();
    private DescriptorResourceClient descriptorResourceClient = new DescriptorResourceClient();
    private ExperimentResourceClient experimentResourceClient = new ExperimentResourceClient();
    private ProjectResourceClient projectResourceClient = new ProjectResourceClient();
    private ProvenanceResourceClient provenanceResourceClient = new ProvenanceResourceClient();
    private PublishedWorkflowResourceClient publishedWorkflowResourceClient = new PublishedWorkflowResourceClient();
    private UserWorkflowResourceClient userWorkflowResourceClient = new UserWorkflowResourceClient();

    public BasicRegistryResourceClient getBasicRegistryResourceClient() {
        return basicRegistryResourceClient;
    }

    public ConfigurationResourceClient getConfigurationResourceClient() {
        return configurationResourceClient;
    }

    public DescriptorResourceClient getDescriptorResourceClient() {
        return descriptorResourceClient;
    }

    public ExperimentResourceClient getExperimentResourceClient() {
        return experimentResourceClient;
    }

    public ProjectResourceClient getProjectResourceClient() {
        return projectResourceClient;
    }

    public ProvenanceResourceClient getProvenanceResourceClient() {
        return provenanceResourceClient;
    }

    public PublishedWorkflowResourceClient getPublishedWorkflowResourceClient() {
        return publishedWorkflowResourceClient;
    }

    public UserWorkflowResourceClient getUserWorkflowResourceClient() {
        return userWorkflowResourceClient;
    }
}

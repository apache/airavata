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
package org.apache.airavata.service.integration;

import java.util.UUID;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;

/**
 * Test fixtures for common test scenarios.
 * Provides pre-configured test data setups for integration tests.
 */
public class TestFixtures {

    /**
     * Creates and persists a complete test gateway setup:
     * - Gateway
     * - User Profile
     * - Project
     *
     * @param gatewayService Gateway service
     * @param projectService Project service
     * @return TestGatewaySetup containing all created entity IDs
     */
    public static TestGatewaySetup createGatewaySetup(GatewayService gatewayService, ProjectService projectService)
            throws RegistryException {
        TestGatewaySetup setup = new TestGatewaySetup();

        // Create Gateway
        Gateway gateway = TestDataFactory.createTestGatewayWithDefaults();
        setup.gatewayId = gatewayService.addGateway(gateway);

        // Create Project
        Project project = TestDataFactory.createTestProjectWithDefaults(setup.gatewayId);
        setup.projectId = projectService.addProject(project, setup.gatewayId);

        return setup;
    }

    /**
     * Test gateway setup containing all entity IDs.
     */
    public static class TestGatewaySetup {
        public String gatewayId;
        public String projectId;
    }

    /**
     * Creates a test experiment hierarchy:
     * Gateway -> Project -> Experiment
     *
     * @param gatewayService Gateway service
     * @param projectService Project service
     * @param experimentService Experiment service
     * @return TestExperimentSetup containing all created entity IDs
     */
    public static TestExperimentSetup createExperimentSetup(
            GatewayService gatewayService, ProjectService projectService, ExperimentService experimentService)
            throws RegistryException {
        TestExperimentSetup setup = new TestExperimentSetup();

        // Create Gateway
        Gateway gateway = TestDataFactory.createTestGatewayWithDefaults();
        setup.gatewayId = gatewayService.addGateway(gateway);

        // Create Project
        Project project = TestDataFactory.createTestProjectWithDefaults(setup.gatewayId);
        setup.projectId = projectService.addProject(project, setup.gatewayId);

        // Create Experiment
        var experiment = TestDataFactory.createTestExperimentWithDefaults(setup.projectId, setup.gatewayId);
        setup.experimentId = experimentService.addExperiment(experiment);

        return setup;
    }

    /**
     * Test experiment setup containing all entity IDs.
     */
    public static class TestExperimentSetup {
        public String gatewayId;
        public String projectId;
        public String experimentId;
    }

    /**
     * Generates a unique test ID with optional prefix.
     */
    public static String generateTestId(String prefix) {
        return (prefix != null ? prefix + "-" : "") + UUID.randomUUID().toString();
    }

    /**
     * Generates a unique test ID.
     */
    public static String generateTestId() {
        return generateTestId(null);
    }
}

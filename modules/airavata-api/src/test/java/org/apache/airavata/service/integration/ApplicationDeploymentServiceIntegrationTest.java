/**
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.CommandObject;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.apache.airavata.common.model.SetEnvPaths;
import org.apache.airavata.registry.services.ApplicationDeploymentService;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.service.registry.RegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestConstructor;

/**
 * Comprehensive integration tests for ApplicationDeploymentService.
 *
 * <p>Tests cover:
 * - CRUD operations for application deployments
 * - Module load commands
 * - Pre/post job commands
 * - Environment variables
 * - Library paths
 * - Filtering and searching
 * - Accessibility queries
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisplayName("ApplicationDeploymentService Integration Tests")
public class ApplicationDeploymentServiceIntegrationTest extends ServiceIntegrationTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeploymentServiceIntegrationTest.class);

    private final ApplicationDeploymentService deploymentService;
    private final ComputeResourceService computeResourceService;
    private final RegistryService registryService;

    private String testComputeResourceId;
    private String testAppModuleId;

    public ApplicationDeploymentServiceIntegrationTest(
            ApplicationDeploymentService deploymentService,
            ComputeResourceService computeResourceService,
            RegistryService registryService) {
        this.deploymentService = deploymentService;
        this.computeResourceService = computeResourceService;
        this.registryService = registryService;
    }

    @BeforeEach
    void setupTestData() throws Exception {
        // Create a test compute resource
        ComputeResourceDescription computeResource = new ComputeResourceDescription();
        computeResource.setHostName("test-cluster-" + UUID.randomUUID().toString().substring(0, 8));
        computeResource.setResourceDescription("Test compute resource for deployment tests");
        computeResource.setMaxMemoryPerNode(32768);
        testComputeResourceId = computeResourceService.addComputeResource(computeResource);
        logger.info("Created test compute resource: {}", testComputeResourceId);

        // Create a test application module
        ApplicationModule appModule = new ApplicationModule();
        appModule.setAppModuleName("TestApp-" + UUID.randomUUID().toString().substring(0, 8));
        appModule.setAppModuleVersion("1.0");
        appModule.setAppModuleDescription("Test application module");
        testAppModuleId = registryService.registerApplicationModule(TEST_GATEWAY_ID, appModule);
        logger.info("Created test application module: {}", testAppModuleId);
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCRUDTests {

        @Test
        @DisplayName("Should create application deployment")
        void shouldCreateApplicationDeployment() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);

            // Then
            assertNotNull(deploymentId, "Deployment ID should be generated");
            logger.info("Created deployment: {}", deploymentId);

            // Verify
            assertTrue(deploymentService.isAppDeploymentExists(deploymentId));
        }

        @Test
        @DisplayName("Should retrieve application deployment")
        void shouldRetrieveApplicationDeployment() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            deployment.setAppDeploymentDescription("Retrieval test deployment");
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);

            // When
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertNotNull(retrieved, "Retrieved deployment should not be null");
            assertEquals(deploymentId, retrieved.getAppDeploymentId());
            assertEquals(testAppModuleId, retrieved.getAppModuleId());
            assertEquals(testComputeResourceId, retrieved.getComputeHostId());
            assertEquals("/usr/local/bin/testapp", retrieved.getExecutablePath());
            assertEquals("Retrieval test deployment", retrieved.getAppDeploymentDescription());
        }

        @Test
        @DisplayName("Should update application deployment")
        void shouldUpdateApplicationDeployment() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);

            // When - Update deployment
            deployment.setAppDeploymentId(deploymentId);
            deployment.setExecutablePath("/opt/new/path/testapp");
            deployment.setAppDeploymentDescription("Updated description");
            deploymentService.updateApplicationDeployment(deploymentId, deployment);

            // Then
            ApplicationDeploymentDescription updated = deploymentService.getApplicationDeployement(deploymentId);
            assertEquals("/opt/new/path/testapp", updated.getExecutablePath());
            assertEquals("Updated description", updated.getAppDeploymentDescription());
        }

        @Test
        @DisplayName("Should delete application deployment")
        void shouldDeleteApplicationDeployment() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            assertTrue(deploymentService.isAppDeploymentExists(deploymentId));

            // When
            deploymentService.removeAppDeployment(deploymentId);

            // Then
            assertFalse(deploymentService.isAppDeploymentExists(deploymentId));
            assertNull(deploymentService.getApplicationDeployement(deploymentId));
        }

        @Test
        @DisplayName("Should return null for non-existent deployment")
        void shouldReturnNullForNonExistentDeployment() throws Exception {
            // When
            ApplicationDeploymentDescription result = deploymentService.getApplicationDeployement(
                    "non-existent-deployment-" + UUID.randomUUID());

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Command Configuration Tests")
    class CommandConfigurationTests {

        @Test
        @DisplayName("Should save and retrieve module load commands")
        void shouldSaveAndRetrieveModuleLoadCommands() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            List<CommandObject> moduleLoadCmds = new ArrayList<>();
            moduleLoadCmds.add(createCommand("module load python/3.9", 1));
            moduleLoadCmds.add(createCommand("module load cuda/11.0", 2));
            moduleLoadCmds.add(createCommand("module load openmpi/4.0", 3));
            deployment.setModuleLoadCmds(moduleLoadCmds);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertNotNull(retrieved.getModuleLoadCmds());
            assertEquals(3, retrieved.getModuleLoadCmds().size());

            // Verify order
            List<CommandObject> cmds = retrieved.getModuleLoadCmds();
            cmds.sort((a, b) -> Integer.compare(a.getCommandOrder(), b.getCommandOrder()));
            assertEquals("module load python/3.9", cmds.get(0).getCommand());
            assertEquals("module load cuda/11.0", cmds.get(1).getCommand());
            assertEquals("module load openmpi/4.0", cmds.get(2).getCommand());
        }

        @Test
        @DisplayName("Should save and retrieve pre-job commands")
        void shouldSaveAndRetrievePreJobCommands() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            List<CommandObject> preJobCmds = new ArrayList<>();
            preJobCmds.add(createCommand("echo 'Starting job'", 1));
            preJobCmds.add(createCommand("mkdir -p $SCRATCH/output", 2));
            deployment.setPreJobCommands(preJobCmds);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertNotNull(retrieved.getPreJobCommands());
            assertEquals(2, retrieved.getPreJobCommands().size());
        }

        @Test
        @DisplayName("Should save and retrieve post-job commands")
        void shouldSaveAndRetrievePostJobCommands() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            List<CommandObject> postJobCmds = new ArrayList<>();
            postJobCmds.add(createCommand("echo 'Job completed'", 1));
            postJobCmds.add(createCommand("cleanup_temp_files.sh", 2));
            deployment.setPostJobCommands(postJobCmds);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertNotNull(retrieved.getPostJobCommands());
            assertEquals(2, retrieved.getPostJobCommands().size());
        }
    }

    @Nested
    @DisplayName("Environment Configuration Tests")
    class EnvironmentConfigurationTests {

        @Test
        @DisplayName("Should save and retrieve environment variables")
        void shouldSaveAndRetrieveEnvironmentVariables() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            List<SetEnvPaths> envVars = new ArrayList<>();
            envVars.add(createEnvPath("CUDA_HOME", "/usr/local/cuda"));
            envVars.add(createEnvPath("OMP_NUM_THREADS", "4"));
            envVars.add(createEnvPath("MPI_HOME", "/opt/openmpi"));
            deployment.setSetEnvironment(envVars);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertNotNull(retrieved.getSetEnvironment());
            assertEquals(3, retrieved.getSetEnvironment().size());

            Map<String, String> envMap = new HashMap<>();
            retrieved.getSetEnvironment().forEach(e -> envMap.put(e.getName(), e.getValue()));
            assertEquals("/usr/local/cuda", envMap.get("CUDA_HOME"));
            assertEquals("4", envMap.get("OMP_NUM_THREADS"));
            assertEquals("/opt/openmpi", envMap.get("MPI_HOME"));
        }

        @Test
        @DisplayName("Should save and retrieve library prepend paths")
        void shouldSaveAndRetrieveLibraryPrependPaths() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            List<SetEnvPaths> libPrepend = new ArrayList<>();
            libPrepend.add(createEnvPath("LD_LIBRARY_PATH", "/opt/custom/lib"));
            libPrepend.add(createEnvPath("PYTHONPATH", "/opt/custom/python"));
            deployment.setLibPrependPaths(libPrepend);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertNotNull(retrieved.getLibPrependPaths());
            assertEquals(2, retrieved.getLibPrependPaths().size());
        }

        @Test
        @DisplayName("Should save and retrieve library append paths")
        void shouldSaveAndRetrieveLibraryAppendPaths() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            List<SetEnvPaths> libAppend = new ArrayList<>();
            libAppend.add(createEnvPath("LD_LIBRARY_PATH", "/usr/local/lib"));
            deployment.setLibAppendPaths(libAppend);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertNotNull(retrieved.getLibAppendPaths());
            assertEquals(1, retrieved.getLibAppendPaths().size());
        }
    }

    @Nested
    @DisplayName("Query and Filter Tests")
    class QueryAndFilterTests {

        @Test
        @DisplayName("Should verify deployment exists by ID")
        void shouldVerifyDeploymentExistsById() throws Exception {
            // Given - Create a deployment
            ApplicationDeploymentDescription deploy1 = createBasicDeployment();
            deploy1.setAppDeploymentDescription("Deployment to verify");
            String id1 = deploymentService.addApplicationDeployment(deploy1, TEST_GATEWAY_ID);

            // When
            boolean exists = deploymentService.isAppDeploymentExists(id1);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(id1);

            // Then
            assertTrue(exists, "Deployment should exist");
            assertNotNull(retrieved, "Should retrieve deployment by ID");
            assertEquals(id1, retrieved.getAppDeploymentId());
            assertEquals("Deployment to verify", retrieved.getAppDeploymentDescription());
        }

        @Test
        @DisplayName("Should filter deployments by application module")
        void shouldFilterDeploymentsByApplicationModule() throws Exception {
            // Given - Create deployment for specific module
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);

            // When - Use correct filter key (appModuleId, not APPLICATION_MODULE_ID)
            Map<String, String> filters = new HashMap<>();
            filters.put("appModuleId", testAppModuleId);
            List<ApplicationDeploymentDescription> filtered = deploymentService.getApplicationDeployments(filters);

            // Then
            assertNotNull(filtered);
            assertTrue(filtered.size() >= 1);
            assertTrue(filtered.stream().allMatch(d -> testAppModuleId.equals(d.getAppModuleId())));
        }

        @Test
        @DisplayName("Should filter deployments by compute resource")
        void shouldFilterDeploymentsByComputeResource() throws Exception {
            // Given - Create deployment for specific compute resource
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);

            // When - Use correct filter key (computeHostId, not COMPUTE_HOST_ID)
            Map<String, String> filters = new HashMap<>();
            filters.put("computeHostId", testComputeResourceId);
            List<ApplicationDeploymentDescription> filtered = deploymentService.getApplicationDeployments(filters);

            // Then
            assertNotNull(filtered);
            assertTrue(filtered.size() >= 1);
            assertTrue(filtered.stream().allMatch(d -> testComputeResourceId.equals(d.getComputeHostId())));
        }

        @Test
        @DisplayName("Should get all deployment IDs")
        void shouldGetAllDeploymentIds() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);

            // When
            List<String> allIds = deploymentService.getAllApplicationDeployementIds();

            // Then
            assertNotNull(allIds, "All deployment IDs should not be null");
            assertTrue(allIds.contains(deploymentId), "Should contain the created deployment ID");
        }
    }

    @Nested
    @DisplayName("Parallelism Type Tests")
    class ParallelismTypeTests {

        @Test
        @DisplayName("Should save deployment with SERIAL parallelism")
        void shouldSaveDeploymentWithSerialParallelism() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            deployment.setParallelism(ApplicationParallelismType.SERIAL);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertEquals(ApplicationParallelismType.SERIAL, retrieved.getParallelism());
        }

        @Test
        @DisplayName("Should save deployment with MPI parallelism")
        void shouldSaveDeploymentWithMPIParallelism() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            deployment.setParallelism(ApplicationParallelismType.MPI);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertEquals(ApplicationParallelismType.MPI, retrieved.getParallelism());
        }

        @Test
        @DisplayName("Should save deployment with OPENMP parallelism")
        void shouldSaveDeploymentWithOpenMPParallelism() throws Exception {
            // Given
            ApplicationDeploymentDescription deployment = createBasicDeployment();
            deployment.setParallelism(ApplicationParallelismType.OPENMP);

            // When
            String deploymentId = deploymentService.addApplicationDeployment(deployment, TEST_GATEWAY_ID);
            ApplicationDeploymentDescription retrieved = deploymentService.getApplicationDeployement(deploymentId);

            // Then
            assertEquals(ApplicationParallelismType.OPENMP, retrieved.getParallelism());
        }
    }

    // Helper methods

    private ApplicationDeploymentDescription createBasicDeployment() {
        ApplicationDeploymentDescription deployment = new ApplicationDeploymentDescription();
        deployment.setAppDeploymentId(AiravataCommonsConstants.DEFAULT_ID); // Let service generate ID
        deployment.setAppModuleId(testAppModuleId);
        deployment.setComputeHostId(testComputeResourceId);
        deployment.setExecutablePath("/usr/local/bin/testapp");
        deployment.setParallelism(ApplicationParallelismType.SERIAL);
        deployment.setAppDeploymentDescription("Test deployment");
        return deployment;
    }

    private CommandObject createCommand(String command, int order) {
        CommandObject cmd = new CommandObject();
        cmd.setCommand(command);
        cmd.setCommandOrder(order);
        return cmd;
    }

    private SetEnvPaths createEnvPath(String name, String value) {
        SetEnvPaths envPath = new SetEnvPaths();
        envPath.setName(name);
        envPath.setValue(value);
        return envPath;
    }
}

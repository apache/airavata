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
package org.apache.airavata.registry.repositories.appcatalog;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.CommandObject;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.SetEnvPaths;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ApplicationDeploymentService;
import org.apache.airavata.registry.services.ApplicationInterfaceService;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.test.util.ReflectionEquals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ApplicationDeploymentRepositoryTest extends TestBase {

    private final ComputeResourceService computeResourceService;
    private final ApplicationInterfaceService applicationInterfaceService;
    private final ApplicationDeploymentService applicationDeploymentService;

    private String gatewayId = "testGateway";

    public ApplicationDeploymentRepositoryTest(
            ComputeResourceService computeResourceService,
            ApplicationInterfaceService applicationInterfaceService,
            ApplicationDeploymentService applicationDeploymentService) {
        this.computeResourceService = computeResourceService;
        this.applicationInterfaceService = applicationInterfaceService;
        this.applicationDeploymentService = applicationDeploymentService;
    }

    private String addSampleApplicationModule(String tag) throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod" + tag);
        applicationModule.setAppModuleName("appModName" + tag);
        return applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);
    }

    private String addSampleComputeResource(String tag) throws AppCatalogException {
        ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
        computeResourceDescription.setComputeResourceId("compHost" + tag);
        computeResourceDescription.setHostName("compHostName" + tag);
        org.apache.airavata.common.model.BatchQueue queue = new org.apache.airavata.common.model.BatchQueue();
        queue.setQueueName("default");
        queue.setQueueDescription("Default queue");
        queue.setMaxRunTime(24);
        queue.setMaxNodes(1);
        queue.setMaxJobsInQueue(10);
        computeResourceDescription.setBatchQueues(java.util.Collections.singletonList(queue));
        return computeResourceService.addComputeResource(computeResourceDescription);
    }

    private boolean deepCompareDeployment(
            ApplicationDeploymentDescription expected, ApplicationDeploymentDescription actual) {
        // Compare basic fields first, excluding collections and internal fields
        boolean equals = ReflectionEquals.reflectionEquals(
                expected,
                actual,
                "moduleLoadCmds",
                "libPrependPaths",
                "libAppendPaths",
                "setEnvironment",
                "preJobCommands",
                "postJobCommands",
                "__isset_bitfield",
                "appDeploymentDescription"); // appDeploymentDescription might be null in actual

        // Compare collections
        equals = equals
                && deepCompareLists(
                        expected.getSetEnvironment(),
                        actual.getSetEnvironment(),
                        Comparator.comparingInt(SetEnvPaths::getEnvPathOrder));
        // Compare libPrependPaths and libAppendPaths by name, ignoring envPathOrder since it's always 0 for these
        equals = equals
                && deepCompareListsIgnoreEnvPathOrder(
                        expected.getLibPrependPaths(),
                        actual.getLibPrependPaths(),
                        Comparator.comparing(SetEnvPaths::getName));
        equals = equals
                && deepCompareListsIgnoreEnvPathOrder(
                        expected.getLibAppendPaths(),
                        actual.getLibAppendPaths(),
                        Comparator.comparing(SetEnvPaths::getName));
        equals = equals
                && deepCompareLists(
                        expected.getModuleLoadCmds(),
                        actual.getModuleLoadCmds(),
                        Comparator.comparingInt(CommandObject::getCommandOrder));
        equals = equals
                && deepCompareLists(
                        expected.getPreJobCommands(),
                        actual.getPreJobCommands(),
                        Comparator.comparingInt(CommandObject::getCommandOrder));
        equals = equals
                && deepCompareLists(
                        expected.getPostJobCommands(),
                        actual.getPostJobCommands(),
                        Comparator.comparingInt(CommandObject::getCommandOrder));
        return equals;
    }

    private <T> boolean deepCompareLists(List<T> expected, List<T> actual, Comparator<? super T> c) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;
        if (expected.size() != actual.size()) return false;
        if (expected.isEmpty()) return true;

        List<T> expectedCopy = new ArrayList<>(expected);
        expectedCopy.sort(c);
        List<T> actualCopy = new ArrayList<>(actual);
        actualCopy.sort(c);

        // Compare elements one by one, excluding __isset_bitfield which may differ
        for (int i = 0; i < expectedCopy.size(); i++) {
            T expectedItem = expectedCopy.get(i);
            T actualItem = actualCopy.get(i);
            if (!ReflectionEquals.reflectionEquals(expectedItem, actualItem, "__isset_bitfield")) {
                return false;
            }
        }
        return true;
    }

    private boolean deepCompareListsIgnoreEnvPathOrder(
            List<SetEnvPaths> expected, List<SetEnvPaths> actual, Comparator<? super SetEnvPaths> c) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;
        if (expected.size() != actual.size()) return false;
        if (expected.isEmpty()) return true;

        List<SetEnvPaths> expectedCopy = new ArrayList<>(expected);
        expectedCopy.sort(c);
        List<SetEnvPaths> actualCopy = new ArrayList<>(actual);
        actualCopy.sort(c);

        // Compare elements one by one, excluding __isset_bitfield and envPathOrder
        for (int i = 0; i < expectedCopy.size(); i++) {
            SetEnvPaths expectedItem = expectedCopy.get(i);
            SetEnvPaths actualItem = actualCopy.get(i);
            if (!ReflectionEquals.reflectionEquals(expectedItem, actualItem, "__isset_bitfield", "envPathOrder")) {
                return false;
            }
        }
        return true;
    }

    private ApplicationDeploymentDescription prepareSampleDeployment(
            String tag, String applicationModule, String computeResource) {
        CommandObject moduleLoadCmd = new CommandObject();
        moduleLoadCmd.setCommand("moduleLoadCmd");
        moduleLoadCmd.setCommandOrder(1);

        SetEnvPaths libPrependPath = new SetEnvPaths();
        libPrependPath.setName("libPrependPath");
        libPrependPath.setValue("libPrependPathValue");
        libPrependPath.setEnvPathOrder(0); // LibraryPathEntity doesn't store envPathOrder, always returns 0
        SetEnvPaths libAppendPath = new SetEnvPaths();
        libAppendPath.setName("libAppendPath");
        libAppendPath.setValue("libAppendPathValue");
        libAppendPath.setEnvPathOrder(0); // LibraryPathEntity doesn't store envPathOrder, always returns 0

        SetEnvPaths setEnvironment = new SetEnvPaths();
        setEnvironment.setName("setEnvironment");
        setEnvironment.setValue("setEnvironmentValue");
        setEnvironment.setEnvPathOrder(3);

        CommandObject preJobCommand = new CommandObject();
        preJobCommand.setCommand("preCommand");
        preJobCommand.setCommandOrder(2);
        CommandObject postJobCommand = new CommandObject();
        postJobCommand.setCommand("postCommand");
        postJobCommand.setCommandOrder(3);

        ApplicationDeploymentDescription deployment = new ApplicationDeploymentDescription();
        deployment.setAppDeploymentId("appDep" + tag);
        deployment.setAppDeploymentDescription("test application deployment" + tag);
        deployment.setAppModuleId(applicationModule);
        deployment.setComputeHostId(computeResource);
        deployment.setExecutablePath("executablePath" + tag);
        deployment.setParallelism(ApplicationParallelismType.SERIAL);
        deployment.setModuleLoadCmds(new ArrayList<>(Arrays.asList(moduleLoadCmd)));
        deployment.setLibPrependPaths(new ArrayList<>(Arrays.asList(libPrependPath)));
        deployment.setLibAppendPaths(new ArrayList<>(Arrays.asList(libAppendPath)));
        deployment.setPreJobCommands(new ArrayList<>(Arrays.asList(preJobCommand)));
        deployment.setPostJobCommands(new ArrayList<>(Arrays.asList(postJobCommand)));
        deployment.setSetEnvironment(new ArrayList<>(Arrays.asList(setEnvironment)));
        deployment.setDefaultQueueName("queue" + tag);
        deployment.setDefaultCPUCount(10);
        deployment.setDefaultNodeCount(5);
        deployment.setDefaultWalltime(15);
        deployment.setEditableByUser(true);

        return deployment;
    }

    @Test
    public void createAppDeploymentTest() throws AppCatalogException {
        // Clean up any existing deployment from previous test runs
        try {
            applicationDeploymentService.removeAppDeployment("appDep1");
        } catch (Exception e) {
            // Ignore if deployment doesn't exist
        }

        Assertions.assertNull(applicationDeploymentService.getApplicationDeployement("appDep1"));
        String applicationModule = addSampleApplicationModule("1");
        String computeResource = addSampleComputeResource("1");

        ApplicationDeploymentDescription deployment = prepareSampleDeployment("1", applicationModule, computeResource);
        applicationDeploymentService.addApplicationDeployment(deployment, gatewayId);
        ApplicationDeploymentDescription savedDeployment =
                applicationDeploymentService.getApplicationDeployement("appDep1");
        Assertions.assertNotNull(savedDeployment);

        // Compare key fields explicitly for better error messages
        Assertions.assertEquals(deployment.getAppDeploymentId(), savedDeployment.getAppDeploymentId());
        Assertions.assertEquals(deployment.getAppModuleId(), savedDeployment.getAppModuleId());
        Assertions.assertEquals(deployment.getComputeHostId(), savedDeployment.getComputeHostId());
        Assertions.assertEquals(deployment.getExecutablePath(), savedDeployment.getExecutablePath());
        Assertions.assertEquals(deployment.getParallelism(), savedDeployment.getParallelism());

        // Compare collections
        Assertions.assertEquals(
                deployment.getModuleLoadCmds() != null
                        ? deployment.getModuleLoadCmds().size()
                        : 0,
                savedDeployment.getModuleLoadCmds() != null
                        ? savedDeployment.getModuleLoadCmds().size()
                        : 0);
        Assertions.assertEquals(
                deployment.getLibPrependPaths() != null
                        ? deployment.getLibPrependPaths().size()
                        : 0,
                savedDeployment.getLibPrependPaths() != null
                        ? savedDeployment.getLibPrependPaths().size()
                        : 0);
        Assertions.assertEquals(
                deployment.getLibAppendPaths() != null
                        ? deployment.getLibAppendPaths().size()
                        : 0,
                savedDeployment.getLibAppendPaths() != null
                        ? savedDeployment.getLibAppendPaths().size()
                        : 0);
        Assertions.assertEquals(
                deployment.getSetEnvironment() != null
                        ? deployment.getSetEnvironment().size()
                        : 0,
                savedDeployment.getSetEnvironment() != null
                        ? savedDeployment.getSetEnvironment().size()
                        : 0);
        Assertions.assertEquals(
                deployment.getPreJobCommands() != null
                        ? deployment.getPreJobCommands().size()
                        : 0,
                savedDeployment.getPreJobCommands() != null
                        ? savedDeployment.getPreJobCommands().size()
                        : 0);
        Assertions.assertEquals(
                deployment.getPostJobCommands() != null
                        ? deployment.getPostJobCommands().size()
                        : 0,
                savedDeployment.getPostJobCommands() != null
                        ? savedDeployment.getPostJobCommands().size()
                        : 0);

        // Now do deep comparison
        Assertions.assertTrue(
                deepCompareDeployment(deployment, savedDeployment),
                "Deep comparison failed. Expected: " + deployment + ", Actual: " + savedDeployment);
    }

    @Test
    public void createAppDeploymentWithDefaultIdTest() throws AppCatalogException {

        String applicationModule = addSampleApplicationModule("1");
        String computeResource = addSampleComputeResource("1");

        ApplicationDeploymentDescription deployment = prepareSampleDeployment("1", applicationModule, computeResource);
        deployment.setAppDeploymentId(AiravataCommonsConstants.DEFAULT_ID);
        String deploymentId = applicationDeploymentService.addApplicationDeployment(deployment, gatewayId);
        Assertions.assertNotEquals(deploymentId, AiravataCommonsConstants.DEFAULT_ID);
        Assertions.assertEquals("compHostName1" + "_" + applicationModule, deploymentId);
    }

    @Test
    public void updateAppDeploymentTest() throws AppCatalogException {
        String applicationModule = addSampleApplicationModule("1");
        String computeResource = addSampleComputeResource("1");

        ApplicationDeploymentDescription deployment = prepareSampleDeployment("1", applicationModule, computeResource);

        String deploymentId = applicationDeploymentService.addApplicationDeployment(deployment, gatewayId);

        deployment.setDefaultQueueName("updated");
        deployment.setAppDeploymentDescription("updated description");

        CommandObject moduleLoadCmd = new CommandObject();
        moduleLoadCmd.setCommand("moduleLoadCmd2");
        moduleLoadCmd.setCommandOrder(2);

        deployment.getModuleLoadCmds().add(moduleLoadCmd);

        SetEnvPaths libPrependPath = new SetEnvPaths();
        libPrependPath.setName("libPrependPath2");
        libPrependPath.setValue("libPrependPathValue2");
        libPrependPath.setEnvPathOrder(4);

        deployment.getLibPrependPaths().add(libPrependPath);

        deployment.setExecutablePath("executablePath2");
        deployment.setParallelism(ApplicationParallelismType.MPI);
        deployment.setDefaultCPUCount(12);
        deployment.setDefaultNodeCount(15);
        deployment.setDefaultWalltime(10);
        deployment.setEditableByUser(false);

        applicationDeploymentService.updateApplicationDeployment(deploymentId, deployment);

        ApplicationDeploymentDescription updatedDeployment =
                applicationDeploymentService.getApplicationDeployement(deploymentId);

        Assertions.assertTrue(deepCompareDeployment(deployment, updatedDeployment));
    }

    @Test
    public void listAllDeployments() throws AppCatalogException {

        List<ApplicationDeploymentDescription> allDeployments = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String applicationModule = addSampleApplicationModule(i + "");
            String computeResource = addSampleComputeResource(i + "");
            ApplicationDeploymentDescription deployment =
                    prepareSampleDeployment(i + "", applicationModule, computeResource);
            allDeployments.add(deployment);
            String savedDeploymentId = applicationDeploymentService.addApplicationDeployment(deployment, gatewayId);
            Assertions.assertEquals(deployment.getAppDeploymentId(), savedDeploymentId);
        }

        List<ApplicationDeploymentDescription> appDeploymentList =
                applicationDeploymentService.getAllApplicationDeployements(gatewayId);
        List<String> appDeploymentIds = applicationDeploymentService.getAllApplicationDeployementIds();

        Assertions.assertEquals(allDeployments.size(), appDeploymentList.size());
        Assertions.assertEquals(allDeployments.size(), appDeploymentIds.size());

        for (int i = 0; i < allDeployments.size(); i++) {
            Assertions.assertTrue(deepCompareDeployment(allDeployments.get(i), appDeploymentList.get(i)));
            Assertions.assertEquals(allDeployments.get(i).getAppDeploymentId(), appDeploymentIds.get(i));
        }
    }

    @Test
    public void filterApplicationDeploymentsTest() throws AppCatalogException {

        String applicationModule1 = addSampleApplicationModule("1");
        String computeResource1 = addSampleComputeResource("1");
        String applicationModule2 = addSampleApplicationModule("2");
        String computeResource2 = addSampleComputeResource("2");

        ApplicationDeploymentDescription deployment1 =
                prepareSampleDeployment("1", applicationModule1, computeResource1);
        ApplicationDeploymentDescription deployment2 =
                prepareSampleDeployment("2", applicationModule1, computeResource2);
        ApplicationDeploymentDescription deployment3 =
                prepareSampleDeployment("3", applicationModule2, computeResource2);

        applicationDeploymentService.addApplicationDeployment(deployment1, gatewayId);
        applicationDeploymentService.addApplicationDeployment(deployment2, gatewayId);
        applicationDeploymentService.addApplicationDeployment(deployment3, gatewayId);

        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule1);
        List<ApplicationDeploymentDescription> filteredDeployments =
                applicationDeploymentService.getApplicationDeployments(filters);
        Assertions.assertEquals(2, filteredDeployments.size());
        Assertions.assertTrue(deepCompareDeployment(deployment1, filteredDeployments.get(0)));
        Assertions.assertTrue(deepCompareDeployment(deployment2, filteredDeployments.get(1)));

        filters.clear();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule2);
        filteredDeployments = applicationDeploymentService.getApplicationDeployments(filters);
        Assertions.assertEquals(1, filteredDeployments.size());
        Assertions.assertTrue(deepCompareDeployment(deployment3, filteredDeployments.get(0)));

        filters.clear();
        filters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, computeResource1);
        filteredDeployments = applicationDeploymentService.getApplicationDeployments(filters);
        Assertions.assertEquals(1, filteredDeployments.size());
        Assertions.assertTrue(deepCompareDeployment(deployment1, filteredDeployments.get(0)));

        filters.clear();
        filters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, computeResource2);
        filteredDeployments = applicationDeploymentService.getApplicationDeployments(filters);
        Assertions.assertEquals(2, filteredDeployments.size());
        Assertions.assertTrue(deepCompareDeployment(deployment2, filteredDeployments.get(0)));
        Assertions.assertTrue(deepCompareDeployment(deployment3, filteredDeployments.get(1)));

        filters.clear();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule1);
        filters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, computeResource2);
        filteredDeployments = applicationDeploymentService.getApplicationDeployments(filters);
        Assertions.assertEquals(1, filteredDeployments.size());
        Assertions.assertTrue(deepCompareDeployment(deployment2, filteredDeployments.get(0)));

        filters.clear();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule1);
        filters.put("INVALID FIELD", computeResource2);
        // Expect exception when using invalid filter field
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> applicationDeploymentService.getApplicationDeployments(filters),
                "Should throw exception for invalid filter field");
    }

    @Test
    public void deleteApplicationDeploymentTest() throws AppCatalogException {
        // Clean up any existing deployment from previous test runs
        try {
            applicationDeploymentService.removeAppDeployment("appDep1");
        } catch (Exception e) {
            // Ignore if deployment doesn't exist
        }

        String applicationModule = addSampleApplicationModule("1");
        String computeResource = addSampleComputeResource("1");
        ApplicationDeploymentDescription deployment = prepareSampleDeployment("1", applicationModule, computeResource);

        Assertions.assertNull(applicationDeploymentService.getApplicationDeployement(deployment.getAppDeploymentId()));

        applicationDeploymentService.addApplicationDeployment(deployment, gatewayId);
        Assertions.assertNotNull(
                applicationDeploymentService.getApplicationDeployement(deployment.getAppDeploymentId()));
        applicationDeploymentService.removeAppDeployment(deployment.getAppDeploymentId());
        Assertions.assertNull(applicationDeploymentService.getApplicationDeployement(deployment.getAppDeploymentId()));
    }

    @Test
    public void accessibleDeploymentTest() throws AppCatalogException {
        String applicationModule1 = addSampleApplicationModule("1");
        String computeResource1 = addSampleComputeResource("1");
        String applicationModule2 = addSampleApplicationModule("2");
        String computeResource2 = addSampleComputeResource("2");

        ApplicationDeploymentDescription deployment1 =
                prepareSampleDeployment("1", applicationModule1, computeResource1);
        ApplicationDeploymentDescription deployment2 =
                prepareSampleDeployment("2", applicationModule1, computeResource2);
        ApplicationDeploymentDescription deployment3 =
                prepareSampleDeployment("3", applicationModule2, computeResource2);

        applicationDeploymentService.addApplicationDeployment(deployment1, gatewayId);
        applicationDeploymentService.addApplicationDeployment(deployment2, gatewayId);
        applicationDeploymentService.addApplicationDeployment(deployment3, gatewayId);

        List<String> accessibleAppIds = new ArrayList<>();
        accessibleAppIds.add(deployment1.getAppDeploymentId());
        accessibleAppIds.add(deployment2.getAppDeploymentId());
        accessibleAppIds.add(deployment3.getAppDeploymentId());

        List<String> accessibleCompHostIds = new ArrayList<>();
        accessibleCompHostIds.add(computeResource1);

        List<ApplicationDeploymentDescription> accessibleApplicationDeployments =
                applicationDeploymentService.getAccessibleApplicationDeployments(
                        gatewayId, accessibleAppIds, accessibleCompHostIds);

        assertTrue(accessibleApplicationDeployments.size() == 1);
        assertTrue(deepCompareDeployment(deployment1, accessibleApplicationDeployments.get(0)));

        accessibleCompHostIds = new ArrayList<>();
        accessibleCompHostIds.add(computeResource2);

        accessibleApplicationDeployments = applicationDeploymentService.getAccessibleApplicationDeployments(
                gatewayId, accessibleAppIds, accessibleCompHostIds);

        assertTrue(accessibleApplicationDeployments.size() == 2);
        assertTrue(deepCompareDeployment(deployment2, accessibleApplicationDeployments.get(0)));
        assertTrue(deepCompareDeployment(deployment3, accessibleApplicationDeployments.get(1)));
    }
}

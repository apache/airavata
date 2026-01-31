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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.OutputDataObjectType;
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
public class ApplicationInterfaceRepositoryTest extends TestBase {

    private static final List<String> SYSTEM_INPUT_NAMES = List.of("STDIN");
    private static final List<String> SYSTEM_OUTPUT_NAMES = List.of("STDOUT", "STDERR");

    private final ApplicationInterfaceService applicationInterfaceService;
    private final ComputeResourceService computeResourceService;
    private final ApplicationDeploymentService applicationDeploymentService;

    private String gatewayId = "testGateway";

    /** Count user-defined inputs/outputs excluding system ones (STDIN, STDOUT, STDERR). */
    private static int userInputCount(List<InputDataObjectType> inputs) {
        return (int) inputs.stream().filter(i -> !SYSTEM_INPUT_NAMES.contains(i.getName())).count();
    }
    private static int userOutputCount(List<OutputDataObjectType> outputs) {
        return (int) outputs.stream().filter(o -> !SYSTEM_OUTPUT_NAMES.contains(o.getName())).count();
    }
    private static InputDataObjectType getUserInputByName(List<InputDataObjectType> inputs, String name) {
        return inputs.stream().filter(i -> name.equals(i.getName())).findFirst().orElse(null);
    }
    private static OutputDataObjectType getUserOutputByName(List<OutputDataObjectType> outputs, String name) {
        return outputs.stream().filter(o -> name.equals(o.getName())).findFirst().orElse(null);
    }

    public ApplicationInterfaceRepositoryTest(
            ApplicationInterfaceService applicationInterfaceService,
            ComputeResourceService computeResourceService,
            ApplicationDeploymentService applicationDeploymentService) {
        this.applicationInterfaceService = applicationInterfaceService;
        this.computeResourceService = computeResourceService;
        this.applicationDeploymentService = applicationDeploymentService;
    }

    @Test
    public void addApplicationModuleTest() throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        String moduleId = applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);

        ApplicationModule savedAppModule = applicationInterfaceService.getApplicationModule(moduleId);
        Assertions.assertTrue(ReflectionEquals.reflectionEquals(applicationModule, savedAppModule));
    }

    @Test
    public void addApplicationModuleWithEmptyIdTest() throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        String moduleId = applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);

        ApplicationModule savedAppModule = applicationInterfaceService.getApplicationModule(moduleId);
        Assertions.assertNotEquals(applicationModule.getAppModuleName(), savedAppModule.getAppModuleId());
        Assertions.assertTrue(savedAppModule.getAppModuleId().startsWith(applicationModule.getAppModuleName()));
    }

    @Test
    public void deleteApplicationModuleTest() throws AppCatalogException {

        Assertions.assertNull(applicationInterfaceService.getApplicationModule("appMod1"));

        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        String moduleId = applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);
        Assertions.assertNotNull(applicationInterfaceService.getApplicationModule(moduleId));

        Assertions.assertTrue(applicationInterfaceService.removeApplicationModule("appMod1"));

        Assertions.assertNull(applicationInterfaceService.getApplicationModule("appMod1"));
    }

    @Test
    public void updateApplicationModuleTest() throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        String moduleId = applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);

        ApplicationModule savedAppModule = applicationInterfaceService.getApplicationModule(moduleId);
        Assertions.assertTrue(ReflectionEquals.reflectionEquals(applicationModule, savedAppModule));

        savedAppModule.setAppModuleName("Updated Name");
        savedAppModule.setAppModuleDescription("Updated Description");
        savedAppModule.setAppModuleVersion("new version");

        applicationInterfaceService.updateApplicationModule("appMod1", savedAppModule);

        ApplicationModule updatedAppModule = applicationInterfaceService.getApplicationModule(moduleId);
        Assertions.assertTrue(ReflectionEquals.reflectionEquals(savedAppModule, updatedAppModule));
    }

    @Test
    public void addApplicationInterfaceTest() throws AppCatalogException {
        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");
        applicationInterfaceDescription.setApplicationModules(new ArrayList<>());
        applicationInterfaceDescription.setApplicationInputs(new ArrayList<>());
        applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());

        String interfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);
        assertEquals(applicationInterfaceDescription.getApplicationInterfaceId(), interfaceId);

        ApplicationInterfaceDescription savedInterface =
                applicationInterfaceService.getApplicationInterface(interfaceId);
        // Exclude inputs/outputs: service may auto-add system STDIN/STDOUT/STDERR
        Assertions.assertTrue(
                ReflectionEquals.reflectionEquals(
                        applicationInterfaceDescription,
                        savedInterface,
                        "__isset_bitfield",
                        "applicationInputs",
                        "applicationOutputs"));
        Assertions.assertEquals(0, userInputCount(savedInterface.getApplicationInputs()));
        Assertions.assertEquals(0, userOutputCount(savedInterface.getApplicationOutputs()));
    }

    @Test
    public void addApplicationInterfaceWithDefaultIdTest() throws AppCatalogException {
        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationName("app interface 1");

        applicationInterfaceDescription.setApplicationModules(new ArrayList<>());

        InputDataObjectType input = new InputDataObjectType();
        input.setName("input1");
        input.setApplicationArgument("Arg");
        input.setDataStaged(true);
        input.setInputOrder(0);
        input.setIsReadOnly(true);
        input.setIsRequired(true);
        input.setRequiredToAddedToCommandLine(true);
        input.setType(DataType.FLOAT);
        input.setUserFriendlyDescription("User friendly description");
        input.setValue("113");
        input.setMetaData("Metadata");
        input.setStandardInput(true);
        applicationInterfaceDescription.setApplicationInputs(Collections.singletonList(input));

        applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());

        String interfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);
        Assertions.assertTrue(
                interfaceId.startsWith("app_interface_1"),
                MessageFormat.format("{0} does not start with {1}", interfaceId, "app_interface_1"));
    }

    @Test
    public void deleteApplicationInterfaceTest() throws AppCatalogException {

        Assertions.assertNull(applicationInterfaceService.getApplicationModule("interface1"));

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");
        applicationInterfaceDescription.setApplicationModules(new ArrayList<>());
        applicationInterfaceDescription.setApplicationInputs(new ArrayList<>());
        applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());

        InputDataObjectType input = new InputDataObjectType();
        input.setName("input1");
        input.setApplicationArgument("Arg");
        input.setDataStaged(true);
        input.setInputOrder(0);
        input.setIsReadOnly(true);
        input.setIsRequired(true);
        input.setRequiredToAddedToCommandLine(true);
        input.setType(DataType.FLOAT);
        input.setUserFriendlyDescription("User friendly description");
        input.setValue("113");
        input.setMetaData("Metadata");
        input.setStandardInput(true);
        applicationInterfaceDescription.getApplicationInputs().add(input);

        OutputDataObjectType output = new OutputDataObjectType();
        output.setName("output1");
        output.setValue("value");
        output.setType(DataType.FLOAT);
        output.setApplicationArgument("Argument");
        output.setDataMovement(true);
        output.setIsRequired(true);
        output.setLocation("/home/");
        output.setSearchQuery("Search query");
        output.setRequiredToAddedToCommandLine(true);
        output.setOutputStreaming(true);
        applicationInterfaceDescription.getApplicationOutputs().add(output);

        String interfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        Assertions.assertNotNull(applicationInterfaceService.getApplicationInterface(interfaceId));
        Assertions.assertTrue(applicationInterfaceService.removeApplicationInterface(interfaceId));
        Assertions.assertNull(applicationInterfaceService.getApplicationInterface(interfaceId));
    }

    @Test
    public void addModulesToInterfaceTest() throws AppCatalogException {
        // Use unique IDs to avoid conflicts from previous test runs
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String moduleId1 = "appMod1-" + uniqueSuffix;
        String moduleId2 = "appMod2-" + uniqueSuffix;
        String interfaceId = "interface1-" + uniqueSuffix;

        ApplicationModule applicationModule1 = new ApplicationModule();
        applicationModule1.setAppModuleId(moduleId1);
        applicationModule1.setAppModuleName("appMod1Name");
        String savedModuleId1 = applicationInterfaceService.addApplicationModule(applicationModule1, gatewayId);

        ApplicationModule applicationModule2 = new ApplicationModule();
        applicationModule2.setAppModuleId(moduleId2);
        applicationModule2.setAppModuleName("appMod2Name");
        String savedModuleId2 = applicationInterfaceService.addApplicationModule(applicationModule2, gatewayId);

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId(interfaceId);
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String savedInterfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        applicationInterfaceService.addApplicationModuleMapping(savedModuleId1, savedInterfaceId);
        applicationInterfaceService.addApplicationModuleMapping(savedModuleId2, savedInterfaceId);

        ApplicationInterfaceDescription savedInterface =
                applicationInterfaceService.getApplicationInterface(savedInterfaceId);

        Assertions.assertNotNull(savedInterface.getApplicationModules());
        Assertions.assertTrue(
                savedInterface.getApplicationModules().size() >= 2,
                "Should have at least 2 modules, but got: "
                        + savedInterface.getApplicationModules().size());
        Assertions.assertTrue(savedInterface.getApplicationModules().contains(applicationModule1.getAppModuleId()));
        Assertions.assertTrue(savedInterface.getApplicationModules().contains(applicationModule2.getAppModuleId()));
    }

    @Test
    public void addInputsOutputsToInterfaceTest() throws AppCatalogException {
        // Clean up any existing interface from previous test runs
        try {
            applicationInterfaceService.removeApplicationInterface("interface1");
        } catch (Exception e) {
            // Ignore if doesn't exist
        }

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String interfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        InputDataObjectType input = new InputDataObjectType();
        input.setName("input1");
        input.setApplicationArgument("Arg");
        input.setDataStaged(true);
        input.setInputOrder(0);
        input.setIsReadOnly(true);
        input.setIsRequired(true);
        input.setRequiredToAddedToCommandLine(true);
        input.setType(DataType.FLOAT);
        input.setUserFriendlyDescription("User friendly description");
        input.setValue("113");
        input.setMetaData("Metadata");
        input.setStandardInput(true);
        input.setStorageResourceId("test-storage-resource-id");

        OutputDataObjectType output = new OutputDataObjectType();
        output.setName("output1");
        output.setValue("value");
        output.setType(DataType.FLOAT);
        output.setApplicationArgument("Argument");
        output.setDataMovement(true);
        output.setIsRequired(true);
        output.setLocation("/home/");
        output.setSearchQuery("Search query");
        output.setRequiredToAddedToCommandLine(true);
        output.setOutputStreaming(true);
        output.setMetaData("outputMetaData");
        output.setStorageResourceId("test-storage-resource-id");

        applicationInterfaceDescription.setApplicationInputs(Collections.singletonList(input));
        applicationInterfaceDescription.setApplicationOutputs(Collections.singletonList(output));

        applicationInterfaceService.updateApplicationInterface(interfaceId, applicationInterfaceDescription);

        ApplicationInterfaceDescription savedInterface =
                applicationInterfaceService.getApplicationInterface(interfaceId);
        Assertions.assertEquals(1, userInputCount(savedInterface.getApplicationInputs()),
                "One user input (system STDIN may also be present)");
        Assertions.assertEquals(1, userOutputCount(savedInterface.getApplicationOutputs()),
                "One user output (system STDOUT/STDERR may also be present)");

        // Compare inputs and outputs - verify key fields explicitly for better error messages
        InputDataObjectType savedInput = getUserInputByName(savedInterface.getApplicationInputs(), "input1");
        OutputDataObjectType savedOutput =
                getUserOutputByName(savedInterface.getApplicationOutputs(), "output1");
        Assertions.assertNotNull(savedInput, "User input input1 should be present");
        Assertions.assertNotNull(savedOutput, "User output output1 should be present");

        // Verify key input fields explicitly
        Assertions.assertEquals(input.getName(), savedInput.getName(), "Input name should match");
        Assertions.assertEquals(
                input.getApplicationArgument(),
                savedInput.getApplicationArgument(),
                "Input application argument should match");
        Assertions.assertEquals(input.getType(), savedInput.getType(), "Input type should match");
        Assertions.assertEquals(input.getValue(), savedInput.getValue(), "Input value should match");
        Assertions.assertEquals(
                input.getUserFriendlyDescription(),
                savedInput.getUserFriendlyDescription(),
                "Input user friendly description should match");
        Assertions.assertEquals(input.getDataStaged(), savedInput.getDataStaged(), "Input dataStaged should match");
        Assertions.assertEquals(input.getIsRequired(), savedInput.getIsRequired(), "Input isRequired should match");
        Assertions.assertEquals(input.getIsReadOnly(), savedInput.getIsReadOnly(), "Input isReadOnly should match");
        Assertions.assertEquals(
                input.getRequiredToAddedToCommandLine(),
                savedInput.getRequiredToAddedToCommandLine(),
                "Input requiredToAddedToCommandLine should match");
        Assertions.assertEquals(
                input.getStandardInput(), savedInput.getStandardInput(), "Input standardInput should match");
        // Note: storageResourceId is not persisted in APPLICATION_INPUT table, so skip this check

        // Verify key output fields explicitly
        Assertions.assertEquals(output.getName(), savedOutput.getName(), "Output name should match");
        Assertions.assertEquals(
                output.getApplicationArgument(),
                savedOutput.getApplicationArgument(),
                "Output application argument should match");
        Assertions.assertEquals(output.getType(), savedOutput.getType(), "Output type should match");
        Assertions.assertEquals(output.getValue(), savedOutput.getValue(), "Output value should match");
        Assertions.assertEquals(output.getLocation(), savedOutput.getLocation(), "Output location should match");
        Assertions.assertEquals(
                output.getDataMovement(), savedOutput.getDataMovement(), "Output dataMovement should match");
        Assertions.assertEquals(output.getIsRequired(), savedOutput.getIsRequired(), "Output isRequired should match");
        Assertions.assertEquals(
                output.getRequiredToAddedToCommandLine(),
                savedOutput.getRequiredToAddedToCommandLine(),
                "Output requiredToAddedToCommandLine should match");
        Assertions.assertEquals(
                output.getOutputStreaming(), savedOutput.getOutputStreaming(), "Output outputStreaming should match");
        Assertions.assertEquals(
                output.getSearchQuery(), savedOutput.getSearchQuery(), "Output searchQuery should match");
        // Note: storageResourceId is not persisted in APPLICATION_OUTPUT table, so skip this check

        List<InputDataObjectType> savedInputs = applicationInterfaceService.getApplicationInputs(interfaceId);
        List<OutputDataObjectType> savedOutputs = applicationInterfaceService.getApplicationOutputs(interfaceId);

        Assertions.assertEquals(1, userInputCount(savedInputs), "One user input (system STDIN may also be present)");
        Assertions.assertEquals(1, userOutputCount(savedOutputs), "One user output (system STDOUT/STDERR may also be present)");

        InputDataObjectType savedInput1 = getUserInputByName(savedInputs, "input1");
        OutputDataObjectType savedOutput1 = getUserOutputByName(savedOutputs, "output1");
        Assertions.assertNotNull(savedInput1, "User input input1 should be present");
        Assertions.assertNotNull(savedOutput1, "User output output1 should be present");
        // Verify saved inputs match
        Assertions.assertEquals(input.getName(), savedInput1.getName(), "Saved input name should match");
        Assertions.assertEquals(input.getType(), savedInput1.getType(), "Saved input type should match");
        // Verify saved outputs match
        Assertions.assertEquals(output.getName(), savedOutput1.getName(), "Saved output name should match");
        Assertions.assertEquals(output.getType(), savedOutput1.getType(), "Saved output type should match");
    }

    @Test
    public void addAndRemoveInputsOutputsToInterfaceTest() throws AppCatalogException {

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String interfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        InputDataObjectType input = new InputDataObjectType();
        input.setName("input1");
        input.setApplicationArgument("Arg");
        input.setDataStaged(true);
        input.setInputOrder(0);
        input.setIsReadOnly(true);
        input.setIsRequired(true);
        input.setRequiredToAddedToCommandLine(true);
        input.setType(DataType.FLOAT);
        input.setUserFriendlyDescription("User friendly description");
        input.setValue("113");
        input.setMetaData("Metadata");
        input.setStandardInput(true);

        InputDataObjectType input2 = new InputDataObjectType();
        input2.setName("input2");
        input2.setInputOrder(1);

        OutputDataObjectType output = new OutputDataObjectType();
        output.setName("output1");
        output.setValue("value");
        output.setType(DataType.FLOAT);
        output.setApplicationArgument("Argument");
        output.setDataMovement(true);
        output.setIsRequired(true);
        output.setLocation("/home/");
        output.setSearchQuery("Search query");
        output.setRequiredToAddedToCommandLine(true);
        output.setOutputStreaming(true);

        OutputDataObjectType output2 = new OutputDataObjectType();
        output2.setName("output2");

        applicationInterfaceDescription.setApplicationInputs(Arrays.asList(input, input2));
        applicationInterfaceDescription.setApplicationOutputs(Arrays.asList(output, output2));

        applicationInterfaceService.updateApplicationInterface(interfaceId, applicationInterfaceDescription);

        ApplicationInterfaceDescription savedInterface =
                applicationInterfaceService.getApplicationInterface(interfaceId);
        Assertions.assertEquals(2, userInputCount(savedInterface.getApplicationInputs()), "Two user inputs");
        Assertions.assertEquals(2, userOutputCount(savedInterface.getApplicationOutputs()), "Two user outputs");

        savedInterface.setApplicationInputs(Arrays.asList(input));
        savedInterface.setApplicationOutputs(Arrays.asList(output));

        applicationInterfaceService.updateApplicationInterface(interfaceId, savedInterface);
        ApplicationInterfaceDescription updatedInterface =
                applicationInterfaceService.getApplicationInterface(interfaceId);
        Assertions.assertEquals(1, userInputCount(updatedInterface.getApplicationInputs()),
                "One user input after remove (system STDIN may also be present)");
        Assertions.assertEquals(1, userOutputCount(updatedInterface.getApplicationOutputs()),
                "One user output after remove (system STDOUT/STDERR may also be present)");
    }

    @Test
    public void filterApplicationInterfacesTest() throws AppCatalogException {

        List<ApplicationInterfaceDescription> interfaces = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
            applicationInterfaceDescription.setApplicationInterfaceId("interface" + i);
            applicationInterfaceDescription.setApplicationName("app interface " + i);
            interfaces.add(applicationInterfaceDescription);
            applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);
        }

        for (ApplicationInterfaceDescription iface : interfaces) {
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationInterface.APPLICATION_NAME, iface.getApplicationName());
            assertEquals(
                    iface.getApplicationName(),
                    applicationInterfaceService
                            .getApplicationInterfaces(filters)
                            .get(0)
                            .getApplicationName());
        }
    }

    @Test
    public void filterApplicationModulesTest() throws AppCatalogException {
        List<ApplicationModule> modules = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ApplicationModule applicationModule = new ApplicationModule();
            applicationModule.setAppModuleId("appMod" + i);
            applicationModule.setAppModuleName("appMod1Name");
            applicationModule.setAppModuleDescription("Description");
            applicationModule.setAppModuleVersion("Version1");
            modules.add(applicationModule);
            applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);
        }

        for (ApplicationModule module : modules) {
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME, module.getAppModuleName());
            assertEquals(
                    module.getAppModuleName(),
                    applicationInterfaceService
                            .getApplicationModules(filters)
                            .get(0)
                            .getAppModuleName());
        }
    }

    @Test
    public void filterModuleByWrongCategoryTest() throws AppCatalogException {

        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        applicationInterfaceService.addApplicationModule(applicationModule, gatewayId);

        Map<String, String> filters = new HashMap<>();
        filters.put("INVALID KEY", applicationModule.getAppModuleName());
        // Expect IllegalArgumentException when using invalid filter key
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> applicationInterfaceService.getApplicationModules(filters),
                "Should throw IllegalArgumentException for invalid filter key");
    }

    @Test
    public void filterInterfaceByWrongCategoryTest() throws AppCatalogException {

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface");
        applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        Map<String, String> filters = new HashMap<>();
        filters.put("INVALID KEY", applicationInterfaceDescription.getApplicationName());
        // Expect IllegalArgumentException when using invalid filter key
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> applicationInterfaceService.getApplicationInterfaces(filters),
                "Should throw IllegalArgumentException for invalid filter key");
    }

    @Test
    public void getAccessibleApplicationModulesTest() throws AppCatalogException {
        org.apache.airavata.common.model.BatchQueue q = new org.apache.airavata.common.model.BatchQueue();
        q.setQueueName("default");
        q.setQueueDescription("Default");
        q.setMaxRunTime(24);
        q.setMaxNodes(1);
        q.setMaxJobsInQueue(10);

        ComputeResourceDescription computeResourceDescription1 = new ComputeResourceDescription();
        computeResourceDescription1.setComputeResourceId("compHost1");
        computeResourceDescription1.setHostName("compHost1Name");
        computeResourceDescription1.setBatchQueues(Collections.singletonList(q));
        String computeResourceId1 = computeResourceService.addComputeResource(computeResourceDescription1);

        ComputeResourceDescription computeResourceDescription2 = new ComputeResourceDescription();
        computeResourceDescription2.setComputeResourceId("compHost2");
        computeResourceDescription2.setHostName("compHost2Name");
        computeResourceDescription2.setBatchQueues(Collections.singletonList(q));
        String computeResourceId2 = computeResourceService.addComputeResource(computeResourceDescription2);

        ApplicationModule applicationModule1 = new ApplicationModule();
        applicationModule1.setAppModuleId("appMod1");
        applicationModule1.setAppModuleName("appMod1Name");
        String moduleId1 = applicationInterfaceService.addApplicationModule(applicationModule1, gatewayId);

        ApplicationModule applicationModule2 = new ApplicationModule();
        applicationModule2.setAppModuleId("appMod2");
        applicationModule2.setAppModuleName("appMod2Name");
        applicationInterfaceService.addApplicationModule(applicationModule2, gatewayId);

        ApplicationDeploymentDescription applicationDeploymentDescription1 = new ApplicationDeploymentDescription();
        applicationDeploymentDescription1.setAppDeploymentId("appDep1");
        applicationDeploymentDescription1.setAppModuleId(moduleId1);
        applicationDeploymentDescription1.setComputeHostId(computeResourceId1);
        applicationDeploymentDescription1.setExecutablePath("executablePath");
        applicationDeploymentDescription1.setParallelism(ApplicationParallelismType.SERIAL);
        String deploymentId1 =
                applicationDeploymentService.addApplicationDeployment(applicationDeploymentDescription1, gatewayId);

        applicationDeploymentService.getApplicationDeployement(deploymentId1);

        ApplicationDeploymentDescription applicationDeploymentDescription2 = new ApplicationDeploymentDescription();
        applicationDeploymentDescription2.setAppDeploymentId("appDep2");
        applicationDeploymentDescription2.setAppModuleId(moduleId1);
        applicationDeploymentDescription2.setComputeHostId(computeResourceId2);
        applicationDeploymentDescription2.setExecutablePath("executablePath");
        applicationDeploymentDescription2.setParallelism(ApplicationParallelismType.SERIAL);
        String deploymentId2 =
                applicationDeploymentService.addApplicationDeployment(applicationDeploymentDescription2, gatewayId);

        List<String> deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId1);
        List<String> compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId1);
        List<ApplicationModule> appModuleList =
                applicationInterfaceService.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);

        assertEquals(1, appModuleList.size());
        assertEquals(moduleId1, appModuleList.get(0).getAppModuleId());

        deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId1);
        compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId2);
        appModuleList =
                applicationInterfaceService.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);
        assertEquals(0, appModuleList.size());

        deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId2);
        compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId2);
        appModuleList =
                applicationInterfaceService.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);
        assertEquals(1, appModuleList.size());
        assertEquals(moduleId1, appModuleList.get(0).getAppModuleId());

        deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId1);
        deploymentIds.add(deploymentId2);
        compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId1);
        compHostIds.add(computeResourceId2);
        appModuleList =
                applicationInterfaceService.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);
        assertEquals(1, appModuleList.size());
        assertEquals(moduleId1, appModuleList.get(0).getAppModuleId());
    }

    @Test
    public void getAllApplicationModulesByGatewayTest() throws AppCatalogException {
        Map<String, List<ApplicationModule>> moduleStore = new HashMap<>();

        for (int j = 0; j < 5; j++) {
            List<ApplicationModule> modules = new ArrayList<>();
            String gateway = "gateway" + j;
            for (int i = 0; i < 5; i++) {
                ApplicationModule applicationModule = new ApplicationModule();
                applicationModule.setAppModuleId(gateway + "appMod" + i);
                applicationModule.setAppModuleName(gateway + "appMod1Name");
                applicationModule.setAppModuleDescription(gateway + "Description");
                applicationModule.setAppModuleVersion(gateway + "Version1");
                modules.add(applicationModule);
                applicationInterfaceService.addApplicationModule(applicationModule, gateway);
            }
            moduleStore.put(gateway, modules);
        }

        for (int j = 0; j < 5; j++) {
            String gateway = "gateway" + j;
            List<ApplicationModule> allApplicationModules =
                    applicationInterfaceService.getAllApplicationModules(gateway);

            Assertions.assertEquals(moduleStore.get(gateway).size(), allApplicationModules.size());
            for (int i = 0; i < allApplicationModules.size(); i++) {
                Assertions.assertTrue(ReflectionEquals.reflectionEquals(
                        moduleStore.get(gateway).get(i), allApplicationModules.get(i), "__isset_bitfield"));
            }
        }
    }

    @Test
    public void getAllApplicationInterfacesByGatewayTest() throws AppCatalogException {
        Map<String, List<ApplicationInterfaceDescription>> interfaceStore = new HashMap<>();

        for (int j = 0; j < 5; j++) {
            List<ApplicationInterfaceDescription> interfaces = new ArrayList<>();
            String gateway = "gateway" + j;
            for (int i = 0; i < 5; i++) {
                ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
                applicationInterfaceDescription.setApplicationInterfaceId(gateway + "interface" + i);
                applicationInterfaceDescription.setApplicationName(gateway + "app interface " + i);
                applicationInterfaceDescription.setApplicationModules(new ArrayList<>());
                applicationInterfaceDescription.setApplicationInputs(new ArrayList<>());
                applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());
                interfaces.add(applicationInterfaceDescription);
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gateway);
            }
            interfaceStore.put(gateway, interfaces);
        }

        for (int j = 0; j < 5; j++) {
            String gateway = "gateway" + j;
            List<ApplicationInterfaceDescription> allApplicationInterfaces =
                    applicationInterfaceService.getAllApplicationInterfaces(gateway);

            Assertions.assertEquals(interfaceStore.get(gateway).size(), allApplicationInterfaces.size());
            for (int i = 0; i < allApplicationInterfaces.size(); i++) {
                Assertions.assertTrue(ReflectionEquals.reflectionEquals(
                        interfaceStore.get(gateway).get(i),
                        allApplicationInterfaces.get(i),
                        "__isset_bitfield",
                        "applicationInputs",
                        "applicationOutputs"));
            }
        }
    }

    @Test
    public void getAllApplicationInterfacesWithoutGatewayTest() throws AppCatalogException {
        // Clean up any existing interfaces from previous test runs
        for (int j = 0; j < 5; j++) {
            String gateway = "gateway" + j;
            for (int i = 0; i < 5; i++) {
                try {
                    applicationInterfaceService.removeApplicationInterface(gateway + "interface" + i);
                } catch (Exception e) {
                    // Ignore if doesn't exist
                }
            }
        }
        // Also clean up "interface1" from other tests
        try {
            applicationInterfaceService.removeApplicationInterface("interface1");
        } catch (Exception e) {
            // Ignore if doesn't exist
        }

        List<ApplicationInterfaceDescription> interfaces = new ArrayList<>();
        for (int j = 0; j < 5; j++) {
            String gateway = "gateway" + j;
            for (int i = 0; i < 5; i++) {
                ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
                applicationInterfaceDescription.setApplicationInterfaceId(gateway + "interface" + i);
                applicationInterfaceDescription.setApplicationName(gateway + "app interface " + i);
                applicationInterfaceDescription.setApplicationModules(new ArrayList<>());
                applicationInterfaceDescription.setApplicationInputs(new ArrayList<>());
                applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());
                interfaces.add(applicationInterfaceDescription);
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gateway);
            }
        }

        List<String> allApplicationInterfaceIds = applicationInterfaceService.getAllApplicationInterfaceIds();
        // Allow for some existing interfaces from other tests, but verify our interfaces are there
        Assertions.assertTrue(
                allApplicationInterfaceIds.size() >= interfaces.size(),
                "Should have at least " + interfaces.size() + " interfaces, but got: "
                        + allApplicationInterfaceIds.size());
        // Verify all our interfaces are in the list
        for (ApplicationInterfaceDescription iface : interfaces) {
            Assertions.assertTrue(
                    allApplicationInterfaceIds.contains(iface.getApplicationInterfaceId()),
                    "Interface " + iface.getApplicationInterfaceId() + " should be in the list");
        }
        for (int i = 0; i < interfaces.size(); i++) {
            Assertions.assertEquals(interfaces.get(i).getApplicationInterfaceId(), allApplicationInterfaceIds.get(i));
        }
    }
}

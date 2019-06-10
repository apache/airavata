/**
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
 */
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationInterfaceRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationInterfaceRepositoryTest.class);

    private ApplicationInterfaceRepository applicationInterfaceRepository;
    private ComputeResourceRepository computeResourceRepository;
    private ApplicationDeploymentRepository applicationDeploymentRepository;
    private String gatewayId = "testGateway";

    public ApplicationInterfaceRepositoryTest() {
        super(TestBase.Database.APP_CATALOG);
        computeResourceRepository = new ComputeResourceRepository();
        applicationInterfaceRepository = new ApplicationInterfaceRepository();
        applicationDeploymentRepository = new ApplicationDeploymentRepository();
    }

    @Test
    public void addApplicationModuleTest() throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        String moduleId = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);

        ApplicationModule savedAppModule = applicationInterfaceRepository.getApplicationModule(moduleId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(applicationModule, savedAppModule));
    }

    @Test
    public void addApplicationModuleWithEmptyIdTest() throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        String moduleId = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);

        ApplicationModule savedAppModule = applicationInterfaceRepository.getApplicationModule(moduleId);
        Assert.assertNotEquals(applicationModule.getAppModuleName(), savedAppModule.getAppModuleId());
        Assert.assertTrue(savedAppModule.getAppModuleId().startsWith(applicationModule.getAppModuleName()));
    }

    @Test
    public void deleteApplicationModuleTest() throws AppCatalogException {

        Assert.assertNull(applicationInterfaceRepository.getApplicationModule("appMod1"));

        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        String moduleId = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);
        Assert.assertNotNull(applicationInterfaceRepository.getApplicationModule(moduleId));

        Assert.assertTrue(applicationInterfaceRepository.removeApplicationModule("appMod1"));

        Assert.assertNull(applicationInterfaceRepository.getApplicationModule("appMod1"));
    }

    @Test
    public void updateApplicationModuleTest() throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        String moduleId = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);

        ApplicationModule savedAppModule = applicationInterfaceRepository.getApplicationModule(moduleId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(applicationModule, savedAppModule));

        savedAppModule.setAppModuleName("Updated Name");
        savedAppModule.setAppModuleDescription("Updated Description");
        savedAppModule.setAppModuleVersion("new version");

        applicationInterfaceRepository.updateApplicationModule("appMod1", savedAppModule);

        ApplicationModule updatedAppModule = applicationInterfaceRepository.getApplicationModule(moduleId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(savedAppModule, updatedAppModule));
    }

    @Test
    public void addApplicationInterfaceTest() throws AppCatalogException {
        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");
        applicationInterfaceDescription.setApplicationModules(new ArrayList<>());
        applicationInterfaceDescription.setApplicationInputs(new ArrayList<>());
        applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());

        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);
        assertEquals(applicationInterfaceDescription.getApplicationInterfaceId(), interfaceId);

        ApplicationInterfaceDescription savedInterface = applicationInterfaceRepository.getApplicationInterface(interfaceId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(applicationInterfaceDescription, savedInterface, "__isset_bitfield"));
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

        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);
        assertTrue(MessageFormat.format("{0} does not start with {1}", interfaceId, "app_interface_1"),
                interfaceId.startsWith("app_interface_1"));
    }

    @Test
    public void deleteApplicationInterfaceTest() throws AppCatalogException {

        Assert.assertNull(applicationInterfaceRepository.getApplicationModule("interface1"));

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
        applicationInterfaceDescription.addToApplicationInputs(input);

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
        applicationInterfaceDescription.addToApplicationOutputs(output);

        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        Assert.assertNotNull(applicationInterfaceRepository.getApplicationInterface(interfaceId));
        Assert.assertTrue(applicationInterfaceRepository.removeApplicationInterface(interfaceId));
        Assert.assertNull(applicationInterfaceRepository.getApplicationInterface(interfaceId));
    }

    @Test
    public void addModulesToInterfaceTest() throws AppCatalogException {
        ApplicationModule applicationModule1 = new ApplicationModule();
        applicationModule1.setAppModuleId("appMod1");
        applicationModule1.setAppModuleName("appMod1Name");
        String moduleId1 = applicationInterfaceRepository.addApplicationModule(applicationModule1, gatewayId);

        ApplicationModule applicationModule2 = new ApplicationModule();
        applicationModule2.setAppModuleId("appMod2");
        applicationModule2.setAppModuleName("appMod2Name");
        String moduleId2 = applicationInterfaceRepository.addApplicationModule(applicationModule2, gatewayId);

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        applicationInterfaceRepository.addApplicationModuleMapping(moduleId1, interfaceId);
        applicationInterfaceRepository.addApplicationModuleMapping(moduleId2, interfaceId);

        ApplicationInterfaceDescription savedInterface = applicationInterfaceRepository.getApplicationInterface(interfaceId);

        Assert.assertEquals(savedInterface.getApplicationModules().get(0), applicationModule1.getAppModuleId());
        Assert.assertEquals(savedInterface.getApplicationModules().get(1), applicationModule2.getAppModuleId());
    }

    @Test
    public void addInputsOutputsToInterfaceTest() throws AppCatalogException {

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);

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
        // TODO missing field
        //input.setStorageResourceId("Storage resource id");

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
        // TODO missing field
        //output.setStorageResourceId("Storage resource id");

        applicationInterfaceDescription.setApplicationInputs(Collections.singletonList(input));
        applicationInterfaceDescription.setApplicationOutputs(Collections.singletonList(output));

        applicationInterfaceRepository.updateApplicationInterface(interfaceId, applicationInterfaceDescription);

        ApplicationInterfaceDescription savedInterface = applicationInterfaceRepository.getApplicationInterface(interfaceId);
        Assert.assertEquals(1, savedInterface.getApplicationInputsSize());
        Assert.assertEquals(1, savedInterface.getApplicationOutputsSize());

        Assert.assertTrue(EqualsBuilder.reflectionEquals(input, savedInterface.getApplicationInputs().get(0), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(output, savedInterface.getApplicationOutputs().get(0), "__isset_bitfield"));

        List<InputDataObjectType> savedInputs = applicationInterfaceRepository.getApplicationInputs(interfaceId);
        List<OutputDataObjectType> savedOutputs = applicationInterfaceRepository.getApplicationOutputs(interfaceId);

        Assert.assertEquals(1, savedInputs.size());
        Assert.assertEquals(1, savedOutputs.size());

        Assert.assertTrue(EqualsBuilder.reflectionEquals(input, savedInputs.get(0), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(output, savedOutputs.get(0), "__isset_bitfield"));

    }

    @Test
    public void addAndRemoveInputsOutputsToInterfaceTest() throws AppCatalogException {

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);

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

        applicationInterfaceRepository.updateApplicationInterface(interfaceId, applicationInterfaceDescription);

        ApplicationInterfaceDescription savedInterface = applicationInterfaceRepository.getApplicationInterface(interfaceId);
        Assert.assertEquals(2, savedInterface.getApplicationInputsSize());
        Assert.assertEquals(2, savedInterface.getApplicationOutputsSize());

        savedInterface.setApplicationInputs(Arrays.asList(input));
        savedInterface.setApplicationOutputs(Arrays.asList(output));

        applicationInterfaceRepository.updateApplicationInterface(interfaceId, savedInterface);
        ApplicationInterfaceDescription updatedInterface = applicationInterfaceRepository.getApplicationInterface(interfaceId);
        Assert.assertEquals(1, updatedInterface.getApplicationInputsSize());
        Assert.assertEquals(1, updatedInterface.getApplicationOutputsSize());

    }

    @Test
    public void filterApplicationInterfacesTest() throws AppCatalogException {

        List<ApplicationInterfaceDescription> interfaces = new ArrayList<>();
        for (int i = 0 ;i < 5 ;i ++) {
            ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
            applicationInterfaceDescription.setApplicationInterfaceId("interface" + i);
            applicationInterfaceDescription.setApplicationName("app interface " + i);
            interfaces.add(applicationInterfaceDescription);
            applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);
        }

        for (ApplicationInterfaceDescription iface : interfaces) {
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationInterface.APPLICATION_NAME, iface.getApplicationName());
            assertEquals(iface.getApplicationName(), applicationInterfaceRepository.getApplicationInterfaces(filters).get(0).getApplicationName());
        }
    }

    @Test
    public void filterApplicationModulesTest() throws AppCatalogException {
        List<ApplicationModule> modules = new ArrayList<>();
        for (int i = 0 ;i < 5 ;i ++) {
            ApplicationModule applicationModule = new ApplicationModule();
            applicationModule.setAppModuleId("appMod" + i);
            applicationModule.setAppModuleName("appMod1Name");
            applicationModule.setAppModuleDescription("Description");
            applicationModule.setAppModuleVersion("Version1");
            modules.add(applicationModule);
            applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);
        }

        for (ApplicationModule module : modules) {
            Map<String, String> filters = new HashMap<>();
            filters.put(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME, module.getAppModuleName());
            assertEquals(module.getAppModuleName(),
                    applicationInterfaceRepository.getApplicationModules(filters).get(0).getAppModuleName());
        }
    }

    @Test
    public void filterModuleByWrongCategoryTest() throws AppCatalogException {

        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationModule.setAppModuleDescription("Description");
        applicationModule.setAppModuleVersion("Version1");
        applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);

        Map<String, String> filters = new HashMap<>();
        filters.put("INVALID KEY", applicationModule.getAppModuleName());
        try {
            applicationInterfaceRepository.getApplicationModules(filters).get(0).getAppModuleName();
            Assert.fail("Expected to throw an exception");
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    @Test
    public void filterInterfaceByWrongCategoryTest() throws AppCatalogException {

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface");
        applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        Map<String, String> filters = new HashMap<>();
        filters.put("INVALID KEY", applicationInterfaceDescription.getApplicationName());
        try {
            applicationInterfaceRepository.getApplicationInterfaces(filters).get(0).getApplicationName();
            Assert.fail("Expected to throw an exception");
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    @Test
    public void getAccessibleApplicationModulesTest() throws AppCatalogException {

        ComputeResourceDescription computeResourceDescription1 = new ComputeResourceDescription();
        computeResourceDescription1.setComputeResourceId("compHost1");
        computeResourceDescription1.setHostName("compHost1Name");
        String computeResourceId1 = computeResourceRepository.addComputeResource(computeResourceDescription1);

        ComputeResourceDescription computeResourceDescription2 = new ComputeResourceDescription();
        computeResourceDescription2.setComputeResourceId("compHost2");
        computeResourceDescription2.setHostName("compHost2Name");
        String computeResourceId2 = computeResourceRepository.addComputeResource(computeResourceDescription2);

        ApplicationModule applicationModule1 = new ApplicationModule();
        applicationModule1.setAppModuleId("appMod1");
        applicationModule1.setAppModuleName("appMod1Name");
        String moduleId1 = applicationInterfaceRepository.addApplicationModule(applicationModule1, gatewayId);

        ApplicationModule applicationModule2 = new ApplicationModule();
        applicationModule2.setAppModuleId("appMod2");
        applicationModule2.setAppModuleName("appMod2Name");
        String moduleId2 = applicationInterfaceRepository.addApplicationModule(applicationModule2, gatewayId);

        ApplicationDeploymentDescription applicationDeploymentDescription1 = new ApplicationDeploymentDescription();
        applicationDeploymentDescription1.setAppDeploymentId("appDep1");
        applicationDeploymentDescription1.setAppModuleId(moduleId1);
        applicationDeploymentDescription1.setComputeHostId(computeResourceId1);
        applicationDeploymentDescription1.setExecutablePath("executablePath");
        applicationDeploymentDescription1.setParallelism(ApplicationParallelismType.SERIAL);
        String deploymentId1 = applicationDeploymentRepository.addApplicationDeployment(applicationDeploymentDescription1, gatewayId);

        ApplicationDeploymentDescription applicationDeployement = applicationDeploymentRepository.getApplicationDeployement(deploymentId1);

        ApplicationDeploymentDescription applicationDeploymentDescription2 = new ApplicationDeploymentDescription();
        applicationDeploymentDescription2.setAppDeploymentId("appDep2");
        applicationDeploymentDescription2.setAppModuleId(moduleId1);
        applicationDeploymentDescription2.setComputeHostId(computeResourceId2);
        applicationDeploymentDescription2.setExecutablePath("executablePath");
        applicationDeploymentDescription2.setParallelism(ApplicationParallelismType.SERIAL);
        String deploymentId2 = applicationDeploymentRepository.addApplicationDeployment(applicationDeploymentDescription2, gatewayId);

        List<String> deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId1);
        List<String> compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId1);
        List<ApplicationModule> appModuleList = applicationInterfaceRepository.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);

        assertEquals(1, appModuleList.size());
        assertEquals(moduleId1, appModuleList.get(0).getAppModuleId());

        deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId1);
        compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId2);
        appModuleList = applicationInterfaceRepository.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);
        assertEquals(0, appModuleList.size());

        deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId2);
        compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId2);
        appModuleList = applicationInterfaceRepository.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);
        assertEquals(1, appModuleList.size());
        assertEquals(moduleId1, appModuleList.get(0).getAppModuleId());

        deploymentIds = new ArrayList<>();
        deploymentIds.add(deploymentId1);
        deploymentIds.add(deploymentId2);
        compHostIds = new ArrayList<>();
        compHostIds.add(computeResourceId1);
        compHostIds.add(computeResourceId2);
        appModuleList = applicationInterfaceRepository.getAccessibleApplicationModules(gatewayId, deploymentIds, compHostIds);
        assertEquals(1, appModuleList.size());
        assertEquals(moduleId1, appModuleList.get(0).getAppModuleId());
    }

    @Test
    public void getAllApplicationModulesByGatewayTest() throws AppCatalogException {
        Map<String, List<ApplicationModule>> moduleStore = new HashMap<>();

        for (int j = 0; j < 5; j++) {
            List<ApplicationModule> modules = new ArrayList<>();
            String gateway  = "gateway" + j;
            for (int i = 0; i < 5; i++) {
                ApplicationModule applicationModule = new ApplicationModule();
                applicationModule.setAppModuleId(gateway + "appMod" + i);
                applicationModule.setAppModuleName(gateway + "appMod1Name");
                applicationModule.setAppModuleDescription(gateway + "Description");
                applicationModule.setAppModuleVersion(gateway + "Version1");
                modules.add(applicationModule);
                applicationInterfaceRepository.addApplicationModule(applicationModule, gateway);
            }
            moduleStore.put(gateway, modules);
        }

        for (int j = 0; j < 5; j++) {
            String gateway  = "gateway" + j;
            List<ApplicationModule> allApplicationModules = applicationInterfaceRepository.getAllApplicationModules(gateway);

            Assert.assertEquals(moduleStore.get(gateway).size(), allApplicationModules.size());
            for (int i = 0; i < allApplicationModules.size(); i++) {
                Assert.assertTrue(EqualsBuilder.reflectionEquals(moduleStore.get(gateway).get(i), allApplicationModules.get(i), "__isset_bitfield"));
            }
        }
    }

    @Test
    public void getAllApplicationInterfacesByGatewayTest() throws AppCatalogException {
        Map<String, List<ApplicationInterfaceDescription>> interfaceStore = new HashMap<>();

        for (int j = 0; j < 5; j++) {
            List<ApplicationInterfaceDescription> interfaces = new ArrayList<>();
            String gateway  = "gateway" + j;
            for (int i = 0; i < 5; i++) {
                ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
                applicationInterfaceDescription.setApplicationInterfaceId(gateway + "interface" + i);
                applicationInterfaceDescription.setApplicationName(gateway + "app interface " + i);
                applicationInterfaceDescription.setApplicationModules(new ArrayList<>());
                applicationInterfaceDescription.setApplicationInputs(new ArrayList<>());
                applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());
                interfaces.add(applicationInterfaceDescription);
                applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gateway);
            }
            interfaceStore.put(gateway, interfaces);
        }

        for (int j = 0; j < 5; j++) {
            String gateway  = "gateway" + j;
            List<ApplicationInterfaceDescription> allApplicationInterfaces = applicationInterfaceRepository.getAllApplicationInterfaces(gateway);

            Assert.assertEquals(interfaceStore.get(gateway).size(), allApplicationInterfaces.size());
            for (int i = 0; i < allApplicationInterfaces.size(); i++) {
                Assert.assertTrue(EqualsBuilder.reflectionEquals(interfaceStore.get(gateway).get(i), allApplicationInterfaces.get(i), "__isset_bitfield"));
            }
        }
    }

    @Test
    public void getAllApplicationInterfacesWithoutGatewayTest() throws AppCatalogException {

        List<ApplicationInterfaceDescription> interfaces = new ArrayList<>();
        for (int j = 0; j < 5; j++) {
            String gateway  = "gateway" + j;
            for (int i = 0; i < 5; i++) {
                ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
                applicationInterfaceDescription.setApplicationInterfaceId(gateway + "interface" + i);
                applicationInterfaceDescription.setApplicationName(gateway + "app interface " + i);
                applicationInterfaceDescription.setApplicationModules(new ArrayList<>());
                applicationInterfaceDescription.setApplicationInputs(new ArrayList<>());
                applicationInterfaceDescription.setApplicationOutputs(new ArrayList<>());
                interfaces.add(applicationInterfaceDescription);
                applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gateway);
            }
        }

        List<String> allApplicationInterfaceIds = applicationInterfaceRepository.getAllApplicationInterfaceIds();
        Assert.assertEquals(interfaces.size(), allApplicationInterfaceIds.size());
        for (int i = 0; i < interfaces.size(); i++) {
            Assert.assertEquals(interfaces.get(i).getApplicationInterfaceId(), allApplicationInterfaceIds.get(i));
        }
    }
}

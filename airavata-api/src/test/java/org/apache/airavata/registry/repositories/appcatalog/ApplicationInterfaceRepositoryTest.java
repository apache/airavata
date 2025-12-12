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
import java.util.*;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ApplicationDeploymentService;
import org.apache.airavata.registry.services.ApplicationInterfaceService;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            ApplicationInterfaceRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.registryService.enabled=false",
            "services.userprofile.enabled=false",
            "services.groupmanager.enabled=false",
            "services.iam.enabled=false",
            "services.orchestrator.enabled=false",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ApplicationInterfaceRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern =
                                "org\\.apache\\.airavata\\.(monitor|helix|sharing\\.migrator|credential|profile|security|accountprovisioning)\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.service\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
        org.apache.airavata.config.DozerMapperConfig.class
    })
    static class TestConfiguration {}

    private final ApplicationInterfaceService applicationInterfaceService;
    private final ComputeResourceService computeResourceService;
    private final ApplicationDeploymentService applicationDeploymentService;

    private String gatewayId = "testGateway";

    public ApplicationInterfaceRepositoryTest(
            ApplicationInterfaceService applicationInterfaceService,
            ComputeResourceService computeResourceService,
            ApplicationDeploymentService applicationDeploymentService) {
        super(TestBase.Database.APP_CATALOG);
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
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(applicationModule, savedAppModule));
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
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(applicationModule, savedAppModule));

        savedAppModule.setAppModuleName("Updated Name");
        savedAppModule.setAppModuleDescription("Updated Description");
        savedAppModule.setAppModuleVersion("new version");

        applicationInterfaceService.updateApplicationModule("appMod1", savedAppModule);

        ApplicationModule updatedAppModule = applicationInterfaceService.getApplicationModule(moduleId);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(savedAppModule, updatedAppModule));
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
        Assertions.assertTrue(
                EqualsBuilder.reflectionEquals(applicationInterfaceDescription, savedInterface, "__isset_bitfield"));
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

        String interfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        Assertions.assertNotNull(applicationInterfaceService.getApplicationInterface(interfaceId));
        Assertions.assertTrue(applicationInterfaceService.removeApplicationInterface(interfaceId));
        Assertions.assertNull(applicationInterfaceService.getApplicationInterface(interfaceId));
    }

    @Test
    public void addModulesToInterfaceTest() throws AppCatalogException {
        ApplicationModule applicationModule1 = new ApplicationModule();
        applicationModule1.setAppModuleId("appMod1");
        applicationModule1.setAppModuleName("appMod1Name");
        String moduleId1 = applicationInterfaceService.addApplicationModule(applicationModule1, gatewayId);

        ApplicationModule applicationModule2 = new ApplicationModule();
        applicationModule2.setAppModuleId("appMod2");
        applicationModule2.setAppModuleName("appMod2Name");
        String moduleId2 = applicationInterfaceService.addApplicationModule(applicationModule2, gatewayId);

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String interfaceId =
                applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        applicationInterfaceService.addApplicationModuleMapping(moduleId1, interfaceId);
        applicationInterfaceService.addApplicationModuleMapping(moduleId2, interfaceId);

        ApplicationInterfaceDescription savedInterface =
                applicationInterfaceService.getApplicationInterface(interfaceId);

        Assertions.assertEquals(savedInterface.getApplicationModules().get(0), applicationModule1.getAppModuleId());
        Assertions.assertEquals(savedInterface.getApplicationModules().get(1), applicationModule2.getAppModuleId());
    }

    @Test
    public void addInputsOutputsToInterfaceTest() throws AppCatalogException {

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
        // TODO missing field
        // input.setStorageResourceId("Storage resource id");

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
        // output.setStorageResourceId("Storage resource id");

        applicationInterfaceDescription.setApplicationInputs(Collections.singletonList(input));
        applicationInterfaceDescription.setApplicationOutputs(Collections.singletonList(output));

        applicationInterfaceService.updateApplicationInterface(interfaceId, applicationInterfaceDescription);

        ApplicationInterfaceDescription savedInterface =
                applicationInterfaceService.getApplicationInterface(interfaceId);
        Assertions.assertEquals(1, savedInterface.getApplicationInputsSize());
        Assertions.assertEquals(1, savedInterface.getApplicationOutputsSize());

        Assertions.assertTrue(EqualsBuilder.reflectionEquals(
                input, savedInterface.getApplicationInputs().get(0), "__isset_bitfield"));
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(
                output, savedInterface.getApplicationOutputs().get(0), "__isset_bitfield"));

        List<InputDataObjectType> savedInputs = applicationInterfaceService.getApplicationInputs(interfaceId);
        List<OutputDataObjectType> savedOutputs = applicationInterfaceService.getApplicationOutputs(interfaceId);

        Assertions.assertEquals(1, savedInputs.size());
        Assertions.assertEquals(1, savedOutputs.size());

        Assertions.assertTrue(EqualsBuilder.reflectionEquals(input, savedInputs.get(0), "__isset_bitfield"));
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(output, savedOutputs.get(0), "__isset_bitfield"));
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
        Assertions.assertEquals(2, savedInterface.getApplicationInputsSize());
        Assertions.assertEquals(2, savedInterface.getApplicationOutputsSize());

        savedInterface.setApplicationInputs(Arrays.asList(input));
        savedInterface.setApplicationOutputs(Arrays.asList(output));

        applicationInterfaceService.updateApplicationInterface(interfaceId, savedInterface);
        ApplicationInterfaceDescription updatedInterface =
                applicationInterfaceService.getApplicationInterface(interfaceId);
        Assertions.assertEquals(1, updatedInterface.getApplicationInputsSize());
        Assertions.assertEquals(1, updatedInterface.getApplicationOutputsSize());
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
        try {
            applicationInterfaceService.getApplicationModules(filters).get(0).getAppModuleName();
            Assertions.fail("Expected to throw an exception");
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    @Test
    public void filterInterfaceByWrongCategoryTest() throws AppCatalogException {

        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface");
        applicationInterfaceService.addApplicationInterface(applicationInterfaceDescription, gatewayId);

        Map<String, String> filters = new HashMap<>();
        filters.put("INVALID KEY", applicationInterfaceDescription.getApplicationName());
        try {
            applicationInterfaceService.getApplicationInterfaces(filters).get(0).getApplicationName();
            Assertions.fail("Expected to throw an exception");
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    @Test
    public void getAccessibleApplicationModulesTest() throws AppCatalogException {

        ComputeResourceDescription computeResourceDescription1 = new ComputeResourceDescription();
        computeResourceDescription1.setComputeResourceId("compHost1");
        computeResourceDescription1.setHostName("compHost1Name");
        String computeResourceId1 = computeResourceService.addComputeResource(computeResourceDescription1);

        ComputeResourceDescription computeResourceDescription2 = new ComputeResourceDescription();
        computeResourceDescription2.setComputeResourceId("compHost2");
        computeResourceDescription2.setHostName("compHost2Name");
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
                Assertions.assertTrue(EqualsBuilder.reflectionEquals(
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
                Assertions.assertTrue(EqualsBuilder.reflectionEquals(
                        interfaceStore.get(gateway).get(i), allApplicationInterfaces.get(i), "__isset_bitfield"));
            }
        }
    }

    @Test
    public void getAllApplicationInterfacesWithoutGatewayTest() throws AppCatalogException {

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
        Assertions.assertEquals(interfaces.size(), allApplicationInterfaceIds.size());
        for (int i = 0; i < interfaces.size(); i++) {
            Assertions.assertEquals(interfaces.get(i).getApplicationInterfaceId(), allApplicationInterfaceIds.get(i));
        }
    }
}

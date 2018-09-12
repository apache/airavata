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
import org.apache.airavata.model.appcatalog.appdeployment.CommandObject;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.appinterface.application_interface_modelConstants;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class ApplicationDeploymentRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeploymentRepositoryTest.class);

    private ComputeResourceRepository computeResourceRepository;
    private ApplicationInterfaceRepository applicationInterfaceRepository;
    private ApplicationDeploymentRepository applicationDeploymentRepository;
    private String gatewayId = "testGateway";

    public ApplicationDeploymentRepositoryTest() {
        super(Database.APP_CATALOG);
        computeResourceRepository = new ComputeResourceRepository();
        applicationInterfaceRepository = new ApplicationInterfaceRepository();
        applicationDeploymentRepository = new ApplicationDeploymentRepository();
    }

    private String addSampleApplicationModule(String tag) throws AppCatalogException {
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod" + tag);
        applicationModule.setAppModuleName("appModName" + tag);
        return applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);
    }

    private String addSampleComputeResource(String tag) throws AppCatalogException {
        ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
        computeResourceDescription.setComputeResourceId("compHost" + tag);
        computeResourceDescription.setHostName("compHostName" + tag);
        return computeResourceRepository.addComputeResource(computeResourceDescription);
    }

    private boolean deepCompareDeployment(ApplicationDeploymentDescription expected, ApplicationDeploymentDescription actual) {
        boolean equals = true;
        equals = equals && EqualsBuilder.reflectionEquals(expected, actual,
                "moduleLoadCmds", "libPrependPaths", "libAppendPaths" ,"setEnvironment" ,"preJobCommands"
                ,"postJobCommands", "__isset_bitfield");
        equals = equals && deepCompareLists(expected.getSetEnvironment(), actual.getSetEnvironment(), Comparator.comparingInt(SetEnvPaths::getEnvPathOrder));
        equals = equals && deepCompareLists(expected.getLibPrependPaths(), actual.getLibPrependPaths(), Comparator.comparingInt(SetEnvPaths::getEnvPathOrder));
        equals = equals && deepCompareLists(expected.getLibAppendPaths(), actual.getLibAppendPaths(), Comparator.comparingInt(SetEnvPaths::getEnvPathOrder));
        equals = equals && deepCompareLists(expected.getModuleLoadCmds(), actual.getModuleLoadCmds(), Comparator.comparingInt(CommandObject::getCommandOrder));
        equals = equals && deepCompareLists(expected.getPreJobCommands(), actual.getPreJobCommands(), Comparator.comparingInt(CommandObject::getCommandOrder));
        equals = equals && deepCompareLists(expected.getPostJobCommands(), actual.getPostJobCommands(), Comparator.comparingInt(CommandObject::getCommandOrder));
        return equals;
    }

    private <T> boolean deepCompareLists(List<T> expected, List<T> actual, Comparator<? super T> c) {

        List<T> expectedCopy = new ArrayList<>(expected);
        expectedCopy.sort(c);
        List<T> actualCopy = new ArrayList<>(actual);
        actualCopy.sort(c);
        return EqualsBuilder.reflectionEquals(expectedCopy, actualCopy);
    }

    private ApplicationDeploymentDescription prepareSampleDeployment(String tag, String applicationModule, String computeResource) {
        CommandObject moduleLoadCmd = new CommandObject();
        moduleLoadCmd.setCommand("moduleLoadCmd");
        moduleLoadCmd.setCommandOrder(1);

        SetEnvPaths libPrependPath = new SetEnvPaths();
        libPrependPath.setName("libPrependPath");
        libPrependPath.setValue("libPrependPathValue");
        libPrependPath.setEnvPathOrder(1);
        SetEnvPaths libAppendPath = new SetEnvPaths();
        libAppendPath.setName("libAppendPath");
        libAppendPath.setValue("libAppendPathValue");
        libAppendPath.setEnvPathOrder(2);

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

        Assert.assertNull(applicationDeploymentRepository.getApplicationDeployement("appDep1"));
        String applicationModule = addSampleApplicationModule("1");
        String computeResource =  addSampleComputeResource("1");

        ApplicationDeploymentDescription deployment = prepareSampleDeployment("1", applicationModule, computeResource);
        String deploymentId = applicationDeploymentRepository.addApplicationDeployment(deployment, gatewayId);
        ApplicationDeploymentDescription savedDeployment = applicationDeploymentRepository.getApplicationDeployement("appDep1");
        Assert.assertNotNull(savedDeployment);
        Assert.assertTrue(deepCompareDeployment(deployment, savedDeployment));
    }

    @Test
    public void createAppDeploymentWithDefaultIdTest() throws AppCatalogException {

        String applicationModule = addSampleApplicationModule("1");
        String computeResource =  addSampleComputeResource("1");

        ApplicationDeploymentDescription deployment = prepareSampleDeployment("1", applicationModule, computeResource);
        deployment.setAppDeploymentId(application_interface_modelConstants.DEFAULT_ID);
        String deploymentId = applicationDeploymentRepository.addApplicationDeployment(deployment, gatewayId);
        Assert.assertNotEquals(deploymentId, application_interface_modelConstants.DEFAULT_ID);
        Assert.assertEquals("compHostName1" + "_" + applicationModule, deploymentId);
    }

    @Test
    public void updateAppDeploymentTest() throws AppCatalogException {
        String applicationModule = addSampleApplicationModule("1");
        String computeResource =  addSampleComputeResource("1");

        ApplicationDeploymentDescription deployment = prepareSampleDeployment("1", applicationModule, computeResource);

        String deploymentId = applicationDeploymentRepository.addApplicationDeployment(deployment, gatewayId);

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

        applicationDeploymentRepository.updateApplicationDeployment(deploymentId, deployment);

        ApplicationDeploymentDescription updatedDeployment = applicationDeploymentRepository.getApplicationDeployement(deploymentId);

        Assert.assertTrue(deepCompareDeployment(deployment, updatedDeployment));
    }

    @Test
    public void listAllDeployments() throws AppCatalogException {

        List<ApplicationDeploymentDescription> allDeployments = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            String applicationModule = addSampleApplicationModule(i + "");
            String computeResource = addSampleComputeResource(i + "");
            ApplicationDeploymentDescription deployment = prepareSampleDeployment(i + "", applicationModule, computeResource);
            allDeployments.add(deployment);
            String savedDeploymentId = applicationDeploymentRepository.addApplicationDeployment(deployment, gatewayId);
            Assert.assertEquals(deployment.getAppDeploymentId(), savedDeploymentId);
        }

        List<ApplicationDeploymentDescription> appDeploymentList = applicationDeploymentRepository.getAllApplicationDeployements(gatewayId);
        List<String> appDeploymentIds = applicationDeploymentRepository.getAllApplicationDeployementIds();

        Assert.assertEquals(allDeployments.size(), appDeploymentList.size());
        Assert.assertEquals(allDeployments.size(), appDeploymentIds.size());

        for (int i = 0; i < allDeployments.size(); i++) {
            Assert.assertTrue(deepCompareDeployment(allDeployments.get(i), appDeploymentList.get(i)));
            Assert.assertEquals(allDeployments.get(i).getAppDeploymentId(), appDeploymentIds.get(i));
        }
    }

    @Test
    public void filterApplicationDeploymentsTest() throws AppCatalogException {

        String applicationModule1 = addSampleApplicationModule("1");
        String computeResource1 =  addSampleComputeResource("1");
        String applicationModule2 = addSampleApplicationModule("2");
        String computeResource2 =  addSampleComputeResource("2");

        ApplicationDeploymentDescription deployment1 = prepareSampleDeployment( "1", applicationModule1, computeResource1);
        ApplicationDeploymentDescription deployment2 = prepareSampleDeployment( "2", applicationModule1, computeResource2);
        ApplicationDeploymentDescription deployment3 = prepareSampleDeployment( "3", applicationModule2, computeResource2);

        applicationDeploymentRepository.saveApplicationDeployment(deployment1, gatewayId);
        applicationDeploymentRepository.saveApplicationDeployment(deployment2, gatewayId);
        applicationDeploymentRepository.saveApplicationDeployment(deployment3, gatewayId);

        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule1);
        List<ApplicationDeploymentDescription> filteredDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
        Assert.assertEquals(2, filteredDeployments.size());
        Assert.assertTrue(deepCompareDeployment(deployment1, filteredDeployments.get(0)));
        Assert.assertTrue(deepCompareDeployment(deployment2, filteredDeployments.get(1)));

        filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule2);
        filteredDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
        Assert.assertEquals(1, filteredDeployments.size());
        Assert.assertTrue(deepCompareDeployment(deployment3, filteredDeployments.get(0)));

        filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, computeResource1);
        filteredDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
        Assert.assertEquals(1, filteredDeployments.size());
        Assert.assertTrue(deepCompareDeployment(deployment1, filteredDeployments.get(0)));

        filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, computeResource2);
        filteredDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
        Assert.assertEquals(2, filteredDeployments.size());
        Assert.assertTrue(deepCompareDeployment(deployment2, filteredDeployments.get(0)));
        Assert.assertTrue(deepCompareDeployment(deployment3, filteredDeployments.get(1)));

        filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule1);
        filters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, computeResource2);
        filteredDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
        Assert.assertEquals(1, filteredDeployments.size());
        Assert.assertTrue(deepCompareDeployment(deployment2, filteredDeployments.get(0)));

        filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule1);
        filters.put("INVALID FIELD", computeResource2);
        try {
            filteredDeployments = applicationDeploymentRepository.getApplicationDeployments(filters);
            Assert.fail();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void deleteApplicationDeploymentTest() throws AppCatalogException {

        String applicationModule = addSampleApplicationModule("1");
        String computeResource =  addSampleComputeResource("1");
        ApplicationDeploymentDescription deployment = prepareSampleDeployment( "1", applicationModule, computeResource);

        Assert.assertNull(applicationDeploymentRepository.getApplicationDeployement(deployment.getAppDeploymentId()));

        applicationDeploymentRepository.addApplicationDeployment(deployment, gatewayId);
        Assert.assertNotNull(applicationDeploymentRepository.getApplicationDeployement(deployment.getAppDeploymentId()));
        applicationDeploymentRepository.removeAppDeployment(deployment.getAppDeploymentId());
        Assert.assertNull(applicationInterfaceRepository.getApplicationInterface(deployment.getAppDeploymentId()));
    }

    @Test
    public void accessibleDeploymentTest() throws AppCatalogException {
        String applicationModule1 = addSampleApplicationModule("1");
        String computeResource1 =  addSampleComputeResource("1");
        String applicationModule2 = addSampleApplicationModule("2");
        String computeResource2 =  addSampleComputeResource("2");

        ApplicationDeploymentDescription deployment1 = prepareSampleDeployment( "1", applicationModule1, computeResource1);
        ApplicationDeploymentDescription deployment2 = prepareSampleDeployment( "2", applicationModule1, computeResource2);
        ApplicationDeploymentDescription deployment3 = prepareSampleDeployment( "3", applicationModule2, computeResource2);

        applicationDeploymentRepository.saveApplicationDeployment(deployment1, gatewayId);
        applicationDeploymentRepository.saveApplicationDeployment(deployment2, gatewayId);
        applicationDeploymentRepository.saveApplicationDeployment(deployment3, gatewayId);

        List<String> accessibleAppIds = new ArrayList<>();
        accessibleAppIds.add(deployment1.getAppDeploymentId());
        accessibleAppIds.add(deployment2.getAppDeploymentId());
        accessibleAppIds.add(deployment3.getAppDeploymentId());

        List<String> accessibleCompHostIds = new ArrayList<>();
        accessibleCompHostIds.add(computeResource1);

        List<ApplicationDeploymentDescription> accessibleApplicationDeployments = applicationDeploymentRepository
                .getAccessibleApplicationDeployments(gatewayId, accessibleAppIds, accessibleCompHostIds);

        assertTrue(accessibleApplicationDeployments.size() == 1);
        assertTrue(deepCompareDeployment(deployment1, accessibleApplicationDeployments.get(0)));

        accessibleCompHostIds = new ArrayList<>();
        accessibleCompHostIds.add(computeResource2);

        accessibleApplicationDeployments = applicationDeploymentRepository
                .getAccessibleApplicationDeployments(gatewayId, accessibleAppIds, accessibleCompHostIds);

        assertTrue(accessibleApplicationDeployments.size() == 2);
        assertTrue(deepCompareDeployment(deployment2, accessibleApplicationDeployments.get(0)));
        assertTrue(deepCompareDeployment(deployment3, accessibleApplicationDeployments.get(1)));
    }
}
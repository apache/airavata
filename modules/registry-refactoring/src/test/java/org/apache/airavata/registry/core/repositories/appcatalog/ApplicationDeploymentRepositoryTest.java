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
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.repositories.appcatalog.util.Initialize;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.*;

public class ApplicationDeploymentRepositoryTest {

    private static Initialize initialize;
    private ComputeResourceRepository computeResourceRepository;
    private ApplicationInterfaceRepository applicationInterfaceRepository;
    private ApplicationDeploymentRepository applicationDeploymentRepository;
    private String gatewayId = "testGateway";
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeploymentRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            computeResourceRepository = new ComputeResourceRepository();
            applicationInterfaceRepository = new ApplicationInterfaceRepository();
            applicationDeploymentRepository = new ApplicationDeploymentRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void ApplicationDeploymentRepositoryTest() throws AppCatalogException {
        ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
        computeResourceDescription.setComputeResourceId("compHost1");
        computeResourceDescription.setHostName("compHost1Name");
        String computeResourceId = computeResourceRepository.addComputeResource(computeResourceDescription);

        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);

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

        ApplicationDeploymentDescription testAppDeploymentDesc1 = new ApplicationDeploymentDescription();
        testAppDeploymentDesc1.setAppDeploymentId("appDep1");
        testAppDeploymentDesc1.setAppDeploymentDescription("test application deployment1");
        testAppDeploymentDesc1.setAppModuleId(applicationModule.getAppModuleId());
        testAppDeploymentDesc1.setComputeHostId(computeResourceId);
        testAppDeploymentDesc1.setExecutablePath("executablePath1");
        testAppDeploymentDesc1.setParallelism(ApplicationParallelismType.SERIAL);
        testAppDeploymentDesc1.setModuleLoadCmds(Arrays.asList(moduleLoadCmd));
        testAppDeploymentDesc1.setLibPrependPaths(Arrays.asList(libPrependPath));
        testAppDeploymentDesc1.setLibAppendPaths(Arrays.asList(libAppendPath));
        testAppDeploymentDesc1.setPreJobCommands(Arrays.asList(preJobCommand));
        testAppDeploymentDesc1.setPostJobCommands(Arrays.asList(postJobCommand));
        testAppDeploymentDesc1.setSetEnvironment(Arrays.asList(setEnvironment));
        testAppDeploymentDesc1.setDefaultQueueName("queue1");
        testAppDeploymentDesc1.setDefaultCPUCount(10);
        testAppDeploymentDesc1.setDefaultNodeCount(5);
        testAppDeploymentDesc1.setDefaultWalltime(15);
        testAppDeploymentDesc1.setEditableByUser(true);

        ApplicationDeploymentDescription testAppDeploymentDesc2 = new ApplicationDeploymentDescription();
        testAppDeploymentDesc2.setAppDeploymentId("appDep2");
        testAppDeploymentDesc2.setAppDeploymentDescription("test application deployment2");
        testAppDeploymentDesc2.setAppModuleId(applicationModule.getAppModuleId());
        testAppDeploymentDesc2.setComputeHostId(computeResourceId);
        testAppDeploymentDesc2.setExecutablePath("executablePath1");
        testAppDeploymentDesc2.setParallelism(ApplicationParallelismType.MPI);
        testAppDeploymentDesc2.setModuleLoadCmds(Arrays.asList(moduleLoadCmd));
        testAppDeploymentDesc2.setLibPrependPaths(Arrays.asList(libPrependPath));
        testAppDeploymentDesc2.setLibAppendPaths(Arrays.asList(libAppendPath));
        testAppDeploymentDesc2.setPreJobCommands(Arrays.asList(preJobCommand));
        testAppDeploymentDesc2.setPostJobCommands(Arrays.asList(postJobCommand));
        testAppDeploymentDesc2.setSetEnvironment(Arrays.asList(setEnvironment));
        testAppDeploymentDesc2.setDefaultQueueName("queue2");
        testAppDeploymentDesc2.setDefaultCPUCount(15);
        testAppDeploymentDesc2.setDefaultNodeCount(10);
        testAppDeploymentDesc2.setDefaultWalltime(5);
        testAppDeploymentDesc2.setEditableByUser(false);

        String testDeploymentId1 = applicationDeploymentRepository.addApplicationDeployment(testAppDeploymentDesc1, gatewayId);
        ApplicationDeploymentDescription retrievedApplicationDeployment = null;
        if(applicationDeploymentRepository.isExists(testDeploymentId1)) {
            retrievedApplicationDeployment = applicationDeploymentRepository.getApplicationDeployement(testDeploymentId1);
            assertTrue("Retrieved app deployment id matched", retrievedApplicationDeployment.getAppDeploymentId().equals("appDep1"));
            assertEquals("test application deployment1", retrievedApplicationDeployment.getAppDeploymentDescription());
            assertEquals(applicationModule.getAppModuleId(), retrievedApplicationDeployment.getAppModuleId());
            assertEquals(computeResourceDescription.getComputeResourceId(), retrievedApplicationDeployment.getComputeHostId());
            assertEquals("executablePath1", retrievedApplicationDeployment.getExecutablePath());
            assertTrue(retrievedApplicationDeployment.getParallelism().equals(ApplicationParallelismType.SERIAL));
        }

        String appDeploymentId = testAppDeploymentDesc1.getAppDeploymentId();
        testAppDeploymentDesc1.setDefaultQueueName("queue3");
        applicationDeploymentRepository.updateApplicationDeployment(appDeploymentId , testAppDeploymentDesc1);
        assertTrue(applicationDeploymentRepository.getApplicationDeployement(appDeploymentId).getDefaultQueueName().equals("queue3"));

        String testDeploymentId2 = applicationDeploymentRepository.addApplicationDeployment(testAppDeploymentDesc2, gatewayId);
        List<ApplicationDeploymentDescription> appDeploymentList = applicationDeploymentRepository.getAllApplicationDeployements(gatewayId);
        List<String> appDeploymentIds = applicationDeploymentRepository.getAllApplicationDeployementIds();
        assertTrue(appDeploymentList.size() == 2);
        assertTrue(appDeploymentIds.size() == 2);

        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID, applicationModule.getAppModuleId());
        filters.put(DBConstants.ApplicationDeployment.COMPUTE_HOST_ID, computeResourceDescription.getComputeResourceId());
        appDeploymentList = applicationDeploymentRepository.getApplicationDeployements(filters);
        assertEquals(computeResourceDescription.getComputeResourceId(), appDeploymentList.get(0).getComputeHostId());

        assertTrue(applicationDeploymentRepository.getAllApplicationDeployements(gatewayId).size() == 2);

        List<String> accessibleAppIds = new ArrayList<>();
        accessibleAppIds.add(testDeploymentId1);
        accessibleAppIds.add(testDeploymentId2);
        List<String> accessibleCompHostIds = new ArrayList<>();
        accessibleCompHostIds.add(computeResourceId);
        appDeploymentList = applicationDeploymentRepository.getAccessibleApplicationDeployements(gatewayId, accessibleAppIds, accessibleCompHostIds);
        assertTrue(appDeploymentList.size() == 2);
        assertEquals(testDeploymentId1, appDeploymentList.get(0).getAppDeploymentId());

        applicationDeploymentRepository.removeAppDeployment(testAppDeploymentDesc2.getAppDeploymentId());
        assertFalse(applicationDeploymentRepository.isExists(testAppDeploymentDesc2.getAppDeploymentId()));

        computeResourceRepository.removeComputeResource(computeResourceDescription.getComputeResourceId());

    }

}


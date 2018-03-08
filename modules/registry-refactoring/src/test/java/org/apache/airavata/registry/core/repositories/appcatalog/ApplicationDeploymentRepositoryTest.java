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
import org.apache.airavata.registry.core.repositories.util.Initialize;
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
        computeResourceRepository.addComputeResource(computeResourceDescription);

        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);

        ApplicationDeploymentDescription applicationDeploymentDescription = new ApplicationDeploymentDescription();
        ApplicationDeploymentDescription applicationDeploymentDescription1 = new ApplicationDeploymentDescription();

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

        applicationDeploymentDescription.setAppDeploymentId("appDep1");
        applicationDeploymentDescription.setAppDeploymentDescription("test application deployment1");
        applicationDeploymentDescription.setAppModuleId(applicationModule.getAppModuleId());
        applicationDeploymentDescription.setComputeHostId(computeResourceDescription.getComputeResourceId());
        applicationDeploymentDescription.setExecutablePath("executablePath1");
        applicationDeploymentDescription.setParallelism(ApplicationParallelismType.SERIAL);
        applicationDeploymentDescription.setModuleLoadCmds(Arrays.asList(moduleLoadCmd));
        applicationDeploymentDescription.setLibPrependPaths(Arrays.asList(libPrependPath));
        applicationDeploymentDescription.setLibAppendPaths(Arrays.asList(libAppendPath));
        applicationDeploymentDescription.setPreJobCommands(Arrays.asList(preJobCommand));
        applicationDeploymentDescription.setPostJobCommands(Arrays.asList(postJobCommand));
        applicationDeploymentDescription.setSetEnvironment(Arrays.asList(setEnvironment));
        applicationDeploymentDescription.setDefaultQueueName("queue1");
        applicationDeploymentDescription.setDefaultCPUCount(10);
        applicationDeploymentDescription.setDefaultNodeCount(5);
        applicationDeploymentDescription.setDefaultWalltime(15);
        applicationDeploymentDescription.setEditableByUser(true);

        applicationDeploymentDescription1.setAppDeploymentId("appDep2");
        applicationDeploymentDescription1.setAppDeploymentDescription("test application deployment2");
        applicationDeploymentDescription1.setAppModuleId(applicationModule.getAppModuleId());
        applicationDeploymentDescription1.setComputeHostId(computeResourceDescription.getComputeResourceId());
        applicationDeploymentDescription1.setExecutablePath("executablePath1");
        applicationDeploymentDescription1.setParallelism(ApplicationParallelismType.MPI);
        applicationDeploymentDescription1.setModuleLoadCmds(Arrays.asList(moduleLoadCmd));
        applicationDeploymentDescription1.setLibPrependPaths(Arrays.asList(libPrependPath));
        applicationDeploymentDescription1.setLibAppendPaths(Arrays.asList(libAppendPath));
        applicationDeploymentDescription1.setPreJobCommands(Arrays.asList(preJobCommand));
        applicationDeploymentDescription1.setPostJobCommands(Arrays.asList(postJobCommand));
        applicationDeploymentDescription1.setSetEnvironment(Arrays.asList(setEnvironment));
        applicationDeploymentDescription1.setDefaultQueueName("queue2");
        applicationDeploymentDescription1.setDefaultCPUCount(15);
        applicationDeploymentDescription1.setDefaultNodeCount(10);
        applicationDeploymentDescription1.setDefaultWalltime(5);
        applicationDeploymentDescription1.setEditableByUser(false);

        String deploymentId = applicationDeploymentRepository.addApplicationDeployment(applicationDeploymentDescription, gatewayId);
        ApplicationDeploymentDescription retrievedApplicationDeployment = null;
        if(applicationDeploymentRepository.isExists(deploymentId)) {
            retrievedApplicationDeployment = applicationDeploymentRepository.getApplicationDeployement(deploymentId);
            assertTrue("Retrieved app deployment id matched", retrievedApplicationDeployment.getAppDeploymentId().equals("appDep1"));
            assertEquals("test application deployment1", retrievedApplicationDeployment.getAppDeploymentDescription());
            assertEquals(applicationModule.getAppModuleId(), retrievedApplicationDeployment.getAppModuleId());
            assertEquals(computeResourceDescription.getComputeResourceId(), retrievedApplicationDeployment.getComputeHostId());
            assertEquals("executablePath1", retrievedApplicationDeployment.getExecutablePath());
            assertTrue(retrievedApplicationDeployment.getParallelism().equals(ApplicationParallelismType.SERIAL));
        }

        String appDeploymentId = applicationDeploymentDescription.getAppDeploymentId();
        applicationDeploymentDescription.setDefaultQueueName("queue3");
        applicationDeploymentRepository.updateApplicationDeployment(appDeploymentId , applicationDeploymentDescription);
        assertTrue(applicationDeploymentRepository.getApplicationDeployement(appDeploymentId).getDefaultQueueName().equals("queue3"));

        String deploymentId1 = applicationDeploymentRepository.addApplicationDeployment(applicationDeploymentDescription1, gatewayId);
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
        accessibleAppIds.add(deploymentId);
        accessibleAppIds.add(deploymentId1);
        appDeploymentList = applicationDeploymentRepository.getAccessibleApplicationDeployements(gatewayId, accessibleAppIds);
        assertTrue(appDeploymentList.size() == 2);
        assertEquals(deploymentId, appDeploymentList.get(0).getAppDeploymentId());

        applicationDeploymentRepository.removeAppDeployment(applicationDeploymentDescription1.getAppDeploymentId());
        assertFalse(applicationDeploymentRepository.isExists(applicationDeploymentDescription1.getAppDeploymentId()));

        computeResourceRepository.removeComputeResource(computeResourceDescription.getComputeResourceId());

    }

}


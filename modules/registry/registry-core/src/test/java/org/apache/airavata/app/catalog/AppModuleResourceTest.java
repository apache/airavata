/*
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

package org.apache.airavata.app.catalog;

import org.apache.airavata.app.catalog.util.Initialize;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatalogResource;
import org.apache.airavata.registry.core.app.catalog.resources.AppModuleResource;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ApplicationDeployment;
import org.apache.airavata.registry.cpi.ComputeResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AppModuleResourceTest {

    private static Initialize initialize;
    private static AppCatalog appcatalog;
    private static final Logger logger = LoggerFactory.getLogger(AppDeploymentTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            appcatalog = RegistryFactory.getAppCatalog();
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void testGetAllWithAccessibleDeploymentIds() throws Exception {

        ApplicationDeployment appDep = appcatalog.getApplicationDeployment();

        String gatewayId = "test-gateway";
        AppModuleResource appModuleResource = new AppModuleResource();
        appModuleResource.setGatewayId(gatewayId);
        appModuleResource.setModuleId("module1-id");
        appModuleResource.setModuleName("module1-name");
        appModuleResource.setModuleDesc("module1-desc");

        appModuleResource.save();

        appModuleResource = new AppModuleResource();
        appModuleResource.setGatewayId(gatewayId);
        appModuleResource.setModuleId("module2-id");
        appModuleResource.setModuleName("module2-name");
        appModuleResource.setModuleDesc("module2-desc");

        appModuleResource.save();

        // Verify we can get the first module
        appModuleResource = new AppModuleResource();
        appModuleResource = (AppModuleResource) appModuleResource.get("module1-id");
        Assert.assertEquals(appModuleResource.getGatewayId(), gatewayId);

        // Verify when when get all modules it returns two of them
        appModuleResource = new AppModuleResource();
        appModuleResource.setGatewayId(gatewayId);
        List<AppCatalogResource> appModuleResources = appModuleResource.getAll();
        Assert.assertEquals(2, appModuleResources.size());

        ComputeResource computeResource = appcatalog.getComputeResource();
        ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
        computeResourceDescription.setHostName("localhost");
        String computeResourceId = computeResource.addComputeResource(computeResourceDescription);

        ApplicationDeploymentDescription appDep1 = new ApplicationDeploymentDescription();
        appDep1.setAppModuleId("module2-id");
        appDep1.setComputeHostId(computeResourceId);
        String appDepId1 = appDep.addApplicationDeployment(appDep1, gatewayId);

        appModuleResource = new AppModuleResource();
        appModuleResource.setGatewayId(gatewayId);
        appModuleResource.setAccessibleApplicationDeploymentIds(Arrays.asList(appDepId1));
        appModuleResource.setAccessibleComputeResourceIds(Arrays.asList(computeResourceId));
        appModuleResources = appModuleResource.getAll();
        Assert.assertEquals(1, appModuleResources.size());
        AppModuleResource firstAppModuleResource = (AppModuleResource) appModuleResources.get(0);
        Assert.assertEquals("module2-id", firstAppModuleResource.getModuleId());

    }
}

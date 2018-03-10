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
package org.apache.airavata.app.catalog;

import org.apache.airavata.app.catalog.util.Initialize;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.CommandObject;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatAbstractResource;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class AppDeploymentTest {
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
    public void testAppDeployment () throws Exception {
        ApplicationDeployment appDep = appcatalog.getApplicationDeployment();
        ApplicationInterface appInt = appcatalog.getApplicationInterface();
        ComputeResource computeRs = appcatalog.getComputeResource();
        ComputeResourceDescription cm = new ComputeResourceDescription();
        cm.setHostName("localhost");
        cm.setResourceDescription("test compute host");
        String hostId = computeRs.addComputeResource(cm);

        ApplicationModule module = new ApplicationModule();
        module.setAppModuleName("WRF");
        module.setAppModuleVersion("1.0.0");
        String wrfModuleId = appInt.addApplicationModule(module, ServerSettings.getDefaultUserGateway());

        ApplicationDeploymentDescription description = new ApplicationDeploymentDescription();
        description.setAppModuleId(wrfModuleId);
        description.setComputeHostId(hostId);
        description.setExecutablePath("/home/a/b/c");
        description.setAppDeploymentDescription("test app deployment");
        CommandObject cmd1 = new CommandObject();
        cmd1.setCommand("cmd1");
        cmd1.setCommandOrder(1);
        CommandObject cmd2 = new CommandObject();
        cmd2.setCommand("cmd1");
        cmd2.setCommandOrder(1);
        description.addToModuleLoadCmds(cmd1);
        description.addToModuleLoadCmds(cmd2);

        List<SetEnvPaths> libPrepandPaths = new ArrayList<SetEnvPaths>();
        libPrepandPaths.add(createSetEnvPath("name1", "val1", 1));
        libPrepandPaths.add(createSetEnvPath("name2", "val2", 2));
        description.setLibPrependPaths(libPrepandPaths);
        List<SetEnvPaths> libApendPaths = new ArrayList<SetEnvPaths>();
        libApendPaths.add(createSetEnvPath("name3", "val3", 1));
        libApendPaths.add(createSetEnvPath("name4", "val4", 2));
        description.setLibAppendPaths(libApendPaths);
        List<SetEnvPaths> appEvns = new ArrayList<SetEnvPaths>();
        appEvns.add(createSetEnvPath("name5", "val5", 1));
        appEvns.add(createSetEnvPath("name6", "val6", 2));
        description.setSetEnvironment(appEvns);

        String appDepId = appDep.addApplicationDeployment(description, ServerSettings.getDefaultUserGateway());
        ApplicationDeploymentDescription app = null;
        if (appDep.isAppDeploymentExists(appDepId)){
            app = appDep.getApplicationDeployement(appDepId);
            System.out.println("*********** application deployment id ********* : " + app.getAppDeploymentId());
            System.out.println("*********** application deployment desc ********* : " + app.getAppDeploymentDescription());
        }

        description.setAppDeploymentDescription("test app deployment2");
        appDep.updateApplicationDeployment(appDepId, description);

        if (appDep.isAppDeploymentExists(appDepId)){
            app = appDep.getApplicationDeployement(appDepId);
            System.out.println("*********** application deployment desc ********* : " + app.getAppDeploymentDescription());
        }

        Map<String, String> moduleIdFilter = new HashMap<String, String>();
        moduleIdFilter.put(AppCatAbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, wrfModuleId);
        List<ApplicationDeploymentDescription> applicationDeployements = appDep.getApplicationDeployements(moduleIdFilter);
        System.out.println("******** Size of App deployments for module *********** : " + applicationDeployements.size());
        Map<String, String> hostFilter = new HashMap<String, String>();
        hostFilter.put(AppCatAbstractResource.ApplicationDeploymentConstants.COMPUTE_HOST_ID, hostId);
        List<ApplicationDeploymentDescription> applicationDeployementsForHost = appDep.getApplicationDeployements(hostFilter);
        System.out.println("******** Size of App deployments for host *********** : " + applicationDeployementsForHost.size());

        List<String> allApplicationDeployementIds = appDep.getAllApplicationDeployementIds();
        System.out.println("******** Size of all App deployments ids *********** : " + allApplicationDeployementIds.size());

        List<String> accessibleAppIds = new ArrayList<>();
        accessibleAppIds.add(wrfModuleId);
        List<String> accessibleComputeResourceIds = new ArrayList<>();
        accessibleComputeResourceIds.add(hostId);
        List<ApplicationDeploymentDescription> allApplicationDeployements = appDep.getAccessibleApplicationDeployements(ServerSettings.getDefaultUserGateway(), accessibleAppIds, accessibleComputeResourceIds);
        System.out.println("******** Size of all App deployments *********** : " + allApplicationDeployements.size());

        assertTrue("App interface saved successfully", app != null);
    }

    public SetEnvPaths createSetEnvPath (String name, String val, int order){
        SetEnvPaths setEnvPaths = new SetEnvPaths();
        setEnvPaths.setName(name);
        setEnvPaths.setValue(val);
        setEnvPaths.setEnvPathOrder(order);
        return setEnvPaths;

    }

}

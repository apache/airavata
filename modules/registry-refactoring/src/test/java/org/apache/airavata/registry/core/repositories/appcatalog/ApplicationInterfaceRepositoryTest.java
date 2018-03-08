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

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
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

public class ApplicationInterfaceRepositoryTest {

    private static Initialize initialize;
    private ApplicationInterfaceRepository applicationInterfaceRepository;
    private String gatewayId = "testGateway";
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInterfaceRepository.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            applicationInterfaceRepository = new ApplicationInterfaceRepository();
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
    public void ApplicationInterfaceRepositoryTest() throws AppCatalogException {
        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();
        applicationInterfaceDescription.setApplicationInterfaceId("interface1");
        applicationInterfaceDescription.setApplicationName("app interface 1");

        String interfaceId = applicationInterfaceRepository.addApplicationInterface(applicationInterfaceDescription, gatewayId);
        assertEquals(applicationInterfaceDescription.getApplicationInterfaceId(), interfaceId);

        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setAppModuleId("appMod1");
        applicationModule.setAppModuleName("appMod1Name");
        String moduleId = applicationInterfaceRepository.addApplicationModule(applicationModule, gatewayId);

        applicationInterfaceRepository.addApplicationModuleMapping(moduleId, interfaceId);

        InputDataObjectType input = new InputDataObjectType();
        input.setName("input1");

        OutputDataObjectType output = new OutputDataObjectType();
        output.setName("output1");

        applicationInterfaceDescription.setApplicationInputs(Arrays.asList(input));
        applicationInterfaceDescription.setApplicationOutputs(Arrays.asList(output));
        applicationInterfaceRepository.updateApplicationInterface(interfaceId, applicationInterfaceDescription);
        ApplicationInterfaceDescription appDescription = applicationInterfaceRepository.getApplicationInterface(interfaceId);
        assertTrue(appDescription.getApplicationInputs().size() == 1);
        assertEquals(output.getName(), appDescription.getApplicationOutputs().get(0).getName());

        applicationModule.setAppModuleVersion("1.0");
        applicationInterfaceRepository.updateApplicationModule(moduleId, applicationModule);
        ApplicationModule appModule = applicationInterfaceRepository.getApplicationModule(moduleId);
        assertFalse(appModule.getAppModuleVersion() == null);

        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.ApplicationInterface.APPLICATION_NAME, applicationInterfaceDescription.getApplicationName());
        assertEquals(applicationInterfaceDescription.getApplicationName(),
                applicationInterfaceRepository.getApplicationInterfaces(filters).get(0).getApplicationName());

        filters = new HashMap<>();
        filters.put(DBConstants.ApplicationModule.APPLICATION_MODULE_NAME, applicationModule.getAppModuleName());
        assertEquals(applicationModule.getAppModuleName(),
                applicationInterfaceRepository.getApplicationModules(filters).get(0).getAppModuleName());

        assertTrue(applicationInterfaceRepository.getAccessibleApplicationModules(gatewayId, Arrays.asList(moduleId)).size() == 1);

        assertTrue(applicationInterfaceRepository.getAllApplicationInterfaces(gatewayId).size() == 1);
        assertTrue(applicationInterfaceRepository.getAllApplicationModules(gatewayId).size() == 1);
        assertEquals(interfaceId, applicationInterfaceRepository.getAllApplicationInterfaceIds().get(0));

        assertEquals(input.getName(), applicationInterfaceRepository.getApplicationInputs(interfaceId).get(0).getName());
        assertEquals(output.getName(), applicationInterfaceRepository.getApplicationOutputs(interfaceId).get(0).getName());

        applicationInterfaceRepository.removeApplicationInterface(interfaceId);
        assertFalse(applicationInterfaceRepository.isApplicationInterfaceExists(interfaceId));

        applicationInterfaceRepository.removeApplicationModule(moduleId);
        assertFalse(applicationInterfaceRepository.isApplicationModuleExists(moduleId));

    }

}

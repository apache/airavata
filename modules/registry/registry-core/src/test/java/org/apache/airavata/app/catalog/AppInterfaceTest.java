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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatAbstractResource;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ApplicationInterface;
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

public class AppInterfaceTest {
    private static Initialize initialize;
    private static AppCatalog appcatalog;
    private static int order = 1;
    private static final Logger logger = LoggerFactory.getLogger(AppInterfaceTest.class);

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
    public void testAppInterface(){
        try {
            ApplicationInterface appInterface = appcatalog.getApplicationInterface();
            ApplicationInterfaceDescription description = new ApplicationInterfaceDescription();
            String wrfModuleId = addAppModule("WRF");
            String amberModuleId = addAppModule("AMBER");
            List<String> modules = new ArrayList<String>();
            modules.add(wrfModuleId);
            modules.add(amberModuleId);
            InputDataObjectType appInput1 = createAppInput("input1", "input1", DataType.STRING);
            InputDataObjectType appInput2 = createAppInput("input2", "input2", DataType.INTEGER);
            List<InputDataObjectType> inputs = new ArrayList<InputDataObjectType>();
            inputs.add(appInput1);
            inputs.add(appInput2);
            OutputDataObjectType output1 = createAppOutput("output1", "", DataType.STRING);
            OutputDataObjectType output2 = createAppOutput("output2", "", DataType.STRING);
            List<OutputDataObjectType> outputs = new ArrayList<OutputDataObjectType>();
            outputs.add(output1);
            outputs.add(output2);
            description.setApplicationName("testApplication");
            description.setApplicationDescription("my testApplication");
            description.setApplicationModules(modules);
            description.setApplicationInputs(inputs);
            description.setApplicationOutputs(outputs);
            String appID = appInterface.addApplicationInterface(description, ServerSettings.getDefaultUserGateway());
            System.out.println("********** application id ************* : " + appID);
            ApplicationInterfaceDescription ainterface = null;
            if (appInterface.isApplicationInterfaceExists(appID)){
                ainterface = appInterface.getApplicationInterface(appID);
                OutputDataObjectType output3 = createAppOutput("output3", "", DataType.STRING);
                OutputDataObjectType output4 = createAppOutput("output4", "", DataType.STRING);
                outputs.add(output3);
                outputs.add(output4);
                ainterface.setApplicationOutputs(outputs);
                appInterface.updateApplicationInterface(appID, ainterface);
                ApplicationInterfaceDescription updateApp = appInterface.getApplicationInterface(appID);
                List<OutputDataObjectType> appOutputs = updateApp.getApplicationOutputs();
                System.out.println("********** application name ************* : " + updateApp.getApplicationName());
                System.out.println("********** application description ************* : " + updateApp.getApplicationDescription());
                System.out.println("********** output size ************* : " + appOutputs.size());
            }
            ApplicationModule wrfModule = appInterface.getApplicationModule(wrfModuleId);
            System.out.println("********** WRF module name ************* : " + wrfModule.getAppModuleName());
            ApplicationModule amberModule = appInterface.getApplicationModule(amberModuleId);
            System.out.println("********** Amber module name ************* : " + amberModule.getAppModuleName());

            List<InputDataObjectType> applicationInputs = appInterface.getApplicationInputs(appID);
            System.out.println("********** App Input size ************* : " + applicationInputs.size());

            List<OutputDataObjectType> applicationOutputs = appInterface.getApplicationOutputs(appID);
            System.out.println("********** App output size ************* : " + applicationOutputs.size());

            description.setApplicationName("testApplication2");
            appInterface.updateApplicationInterface(appID, description);
            if (appInterface.isApplicationInterfaceExists(appID)){
                ainterface = appInterface.getApplicationInterface(appID);
                System.out.println("********** updated application name ************* : " + ainterface.getApplicationName());
            }

            wrfModule.setAppModuleVersion("1.0.1");
            appInterface.updateApplicationModule(wrfModuleId, wrfModule);
            wrfModule = appInterface.getApplicationModule(wrfModuleId);
            System.out.println("********** Updated WRF module version ************* : " + wrfModule.getAppModuleVersion());

            Map<String, String> filters = new HashMap<String, String>();
            filters.put(AppCatAbstractResource.ApplicationInterfaceConstants.APPLICATION_NAME, "testApplication2");
            List<ApplicationInterfaceDescription> apps = appInterface.getApplicationInterfaces(filters);
            System.out.println("********** Size og app interfaces ************* : " + apps.size());

            List<ApplicationInterfaceDescription> appInts = appInterface.getAllApplicationInterfaces(ServerSettings.getDefaultUserGateway());
            System.out.println("********** Size of all app interfaces ************* : " + appInts.size());

            List<String> appIntIds = appInterface.getAllApplicationInterfaceIds();
            System.out.println("********** Size of all app interface ids ************* : " + appIntIds.size());

            assertTrue("App interface saved successfully", ainterface != null);
        }catch (AppCatalogException e) {
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }

    }

    public String addAppModule (String moduleName){
        try {
            ApplicationModule module = new ApplicationModule();
            module.setAppModuleName(moduleName);
            module.setAppModuleVersion("1.0.0");
            module.setAppModuleDescription("WeatherForcast");
            return appcatalog.getApplicationInterface().addApplicationModule(module, ServerSettings.getDefaultUserGateway());
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputDataObjectType createAppInput (String inputName, String value, DataType type ){
        InputDataObjectType input = new InputDataObjectType();
        input.setName(inputName);
        input.setValue(value);
        input.setType(type);
        input.setApplicationArgument("test arg");
        input.setInputOrder(order++);
        return input;
    }

    public OutputDataObjectType createAppOutput (String inputName, String value, DataType type ){
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
        outputDataObjectType.setName(inputName);
        outputDataObjectType.setValue(value);
        outputDataObjectType.setType(type);
        return outputDataObjectType;
    }
}
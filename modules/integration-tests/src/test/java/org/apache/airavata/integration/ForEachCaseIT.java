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
package org.apache.airavata.integration;

import org.testng.annotations.BeforeTest;

import java.io.IOException;

/**
 * Integration test class.
 */
public class ForEachCaseIT extends WorkflowIntegrationTestBase {

    public ForEachCaseIT() throws Exception {
//        setUpEnvironment();
    }

    @BeforeTest
    public void setUp() throws Exception {
//        setupDescriptors();
    }

//    @Test(groups = {"forEachGroup"})
//    public void testForEachUsecases() throws Exception {
//        executeExperiment("src/test/resources/ForEachBasicWorkflow.xwf", Arrays.asList("10", "20"), Arrays.asList("10 20"));
//        executeExperiment("src/test/resources/ForEachBasicWorkflow.xwf", Arrays.asList("10", "20,30"), Arrays.asList("10 20", "10 30"));
//        executeExperiment("src/test/resources/ForEachBasicWorkflow.xwf", Arrays.asList("10,20", "30,40"), Arrays.asList("10 30", "20 40"));
//
//        executeExperiment("src/test/resources/ForEachEchoWorkflow.xwf", Arrays.asList("10", "20"), Arrays.asList("10,20"));
//        executeExperiment("src/test/resources/ForEachEchoWorkflow.xwf", Arrays.asList("10", "20,30"), Arrays.asList("10,20", "10,30"));
//        executeExperiment("src/test/resources/ForEachEchoWorkflow.xwf", Arrays.asList("10,20", "30,40"), Arrays.asList("10,30", "20,40"));
//    }

//    private void setupDescriptors() throws AiravataAPIInvocationException,
//            DescriptorAlreadyExistsException, IOException {
//        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();
//        HostDescription hostDescription = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost2",
//                "127.0.0.1");
//
//        log("Adding host description ....");
//        addHostDescriptor(hostDescription);
//        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(hostDescription.getType().getHostName()));
//
//        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
//        inputParameters.add(descriptorBuilder.buildInputParameterType("data1", "data1", DataType.STRING));
//        inputParameters.add(descriptorBuilder.buildInputParameterType("data2", "data2", DataType.STRING));
//
//        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
//        outputParameters.add(descriptorBuilder.buildOutputParameterType("out", "out", DataType.STD_OUT));
//
//        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("comma_app", "comma_app",
//                inputParameters, outputParameters);
//
//        ServiceDescription serviceDescription2 = descriptorBuilder.buildServiceDescription("echo_app", "echo_app",
//                inputParameters, outputParameters);
//
//        log("Adding service description ...");
//        addServiceDescriptor(serviceDescription, "comma_app");
//        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(
//                serviceDescription.getType().getName()));
//
//        addServiceDescriptor(serviceDescription2, "echo_app");
//        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(
//                serviceDescription2.getType().getName()));
//
//        // Deployment descriptor
//        File executable;
//        if (OsUtils.isWindows()) {
//            executable = getFile("src/test/resources/comma_data.bat");
//        } else {
//            executable = getFile("src/test/resources/comma_data.sh");
//            Runtime.getRuntime().exec("chmod +x " + executable.getAbsolutePath());
//        }
//
//        ApplicationDescription applicationDeploymentDescription = descriptorBuilder
//                .buildApplicationDeploymentDescription("comma_app_localhost", executable.getAbsolutePath(), OsUtils.getTempFolderPath());
//        ApplicationDescription applicationDeploymentDescription2 = descriptorBuilder
//                .buildApplicationDeploymentDescription("echo_app_localhost", OsUtils.getEchoExecutable(), OsUtils.getTempFolderPath());
//
//        log("Adding deployment description ...");
//        addApplicationDescriptor(applicationDeploymentDescription, serviceDescription, hostDescription, "comma_app_localhost");
//
//        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(
//                serviceDescription.getType().getName(), hostDescription.getType().getHostName(),
//                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));
//
//        addApplicationDescriptor(applicationDeploymentDescription2, serviceDescription2, hostDescription, "echo_app_localhost");
//        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(
//                serviceDescription2.getType().getName(), hostDescription.getType().getHostName(),
//                applicationDeploymentDescription2.getType().getApplicationName().getStringValue()));
//    }
}

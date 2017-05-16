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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//
//package org.apache.airavata.testsuite.multitenantedairavata;
//
//import org.apache.airavata.common.exception.AiravataException;
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.airavata.common.utils.ApplicationSettings;
//import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
//import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
//import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
//import org.apache.airavata.model.appcatalog.computeresource.*;
//import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
//import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
//import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
//import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
//import org.apache.airavata.model.application.io.DataType;
//import org.apache.airavata.model.application.io.InputDataObjectType;
//import org.apache.airavata.model.application.io.OutputDataObjectType;
//import org.apache.airavata.model.data.movement.DataMovementProtocol;
//import org.apache.airavata.model.data.movement.SecurityProtocol;
//import org.apache.airavata.model.experiment.ExperimentModel;
//import org.apache.airavata.model.experiment.UserConfigurationDataModel;
//import org.apache.airavata.model.parallelism.ApplicationParallelismType;
//import org.apache.airavata.model.status.ExperimentState;
//import org.apache.airavata.model.status.ExperimentStatus;
//import org.apache.airavata.model.workspace.Gateway;
//import org.apache.airavata.model.workspace.GatewayApprovalStatus;
//import org.apache.airavata.registry.core.experiment.catalog.model.UserConfigurationData;
//import org.apache.airavata.testsuite.multitenantedairavata.utils.ApplicationProperties;
//import org.apache.airavata.testsuite.multitenantedairavata.utils.ComputeResourceProperties;
//import org.apache.airavata.testsuite.multitenantedairavata.utils.Initialize;
//import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
//import org.apache.commons.cli.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//import java.lang.reflect.Field;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//
//import org.apache.airavata.server.ServerMain;
//import java.lang.Thread;
//import org.apache.airavata.gfac.server.GfacServer;
//import org.apache.airavata.orchestrator.server.OrchestratorServer;
//import org.testng.Assert;
//import org.testng.annotations.*;
//import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LocalEchoComputeResource.*;
//import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.*;
//import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LocalApplication.*;
//
//public class Test {
//    private final static Logger logger = LoggerFactory.getLogger(Test.class);
//    private static TestFrameworkProps properties;
//    private static Map<String, String> tokens;
//    private static ExperimentExecution experimentExecution;
//    private FrameworkSetup setup;
//    private Object lock = new Object();
//    private String storageResource;
//    private ComputeResourceProperties computeResourceProperties;
//    private ApplicationProperties applicationProperties;
//    private String experimentId;
////    @BeforeMethod
////    public void airavataSetup(){
////        try {
////                Map<String, String> env = System.getenv();
////                Class[] classes = Collections.class.getDeclaredClasses();
////                for(Class cl : classes) {
////                    if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
////                        Field field = cl.getDeclaredField("m");
////                        field.setAccessible(true);
////                        Object obj = field.get(env);
////                        Map<String, String> map = (Map<String, String>) obj;
////                        Map<String, String> home = new HashMap<>();
////                        home.put("AIRAVATA_HOME", System.getProperty("user.dir") + "/local-exp-resources/");
////                        map.clear();
////                        map.putAll(home);
////                    }
////
////            }
////
////        }catch (Exception e){}
////
////        System.out.print("Starting airavata..........");
////
////        String options[] = {"all"};
//////        try {
//////            ApplicationSettings.getProperties();
//////        } catch (ApplicationSettingsException e) {
//////            e.printStackTrace();
//////        }
////
//////        initialize = new Initialize("appcatalog-derby.sql");
//////
//////        initialize.initializeDB();
////        airavataThread = new Thread(new Runnable() {
////
////            @Override
////            public void run() {
////                try {
////                    ServerMain.test(options, lock);
////
////                } catch (ParseException e) {
////                    e.printStackTrace();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                } catch (AiravataException e) {
////                    e.printStackTrace();
////                }
////            }
////        });
////        airavataThread.start();
////
////        try {
////            synchronized (lock){
////                lock.wait();
////            }
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
////        //return true;
////    }
//
//    @BeforeTest
//    public void setupAiravata(){
//        Process p;
//        try {
//            String result = null;
//            p = Runtime.getRuntime().exec("./startup-airavata.sh");
//            BufferedReader br = new BufferedReader(
//                    new InputStreamReader(p.getInputStream()));
//            while ((result = br.readLine()) != null)
//                System.out.println(result);
//            System.out.println ("exit: " + p.exitValue());
//            Thread.sleep(15000);
//            setup = FrameworkSetup.getInstance();
//            properties = setup.getTestFrameworkProps();
//        } catch (Exception e) {
//            logger.error("Error occured while set up", e);
//            Assert.fail();
//        }
//    }
//
//
//    @org.testng.annotations.Test(priority=1)
//    public void testCreateGateways(){
//        try {
//            setup.getGatewayRegister().createGateway();
//            GatewayResourceProfile gatewayResourceProfile = setup.getGatewayRegister().getGatewayResourceProfile();
//            Assert.assertNotNull(gatewayResourceProfile);
//            Gateway testGateway = setup.getGatewayRegister().getGateway(properties.getGname());
//            Assert.assertNotNull(testGateway);
//            Assert.assertEquals(testGateway.getGatewayName(), properties.getGname());
//            Assert.assertEquals(testGateway.getDomain(), properties.getGname() + properties.getGdomain());
//            Assert.assertEquals(testGateway.getGatewayApprovalStatus(), GatewayApprovalStatus.APPROVED);
//            setup.getGatewayRegister().registerSSHKeys();
//        } catch (Exception e) {
//            logger.error("Error occured while testCreateGateways", e);
//            Assert.fail();
//        }
//    }
//
//    @org.testng.annotations.Test(priority=2)
//    public void testComputeResource(){
//        try {
//
//            computeResourceProperties = setup.getComputeResourceRegister().addComputeResources();
//            Assert.assertNotNull(computeResourceProperties);
//
//            ComputeResourceDescription computeResourceDescription = setup.getComputeResourceRegister().getComputeResource(computeResourceProperties.getComputeResourceId());
//            Assert.assertNotNull(computeResourceDescription.getHostName(), HOST_NAME);
//            Assert.assertNotNull(computeResourceDescription.getResourceDescription(), HOST_DESC);
//
//            LOCALSubmission localSubmission = setup.getComputeResourceRegister().getLocalSubmission(computeResourceProperties.getJobSubmissionId());
//
//            Assert.assertEquals(localSubmission.getResourceJobManager().getResourceJobManagerType(), ResourceJobManagerType.FORK);
//            Assert.assertEquals(localSubmission.getSecurityProtocol(), SecurityProtocol.LOCAL);
//            Assert.assertEquals(localSubmission.getResourceJobManager().getPushMonitoringEndpoint(), null);
//            Assert.assertEquals(localSubmission.getResourceJobManager().getJobManagerBinPath(), "");
//            Assert.assertEquals(localSubmission.getResourceJobManager().getJobManagerCommands().get(JobManagerCommand.SUBMISSION), JOB_MANAGER_COMMAND);
//
//            setup.getComputeResourceRegister().registerGatewayResourceProfile(computeResourceProperties.getComputeResourceId());
//
//            ComputeResourcePreference computeResourcePreference = setup.getComputeResourceRegister().getGatewayComputeResourcePreference(properties.getGname(), computeResourceProperties.getComputeResourceId());
//
//            Assert.assertEquals(computeResourcePreference.getAllocationProjectNumber(), ALLOCATION_PROJECT_NUMBER);
//            Assert.assertEquals(computeResourcePreference.getPreferredBatchQueue(), BATCH_QUEUE);
//            Assert.assertEquals(computeResourcePreference.getPreferredDataMovementProtocol(), DataMovementProtocol.LOCAL);
//            Assert.assertEquals(computeResourcePreference.getPreferredJobSubmissionProtocol(), JobSubmissionProtocol.LOCAL);
//            Assert.assertEquals(computeResourcePreference.getScratchLocation(), TestFrameworkConstants.SCRATCH_LOCATION);
//            Assert.assertEquals(computeResourcePreference.getLoginUserName(), LOGIN_USER);
//
//
//        } catch (Exception e) {
//            logger.error("Error occured while testComputeResource", e);
//            Assert.fail();
//        }
//    }
//
//
//    @org.testng.annotations.Test(priority=3)
//    public void testStorageResource(){
//        try {
//
//            storageResource = setup.getStorageResourceRegister().addStorageResourceResource();;
//            Assert.assertNotNull(storageResource);
//
//            StorageResourceDescription storageResourceDescription = setup.getStorageResourceRegister().getStorageResourceDescription(storageResource);
//            Assert.assertNotNull(storageResourceDescription.getHostName(), HOST_NAME);
//            Assert.assertNotNull(storageResourceDescription.getStorageResourceDescription(), HOST_DESC);
//
//            setup.getStorageResourceRegister().registerGatewayStorageProfile(storageResource);
//
//            StoragePreference storagePreference = setup.getStorageResourceRegister().getStoragePreference(properties.getGname(), storageResource);
//
//            Assert.assertEquals(storagePreference.getLoginUserName(), LOGIN_USER);
//            Assert.assertEquals(storagePreference.getFileSystemRootLocation(), TestFrameworkConstants.STORAGE_LOCATION);
//
//        } catch (Exception e) {
//            logger.error("Error occured while testStorageResource", e);
//            Assert.fail();
//        }
//    }
//
//
//
//    @org.testng.annotations.Test(priority=4)
//    public void testAddApplication(){
//        try {
//
//            applicationProperties = setup.getApplicationRegister().addApplications();;
//            Assert.assertNotNull(applicationProperties);
//
//            ApplicationModule applicationModule = setup.getApplicationRegister().getApplicationModule(applicationProperties.getApplicationModuleId());
//            Assert.assertNotNull(applicationModule);
//            Assert.assertEquals(applicationModule.getAppModuleName(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME);
//            Assert.assertEquals(applicationModule.getAppModuleVersion(), "1.0");
//            Assert.assertEquals(applicationModule.getAppModuleDescription(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION);
//            Assert.assertEquals(applicationModule.getAppModuleName(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME);
//
//
//            ApplicationInterfaceDescription applicationInterfaceDescription = setup.getApplicationRegister().getApplicationInterfaceDescription(applicationProperties.getApplicationInterfaceId());
//            Assert.assertNotNull(applicationInterfaceDescription);
//            Assert.assertEquals(applicationInterfaceDescription.getApplicationName(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME);
//            Assert.assertEquals(applicationInterfaceDescription.getApplicationDescription(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION);
//
//            InputDataObjectType input = applicationInterfaceDescription.getApplicationInputs().get(0);
//            Assert.assertNotNull(input);
//            Assert.assertEquals(input.getName(), INPUT_NAME);
//            Assert.assertEquals(input.getValue(), INPUT_VALUE);
//            Assert.assertEquals(input.getType(), DataType.STRING);
//            Assert.assertEquals(input.getApplicationArgument(), null);
//            Assert.assertEquals(input.getInputOrder(), 0);
//            Assert.assertEquals(input.isIsRequired(), true);
//            Assert.assertEquals(input.isRequiredToAddedToCommandLine(), true);
//            Assert.assertEquals(input.isStandardInput(), false);
//            Assert.assertEquals(input.getUserFriendlyDescription(), INPUT_DESC);
//            Assert.assertEquals(input.getMetaData(), null);
//
//            List<OutputDataObjectType> outputDataObjectTypes = applicationInterfaceDescription.getApplicationOutputs();
//            Assert.assertNotNull(outputDataObjectTypes.get(0));
//            Assert.assertEquals(outputDataObjectTypes.get(0).getName(), STDERR_NAME);
//            Assert.assertEquals(outputDataObjectTypes.get(0).getValue(), STDERR_VALUE);
//            Assert.assertEquals(outputDataObjectTypes.get(0).getType(), DataType.URI);
//            Assert.assertEquals(outputDataObjectTypes.get(0).isIsRequired(), true);
//            Assert.assertEquals(outputDataObjectTypes.get(0).isRequiredToAddedToCommandLine(), true);
//            Assert.assertEquals(outputDataObjectTypes.get(0).getApplicationArgument(), null);
//
//
//            Assert.assertNotNull(outputDataObjectTypes.get(1));
//            Assert.assertEquals(outputDataObjectTypes.get(1).getName(), STDOUT_NAME);
//            Assert.assertEquals(outputDataObjectTypes.get(1).getValue(), STDOUT_VALUE);
//            Assert.assertEquals(outputDataObjectTypes.get(1).getType(), DataType.URI);
//            Assert.assertEquals(outputDataObjectTypes.get(1).isIsRequired(), true);
//            Assert.assertEquals(outputDataObjectTypes.get(1).isRequiredToAddedToCommandLine(), true);
//            Assert.assertEquals(outputDataObjectTypes.get(1).getApplicationArgument(), null);
//
//
//
//            ApplicationDeploymentDescription applicationDeploymentDescription = setup.getApplicationRegister().getApplicationDeploymentDescription(applicationProperties.getApplicationDeployId());
//            Assert.assertNotNull(applicationDeploymentDescription);
//
//            Assert.assertEquals(applicationDeploymentDescription.getExecutablePath(), TestFrameworkConstants.LOCAL_ECHO_JOB_FILE_PATH);
//            Assert.assertEquals(applicationDeploymentDescription.getParallelism(), ApplicationParallelismType.SERIAL);
//            Assert.assertEquals(applicationDeploymentDescription.getAppDeploymentDescription(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION);
//            Assert.assertEquals(applicationDeploymentDescription.getModuleLoadCmds(), null);
//            Assert.assertEquals(applicationDeploymentDescription.getPostJobCommands(), null);
//            Assert.assertEquals(applicationDeploymentDescription.getPreJobCommands(), null);
//
//        } catch (Exception e) {
//            logger.error("Error occured while testAddApplication", e);
//            Assert.fail();
//        }
//    }
//
//    @org.testng.annotations.Test(priority=5)
//    public void testCreateApplication(){
//        try {
//            tokens = readTokens();
//            experimentExecution = new ExperimentExecution(setup.getAiravata(), tokens, properties);
//            experimentId = experimentExecution.createLocalEchoExperiment(properties.getGname(), applicationProperties.getApplicationInterfaceId(), storageResource, computeResourceProperties.getComputeResourceId());
//            Assert.assertNotNull(experimentId);
//
//            ExperimentModel simpleExperiment = experimentExecution.getExperimentModel(experimentId);
//            Assert.assertNotNull(simpleExperiment);
//            UserConfigurationDataModel userConfigurationData= simpleExperiment.getUserConfigurationData();
//            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getResourceHostId(), computeResourceProperties.getComputeResourceId());
//            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getTotalCPUCount(), 4);
//            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getNodeCount(), 1);
//            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getNumberOfThreads(), 1);
//            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getQueueName(), "cpu");
//            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getWallTimeLimit(), 20);
//            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getTotalPhysicalMemory(), 0);
//
//            Assert.assertEquals(userConfigurationData.isAiravataAutoSchedule(), false);
//            Assert.assertEquals(userConfigurationData.isOverrideManualScheduledParams(), false);
//            Assert.assertEquals(userConfigurationData.getStorageId(), storageResource);
//            Assert.assertEquals(userConfigurationData.getExperimentDataDir(), TestFrameworkConstants.STORAGE_LOCATION);
//
//
//        } catch (Exception e) {
//            logger.error("Error occured while testCreateApplication", e);
//            Assert.fail();
//        }
//    }
//
//    @org.testng.annotations.Test(priority=6)
//    public void testLaunchExperiment(){
//        try {
//            experimentExecution.launchExperiments(properties.getGname(), experimentId);
//            experimentExecution.monitorExperiments(lock);
//            ExperimentStatus experimentStatus = experimentExecution.getExperimentStatus(experimentId);
//            while(!experimentStatus.getState().equals(ExperimentState.COMPLETED) && !experimentStatus.getState().equals(ExperimentState.FAILED)){
//                experimentStatus = experimentExecution.getExperimentStatus(experimentId);
//                System.out.print(experimentStatus);
//            }
//            if(experimentStatus.getState().equals(ExperimentState.COMPLETED)){
//                Assert.assertEquals(new String(Files.readAllBytes(Paths.get(TestFrameworkConstants.STORAGE_LOCATION +"stdout.txt"))), LOCAL_ECHO_EXPERIMENT_INPUT);
//
//            }
//        } catch (Exception e) {
//            logger.error("Error occured while testLaunchExperiment", e);
//            Assert.fail();
//        }
//    }
//
////    @org.testng.annotations.Test
////    public void executeExperiment(){
////        System.out.print("Executing local test suit..........");
////        String options[] = {"all"};
////        try {
//////            FrameworkSetup setup = FrameworkSetup.getInstance();
//////            properties = setup.getTestFrameworkProps();
////
////            setup.getGatewayRegister().createGateway();
////            logger.info("Gateways created...");
////            setup.getGatewayRegister().registerSSHKeys();
////            logger.info("Registered SSH keys to each gateway...");
////            tokens = readTokens();
////            setup.getComputeResourceRegister().addComputeResources();
////            setup.getComputeResourceRegister().registerGatewayResourceProfile();
////
////            setup.getStorageResourceRegister().addStorageResourceResource();
////            setup.getStorageResourceRegister().registerGatewayStorageProfile();
////
////            setup.getApplicationRegister().addApplications();
////            logger.info("Applications registered for each each gateway...");
////            experimentExecution = new ExperimentExecution(setup.getAiravata(), tokens, properties);
////            experimentExecution.createLocalEchoExperiment();
////            experimentExecution.launchExperiments();
////            experimentExecution.monitorExperiments(lock);
////            synchronized (lock){
////                lock.wait();
////            }
////            //cleanUp();
////
////        } catch (Exception e) {
////            logger.error("Error occured while set up", e);
////        }
////    }
//
//    @AfterTest
//    private void cleanUp(){
//        System.out.print("Stopping airavata..........");
//        String options[] = {"stop"};
//        Process p;
//        try {
//
//            String result = null;
//            p = Runtime.getRuntime().exec("base-airavata/apache-airavata-server-0.17-SNAPSHOT/bin/airavata-server-stop.sh -f");
//            BufferedReader br = new BufferedReader(
//                    new InputStreamReader(p.getInputStream()));
//            while ((result = br.readLine()) != null)
//                System.out.println("line: " + result);
//            p.waitFor();
//            System.out.println ("exit: " + p.exitValue());
//            p.destroy();
//        } catch (Exception e) {
//            logger.error("Error occured while cleanup", e);
//            Assert.fail();
//        }
//
//    }
//
////    @AfterTest
////    private void cleanUp(){
////        System.out.print("Stopping airavata..........");
////        String options[] = {"stop"};
////        try {
////            ServerMain.test(options, null);
////            Process p;
////            try {
////                List<String> s = new ArrayList<String>();
////                String result = null;
////                p = Runtime.getRuntime().exec("find . -name server_start_*");
////                BufferedReader br = new BufferedReader(
////                        new InputStreamReader(p.getInputStream()));
////                while ((result = br.readLine()) != null)
////                    s.add(result);
////                for(String st : s){
////                    p = Runtime.getRuntime().exec("rm -rf " + st);
////                    br = new BufferedReader(
////                            new InputStreamReader(p.getInputStream()));
////                    while ((result = br.readLine()) != null)
////                        System.out.println("line: " + result);
////                    String proc= st.substring(st.lastIndexOf("_") + 1);
////                    p = Runtime.getRuntime().exec("kill -9 " + proc);
////                    br = new BufferedReader(
////                            new InputStreamReader(p.getInputStream()));
////                    while ((result = br.readLine()) != null)
////                        System.out.println("line: " + result);
////                }
////                p.waitFor();
////                System.out.println ("exit: " + p.exitValue());
////                p.destroy();
////                initialize.stopDerbyServer();
////            } catch (Exception e) {}
////
////        } catch (ParseException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        } catch (AiravataException e) {
////            e.printStackTrace();
////        }
////    }
//
//    public Map<String, String> readTokens () throws Exception{
//        Map<String, String> tokens = new HashMap<String, String>();
//        String fileLocation = properties.getTokenFileLoc();
//        String fileName = TestFrameworkConstants.CredentialStoreConstants.TOKEN_FILE_NAME;
//        String path = fileLocation + File.separator + fileName;
//        File tokenFile = new File(path);
//        if (tokenFile.exists()){
//            FileInputStream fis = new FileInputStream(tokenFile);
//            //Construct BufferedReader from InputStreamReader
//            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] strings = line.split(":");
//                tokens.put(strings[0], strings[1]);
//            }
//            br.close();
//        }else {
//            throw new Exception("Could not find token file.. Please run application registration step if you haven't run it");
//        }
//        return tokens;
//    }
//}

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
package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.testsuite.multitenantedairavata.utils.ApplicationProperties;
import org.apache.airavata.testsuite.multitenantedairavata.utils.ComputeResourceProperties;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_VERSION;
import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.*;
import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LocalApplication.*;
import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LocalEchoComputeResource.*;

public class AiravataIT {
    private final static Logger logger = LoggerFactory.getLogger(AiravataIT.class);
    private static TestFrameworkProps properties;
    private static Map<String, String> tokens;
    private static ExperimentExecution experimentExecution;
    private FrameworkSetup setup;
    private Object lock = new Object();
    private String storageResource;
    private ComputeResourceProperties computeResourceProperties;
    private ApplicationProperties applicationProperties;
    private String experimentId;

    @BeforeTest
    public void setupAiravata(){
        Process p;
        try {
            logger.info("setupAiravata() -> Starting airavata.......");
            p = Runtime.getRuntime().exec("./airavata-server-stop.sh -f");
            p = Runtime.getRuntime().exec("./startup-airavata.sh");
            Thread.sleep(15000);
            setup = FrameworkSetup.getInstance();
            properties = setup.getTestFrameworkProps();
            logger.info("setupAiravata() -> Started airavata.");
        } catch (Exception e) {
            logger.error("Error occured while set up", e);
            Assert.fail();
        }
    }

    @org.testng.annotations.Test(priority=1)
    public void testCreateGateway(){
        try {
            logger.info("testCreateGateway() -> Creating test gateway......");
            setup.getGatewayRegister().createGateway();
            GatewayResourceProfile gatewayResourceProfile = setup.getGatewayRegister().getGatewayResourceProfile();
            Assert.assertNotNull(gatewayResourceProfile);
            Gateway testGateway = setup.getGatewayRegister().getGateway(properties.getGname());
            Assert.assertNotNull(testGateway);
            Assert.assertEquals(testGateway.getGatewayName(), properties.getGname());
            Assert.assertEquals(testGateway.getDomain(), properties.getGname() + properties.getGdomain());
            Assert.assertEquals(testGateway.getGatewayApprovalStatus(), GatewayApprovalStatus.APPROVED);

            String createdToken = setup.getGatewayRegister().writeToken();
            Assert.assertEquals(new String(Files.readAllBytes(Paths.get(properties.getTokenFileLoc()+ File.separator + TestFrameworkConstants.CredentialStoreConstants.TOKEN_FILE_NAME))), createdToken);
            /*
            TODO: Not required for local setup
            FIXME: for scp transfer
            Currently credential store does not support jpa its plane sql and need
             */
            //setup.getGatewayRegister().registerSSHKeys();

            logger.info("testCreateGateway() -> Created test gateway. Gateway Id : " + properties.getGname());
        } catch (Exception e) {
            logger.error("Error occured while testCreateGateways", e);
            Assert.fail();
        }
    }

    @org.testng.annotations.Test(priority=2)
    public void testComputeResource(){
        try {

            logger.info("testComputeResource() -> Creating test compute resource......");

            computeResourceProperties = setup.getComputeResourceRegister().addComputeResources();
            Assert.assertNotNull(computeResourceProperties);

            Assert.assertNotNull(computeResourceProperties.getComputeResourceId());
            Assert.assertNotNull(computeResourceProperties.getJobSubmissionId());


            ComputeResourceDescription computeResourceDescription = setup.getComputeResourceRegister().getComputeResource(computeResourceProperties.getComputeResourceId());
            Assert.assertNotNull(computeResourceDescription.getHostName(), HOST_NAME);
            Assert.assertNotNull(computeResourceDescription.getResourceDescription(), HOST_DESC);

            LOCALSubmission localSubmission = setup.getComputeResourceRegister().getLocalSubmission(computeResourceProperties.getJobSubmissionId());

            Assert.assertEquals(localSubmission.getResourceJobManager().getResourceJobManagerType(), ResourceJobManagerType.FORK);
            Assert.assertEquals(localSubmission.getSecurityProtocol(), SecurityProtocol.LOCAL);
            Assert.assertEquals(localSubmission.getResourceJobManager().getPushMonitoringEndpoint(), null);
            Assert.assertEquals(localSubmission.getResourceJobManager().getJobManagerBinPath(), "");
            Assert.assertEquals(localSubmission.getResourceJobManager().getJobManagerCommands().get(JobManagerCommand.SUBMISSION), JOB_MANAGER_COMMAND);

            setup.getComputeResourceRegister().registerGatewayResourceProfile(computeResourceProperties.getComputeResourceId());

            ComputeResourcePreference computeResourcePreference = setup.getComputeResourceRegister().getGatewayComputeResourcePreference(properties.getGname(), computeResourceProperties.getComputeResourceId());

            Assert.assertEquals(computeResourcePreference.getAllocationProjectNumber(), ALLOCATION_PROJECT_NUMBER);
            Assert.assertEquals(computeResourcePreference.getPreferredBatchQueue(), BATCH_QUEUE);
            Assert.assertEquals(computeResourcePreference.getPreferredDataMovementProtocol(), DataMovementProtocol.LOCAL);
            Assert.assertEquals(computeResourcePreference.getPreferredJobSubmissionProtocol(), JobSubmissionProtocol.LOCAL);
            Assert.assertEquals(computeResourcePreference.getScratchLocation(), TestFrameworkConstants.SCRATCH_LOCATION);
            Assert.assertEquals(computeResourcePreference.getLoginUserName(), LOGIN_USER);

            logger.info("testComputeResource() -> Created test compute resource." + computeResourceProperties.toString());

        } catch (Exception e) {
            logger.error("Error occured while testComputeResource", e);
            Assert.fail();
        }
    }


    @org.testng.annotations.Test(priority=3)
    public void testStorageResource(){
        try {

            logger.info("testStorageResource() -> Creating test storage resource......");

            storageResource = setup.getStorageResourceRegister().addStorageResourceResource();;
            Assert.assertNotNull(storageResource);

            StorageResourceDescription storageResourceDescription = setup.getStorageResourceRegister().getStorageResourceDescription(storageResource);
            Assert.assertNotNull(storageResourceDescription.getHostName(), HOST_NAME);
            Assert.assertNotNull(storageResourceDescription.getStorageResourceDescription(), HOST_DESC);

            setup.getStorageResourceRegister().registerGatewayStorageProfile(storageResource);

            StoragePreference storagePreference = setup.getStorageResourceRegister().getStoragePreference(properties.getGname(), storageResource);

            Assert.assertEquals(storagePreference.getLoginUserName(), LOGIN_USER);
            Assert.assertEquals(storagePreference.getFileSystemRootLocation(), TestFrameworkConstants.STORAGE_LOCATION);

            logger.info("testStorageResource() -> Created test storage resource. Storage Resource Id : " + storageResource);

        } catch (Exception e) {
            logger.error("Error occured while testStorageResource", e);
            Assert.fail();
        }
    }



    @org.testng.annotations.Test(priority=4)
    public void testAddApplication(){
        try {

            logger.info("testAddApplication() -> Adding test application......");

            applicationProperties = setup.getApplicationRegister().addApplications();;
            Assert.assertNotNull(applicationProperties);

            ApplicationModule applicationModule = setup.getApplicationRegister().getApplicationModule(applicationProperties.getApplicationModuleId());
            Assert.assertNotNull(applicationModule);
            Assert.assertEquals(applicationModule.getAppModuleName(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME);
            Assert.assertEquals(applicationModule.getAppModuleVersion(), LOCAL_ECHO_VERSION);
            Assert.assertEquals(applicationModule.getAppModuleDescription(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION);
            Assert.assertEquals(applicationModule.getAppModuleName(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME);


            ApplicationInterfaceDescription applicationInterfaceDescription = setup.getApplicationRegister().getApplicationInterfaceDescription(applicationProperties.getApplicationInterfaceId());
            Assert.assertNotNull(applicationInterfaceDescription);
            Assert.assertEquals(applicationInterfaceDescription.getApplicationName(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME);
            Assert.assertEquals(applicationInterfaceDescription.getApplicationDescription(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION);

            InputDataObjectType input = applicationInterfaceDescription.getApplicationInputs().get(0);
            Assert.assertNotNull(input);
            Assert.assertEquals(input.getName(), INPUT_NAME);
            Assert.assertEquals(input.getValue(), INPUT_VALUE);
            Assert.assertEquals(input.getType(), DataType.STRING);
            Assert.assertEquals(input.getApplicationArgument(), null);
            Assert.assertEquals(input.getInputOrder(), 0);
            Assert.assertEquals(input.isIsRequired(), true);
            Assert.assertEquals(input.isRequiredToAddedToCommandLine(), true);
            Assert.assertEquals(input.isStandardInput(), false);
            Assert.assertEquals(input.getUserFriendlyDescription(), INPUT_DESC);
            Assert.assertEquals(input.getMetaData(), null);

            List<OutputDataObjectType> outputDataObjectTypes = applicationInterfaceDescription.getApplicationOutputs();
            Assert.assertNotNull(outputDataObjectTypes.get(0));
            Assert.assertEquals(outputDataObjectTypes.get(0).getName(), STDERR_NAME);
            Assert.assertEquals(outputDataObjectTypes.get(0).getValue(), STDERR_VALUE);
            Assert.assertEquals(outputDataObjectTypes.get(0).getType(), DataType.URI);
            Assert.assertEquals(outputDataObjectTypes.get(0).isIsRequired(), true);
            Assert.assertEquals(outputDataObjectTypes.get(0).isRequiredToAddedToCommandLine(), true);
            Assert.assertEquals(outputDataObjectTypes.get(0).getApplicationArgument(), null);


            Assert.assertNotNull(outputDataObjectTypes.get(1));
            Assert.assertEquals(outputDataObjectTypes.get(1).getName(), STDOUT_NAME);
            Assert.assertEquals(outputDataObjectTypes.get(1).getValue(), STDOUT_VALUE);
            Assert.assertEquals(outputDataObjectTypes.get(1).getType(), DataType.URI);
            Assert.assertEquals(outputDataObjectTypes.get(1).isIsRequired(), true);
            Assert.assertEquals(outputDataObjectTypes.get(1).isRequiredToAddedToCommandLine(), true);
            Assert.assertEquals(outputDataObjectTypes.get(1).getApplicationArgument(), null);



            ApplicationDeploymentDescription applicationDeploymentDescription = setup.getApplicationRegister().getApplicationDeploymentDescription(applicationProperties.getApplicationDeployId());
            Assert.assertNotNull(applicationDeploymentDescription);

            Assert.assertEquals(applicationDeploymentDescription.getExecutablePath(), TestFrameworkConstants.LOCAL_ECHO_JOB_FILE_PATH);
            Assert.assertEquals(applicationDeploymentDescription.getParallelism(), ApplicationParallelismType.SERIAL);
            Assert.assertEquals(applicationDeploymentDescription.getAppDeploymentDescription(), TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION);
            Assert.assertEquals(applicationDeploymentDescription.getModuleLoadCmds(), null);
            Assert.assertEquals(applicationDeploymentDescription.getPostJobCommands(), null);
            Assert.assertEquals(applicationDeploymentDescription.getPreJobCommands(), null);

            logger.info("testAddApplication() -> Adding test application." + applicationProperties.toString());

        } catch (Exception e) {
            logger.error("Error occured while testAddApplication", e);
            Assert.fail();
        }
    }

    @org.testng.annotations.Test(priority=5)
    public void testCreateExperiment(){
        try {

            logger.info("testCreateExperiment() -> Creating test experiment.");

            tokens = readTokens();
            experimentExecution = new ExperimentExecution(setup.getAiravata(), tokens, properties);
            experimentId = experimentExecution.createLocalEchoExperiment(properties.getGname(), applicationProperties.getApplicationInterfaceId(), storageResource, computeResourceProperties.getComputeResourceId());
            Assert.assertNotNull(experimentId);

            ExperimentModel simpleExperiment = experimentExecution.getExperimentModel(experimentId);
            Assert.assertNotNull(simpleExperiment);
            UserConfigurationDataModel userConfigurationData= simpleExperiment.getUserConfigurationData();
            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getResourceHostId(), computeResourceProperties.getComputeResourceId());
            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getTotalCPUCount(), 4);
            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getNodeCount(), 1);
            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getNumberOfThreads(), 1);
            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getQueueName(), "cpu");
            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getWallTimeLimit(), 20);
            Assert.assertEquals(userConfigurationData.getComputationalResourceScheduling().getTotalPhysicalMemory(), 0);

            Assert.assertEquals(userConfigurationData.isAiravataAutoSchedule(), false);
            Assert.assertEquals(userConfigurationData.isOverrideManualScheduledParams(), false);
            Assert.assertEquals(userConfigurationData.getStorageId(), storageResource);
            Assert.assertEquals(userConfigurationData.getExperimentDataDir(), TestFrameworkConstants.STORAGE_LOCATION);

            logger.info("testCreateExperiment() -> Created test experiment. Experiment Id : " + experimentId);

        } catch (Exception e) {
            logger.error("Error occured while testCreateApplication", e);
            Assert.fail();
        }
    }

    @org.testng.annotations.Test(priority=6)
    public void testLaunchExperiment(){
        try {

            logger.info("testLaunchExperiment() -> Launching test experiment......");

            experimentExecution.launchExperiments(properties.getGname(), experimentId);
            experimentExecution.monitorExperiments();
            ExperimentStatus experimentStatus = experimentExecution.getExperimentStatus(experimentId);
            int maxTry = 5;
            while( maxTry != 0 && !experimentStatus.getState().equals(ExperimentState.COMPLETED) && !experimentStatus.getState().equals(ExperimentState.FAILED)){
                experimentStatus = experimentExecution.getExperimentStatus(experimentId);
                maxTry--;
                Thread.sleep(2000);
            }
            if(experimentStatus.getState().equals(ExperimentState.COMPLETED)){
               Assert.assertEquals(new String(Files.readAllBytes(Paths.get(TestFrameworkConstants.STORAGE_LOCATION +"stdout.txt"))), LOCAL_ECHO_EXPERIMENT_EXPECTED_OUTPUT+"\n");
                logger.info("testLaunchExperiment() -> Test experiment completed");
            }else if(experimentStatus.getState().equals(ExperimentState.FAILED)) {
                Assert.fail("Experiment failed to execute");
            }else{
                Assert.fail("Failed to execute experiment in 10 seconds.");
            }
        } catch (Exception e) {
            logger.error("Error occured while testLaunchExperiment", e);
            Assert.fail();
        }
    }

    @AfterTest(alwaysRun = true)
    private void cleanUp(){
        logger.info("cleanUp() -> Launching test experiment......");
        Process p;
        try {
            String result = null;
            p = Runtime.getRuntime().exec("base-airavata/apache-airavata-server-0.17-SNAPSHOT/bin/airavata-server-stop.sh -f");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((result = br.readLine()) != null)
                System.out.println("line: " + result);
            p.waitFor();
            System.out.println ("exit: " + p.exitValue());
            p.destroy();
        } catch (Exception e) {
            logger.error("Error occured while cleanup", e);
            Assert.fail();
        }

    }

    public Map<String, String> readTokens () throws Exception{
        Map<String, String> tokens = new HashMap<String, String>();
        String fileLocation = properties.getTokenFileLoc();
        String fileName = TestFrameworkConstants.CredentialStoreConstants.TOKEN_FILE_NAME;
        String path = fileLocation + File.separator + fileName;
        File tokenFile = new File(path);
        if (tokenFile.exists()){
            FileInputStream fis = new FileInputStream(tokenFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = br.readLine()) != null) {
                String[] strings = line.split(":");
                tokens.put(strings[0], strings[1]);
            }
            br.close();
        }else {
            throw new Exception("Could not find token file.. Please run application registration step if you haven't run it");
        }
        return tokens;
    }
}

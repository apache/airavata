/*
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
 *
 */

package org.apache.airavata.client.samples;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.airavata.model.error.*;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ClientSettings;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.DocumentCreator;
import org.apache.airavata.client.tools.DocumentCreatorNew;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateLaunchExperiment {

    //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
    private final static Logger logger = LoggerFactory.getLogger(CreateLaunchExperiment.class);
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    private static Airavata.Client client;
    private static String localHostAppId = "localhost_2342b39f-5870-4704-9a52-e631dc790af7,SimpleEcho0_17c5e56e-beda-42f2-a7ec-5c7654177bc3gw111.iu.xsede.org_d532e952-ca5e-494e-942f-526658bdc13f,DO_NOT_SET_AT_CLIENTS";
    private static String sshHostAppId;
    private static String pbsEchoAppId = "trestles.sdsc.edu_278c4814-035d-440b-a192-ab5f03e7445c,SimpleEcho2_9b7a09fc-4874-4cbf-af9c-6859c31ddb94";
    private static String pbsWRFAppId = "trestles.sdsc.edu_00482169-8fc4-4633-b779-5ca1f66f27c1,WRF_0f1e90d3-5915-4629-a5a4-73346c1e7535";
    private static String slurmAppId = "stampede.tacc.xsede.org_b2ef59cb-f626-4767-9ca0-601f94c42ba4,SimpleEcho3_b81c2559-a088-42a3-84ce-40119d874918";
    private static String sgeAppId;
    private static String br2EchoAppId = "bigred2_f435286c-545b-4b31-8366-3e1c6df66c9a,SimpleEchoBR_223a4aa8-fdb9-4d4f-b3cd-b9253c3c9af";
    private static String slurmWRFAppId = "stampede.tacc.xsede.org_459dde5e-4a4f-4d37-ac80-464561b103e6,WRF_7967d316-d92b-4be6-9cde-37dfc4b9ecf5";

    public static void main(String[] args) {
        try {
            AiravataUtils.setExecutionAsClient();
            client = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + client.getAPIVersion());
            addDescriptors();

//            final String expId = createExperimentForSSHHost(airavata);
//            final String expId = createExperimentForTrestles(client);
//            final String expId = createExperimentForStampede(client);
//            final String expId = createExperimentForLocalHost(client);
//            final String expId = createExperimentForLonestar(airavata);
//            final String expId = createExperimentWRFTrestles(client);
//            final String expId = createExperimentForBR2(client);
            final String expId = createExperimentWRFStampede(client);

            System.out.println("Experiment ID : " + expId);
//            updateExperiment(airavata, expId);
            launchExperiment(client, expId);

//            System.out.println("retrieved exp id : " + experiment.getExperimentID());
        } catch (Exception e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addDescriptors() throws AiravataAPIInvocationException, ApplicationSettingsException {
        try {
            DocumentCreatorNew documentCreator = new DocumentCreatorNew(client);
//            DocumentCreator documentCreator = new DocumentCreator(getAiravataAPI());
//            localHostAppId = documentCreator.createLocalHostDocs();
//            sshHostAppId = documentCreator.createSSHHostDocs();
//            documentCreator.createGramDocs();
//            pbsEchoAppId =documentCreator.createPBSDocsForOGCE_Echo();
//            pbsWRFAppId =documentCreator.createPBSDocsForOGCE_WRF();
//            slurmAppId = documentCreator.createSlurmDocs();
//            sgeAppId = documentCreator.createSGEDocs();
//            documentCreator.createEchoHostDocs();
//            br2EchoAppId = documentCreator.createBigRedDocs();
            slurmWRFAppId = documentCreator.createSlumWRFDocs();
            System.out.printf(localHostAppId);
            System.out.println(sshHostAppId);
            System.out.println(pbsEchoAppId);
            System.out.println(pbsWRFAppId);
            System.out.println(slurmAppId);
            System.out.println(sgeAppId);
            System.out.println(br2EchoAppId);
            System.out.println(slurmWRFAppId);
        } catch (Exception e) {
            logger.error("Unable to create documents", e.getMessage());
            throw new ApplicationSettingsException(e.getMessage());
        }
    }

    public static String createExperimentForTrestles(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "echoExperiment", "SimpleEcho2", pbsEchoAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(pbsEchoAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "sds128");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String createExperimentWRFStampede(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("WRF_Namelist");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/namelist.input");

            DataObjectType input1 = new DataObjectType();
            input1.setKey("WRF_Input_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/wrfinput_d01");

            DataObjectType input2 = new DataObjectType();
            input2.setKey("WRF_Boundary_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/wrfbdy_d01");

            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("WRF_Output");
            output.setType(DataType.URI);
            output.setValue("");

            DataObjectType output1 = new DataObjectType();
            output1.setKey("WRF_Execution_Log");
            output1.setType(DataType.URI);
            output1.setValue("");


            exOut.add(output);
            exOut.add(output1);


            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "WRFExperiment", "Testing", slurmWRFAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(slurmWRFAppId.split(",")[0], 32, 2, 1, "development", 90, 0, 1, "TG-STA110014S");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String createExperimentWRFTrestles(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("WRF_Namelist");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/namelist.input");

            DataObjectType input1 = new DataObjectType();
            input1.setKey("WRF_Input_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/wrfinput_d01");

            DataObjectType input2 = new DataObjectType();
            input2.setKey("WRF_Boundary_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/wrfbdy_d01");

            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("WRF_Output");
            output.setType(DataType.URI);
            output.setValue("");

            DataObjectType output1 = new DataObjectType();
            output1.setKey("WRF_Execution_Log");
            output1.setType(DataType.URI);
            output1.setValue("");


            exOut.add(output);
            exOut.add(output1);


            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "WRFExperiment", "Testing", pbsWRFAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(pbsWRFAppId.split(",")[0], 32, 2, 1, "normal", 1, 0, 1, "sds128");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String cloneExperiment(Airavata.Client client, String expId) throws TException {
        try {
            return client.cloneExperiment(expId, "cloneExperiment1");
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }


    public static void updateExperiment(Airavata.Client client, String expId) throws TException {
        try {
            Experiment experiment = client.getExperiment(expId);
            experiment.setDescription("updatedDescription");
            client.updateExperiment(expId, experiment);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }


    public static String createExperimentForLocalHost(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "Echo Test", localHostAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(localHostAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String createExperimentForSSHHost(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SSHEcho1", sshHostAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(sshHostAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "sds128");
            scheduling.setResourceHostId("gw111.iu.xsede.org");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String createExperimentForStampede(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "SimpleEcho3", slurmAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling =
                    ExperimentModelUtil.createComputationResourceScheduling(slurmAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "TG-STA110014S");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String createExperimentForLonestar(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "SimpleEcho4", sgeAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling =
                    ExperimentModelUtil.createComputationResourceScheduling(sgeAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "TG-STA110014S");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (LaunchValidationException e) {
            logger.error("Validation failed" + e.getErrorMessage());
            org.apache.airavata.model.error.ValidationResults validationResult = e.getValidationResult();
            for (org.apache.airavata.model.error.ValidatorResult vResult : validationResult.getValidationResultList()) {
                if (!vResult.isSetResult()) {
                    System.out.println("Error:" + vResult.getErrorDetails());
                }
            }
            throw e;
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String createExperimentForBR2(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", br2EchoAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(br2EchoAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, null);
            scheduling.setResourceHostId("bigred2.uits.iu.edu");
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static void launchExperiment(Airavata.Client client, String expId)
            throws TException {
        try {
            String sshTokenId = "9a269d4b-b2a5-4dc6-b83a-895b9efca56a";
            String gsisshTokenId = "7a8b801f-23e7-465f-b5e4-3cf1e8bee2ab";
            client.launchExperiment(expId, gsisshTokenId);
        } catch (ExperimentNotFoundException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new ExperimentNotFoundException(e);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static List<Experiment> getExperimentsForUser(Airavata.Client client, String user) {
        try {
            return client.getAllUserExperiments(user);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Project> getAllUserProject(Airavata.Client client, String user) {
        try {
            return client.getAllUserProjects(user);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Project> searchProjectsByProjectName(Airavata.Client client, String user, String projectName) {
        try {
            return client.searchProjectsByProjectName(user, projectName);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Project> searchProjectsByProjectDesc(Airavata.Client client, String user, String desc) {
        try {
            return client.searchProjectsByProjectDesc(user, desc);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<ExperimentSummary> searchExperimentsByName(Airavata.Client client, String user, String expName) {
        try {
            return client.searchExperimentsByName(user, expName);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExperimentSummary> searchExperimentsByDesc(Airavata.Client client, String user, String desc) {
        try {
            return client.searchExperimentsByDesc(user, desc);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExperimentSummary> searchExperimentsByApplication(Airavata.Client client, String user, String app) {
        try {
            return client.searchExperimentsByApplication(user, app);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }
}

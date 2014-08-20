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
import org.apache.airavata.persistance.registry.jpa.model.ErrorDetail;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.workflow.model.component.system.StreamSourceComponent;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlsoap.schemas.soap.encoding.*;

import java.lang.String;
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
    private static String localHostAppId = "localhost_3b5962d3-5e7e-4a97-9d1f-25c5ec436ba5,SimpleEcho0_44c34394-ca27-4fa9-bb2d-87f95a02352a";
    private static String sshHostAppId;
    private static String pbsEchoAppId = "trestles.sdsc.edu_03b34af0-f55d-4cb3-9bce-abba35d8b30d,SimpleEcho2_e8ca0bb2-d985-4775-884b-a27b29a33251";
    private static String pbsWRFAppId = "trestles.sdsc.edu_42adfdf7-d7bc-474a-8905-6624817b22ef,WRF_24f50b3c-4f1e-4358-bb10-c6838b12e231";
    private static String slurmAppId = "stampede.tacc.xsede.org_b2ef59cb-f626-4767-9ca0-601f94c42ba4,SimpleEcho3_b81c2559-a088-42a3-84ce-40119d874918";
    private static String sgeAppId;
    private static String br2EchoAppId = "bigred2_9c1e6be8-f7d8-4494-98f2-bf508790e8c6,SimpleEchoBR_149fd613-98e2-46e7-ac7c-4d393349469e";
    private static String slurmWRFAppId = "stampede.tacc.xsede.org_2840c815-7e61-4579-8194-79fe15cea9a9,WRF_00817e82-7995-4986-8fe2-72da08b63ef0";
    private static String br2AmberAppId = "bigred2_5dc35993-31c4-499e-97c1-8d934007e135,AmberBR2_f63fd6f9-a93f-43a8-bd41-065740a32f1f";
    private static String slurmAmberAppId = "bigred2_5dc35993-31c4-499e-97c1-8d934007e135,AmberBR2_f63fd6f9-a93f-43a8-bd41-065740a32f1f";
    private static String trestlesAmberAppId = "trestles.sdsc.edu_8ca93e3d-135c-4e3a-bf58-bdcc2592625d,AmberTrestles_ea0e8e82-3b00-4ef7-9a78-867cfecebbf1";



    public static void main(String[] args) {
        try {
            AiravataUtils.setExecutionAsClient();
            client = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + client.getAPIVersion());
//            getExperiment(client, "WRFExperiment_2a2de26c-7f74-47c9-8e14-40e50dedfe0f");
//            addDescriptors();

////            final String expId = createExperimentForSSHHost(airavata);
//            final String expId = createExperimentForTrestles(client);
////            final String expId = createExperimentForStampede(client);
//            final String expId = createExperimentForLocalHost(client);
//            final String expId = createExperimentForLonestar(airavata);
            final String expId = createExperimentWRFTrestles(client);
//            final String expId = createExperimentForBR2(client);
//            final String expId = createExperimentForBR2Amber(client);
//            final String expId = createExperimentWRFStampede(client);
//            final String expId = createExperimentForStampedeAmber(client);
//            final String expId = createExperimentForTrestlesAmber(client);

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
            localHostAppId = documentCreator.createLocalHostDocs();
            sshHostAppId = documentCreator.createSSHHostDocs();
//            documentCreator.createGramDocs();
            pbsEchoAppId =documentCreator.createPBSDocsForOGCE_Echo();
            pbsWRFAppId =documentCreator.createPBSDocsForOGCE_WRF();
            slurmAppId = documentCreator.createSlurmDocs();
            sgeAppId = documentCreator.createSGEDocs();
//            documentCreator.createEchoHostDocs();
            br2EchoAppId = documentCreator.createBigRedDocs();
            slurmWRFAppId = documentCreator.createSlumWRFDocs();
            br2AmberAppId = documentCreator.createBigRedAmberDocs();
            slurmAmberAppId = documentCreator.createStampedeAmberDocs();
            trestlesAmberAppId = documentCreator.createTrestlesAmberDocs();
            System.out.printf(localHostAppId);
            System.out.println(sshHostAppId);
            System.out.println(pbsEchoAppId);
            System.out.println(pbsWRFAppId);
            System.out.println(slurmAppId);
            System.out.println(sgeAppId);
            System.out.println(br2EchoAppId);
            System.out.println(slurmWRFAppId);
            System.out.println(br2AmberAppId);
            System.out.println(trestlesAmberAppId);
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

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(slurmWRFAppId.split(",")[0], 2, 32, 1, "development", 90, 0, 1, "TG-STA110014S");
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
//            scheduling.setResourceHostId("bigred2.uits.iu.edu");
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

    public static String createExperimentForBR2Amber(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("AMBER_HEAT_RST");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/02_Heat.rst");
            exInputs.add(input);

            DataObjectType input1 = new DataObjectType();
            input1.setKey("AMBER_PROD_IN");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/03_Prod.in");
            exInputs.add(input1);

            DataObjectType input2 = new DataObjectType();
            input2.setKey("AMBER_PRMTOP");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/prmtop");
            exInputs.add(input2);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("AMBER_Prod.info");
            output.setType(DataType.URI);
            output.setValue("");
            exOut.add(output);

            DataObjectType output1 = new DataObjectType();
            output1.setKey("AMBER_Prod.mdcrd");
            output1.setType(DataType.URI);
            output1.setValue("");
            exOut.add(output1);
            DataObjectType output2 = new DataObjectType();
            output2.setKey("AMBER_Prod.out");
            output2.setType(DataType.URI);
            output2.setValue("");
            exOut.add(output2);
            DataObjectType output3 = new DataObjectType();
            output3.setKey("AMBER_Prod.rst");
            output3.setType(DataType.URI);
            output3.setValue("");
            exOut.add(output3);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", br2AmberAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(br2AmberAppId.split(",")[0], 4, 1, 1, "cpu", 20, 0, 1, null);
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

    public static String createExperimentForStampedeAmber(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("AMBER_HEAT_RST");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/02_Heat.rst");
            exInputs.add(input);

            DataObjectType input1 = new DataObjectType();
            input1.setKey("AMBER_PROD_IN");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/03_Prod.in");
            exInputs.add(input1);

            DataObjectType input2 = new DataObjectType();
            input2.setKey("AMBER_PRMTOP");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/prmtop");
            exInputs.add(input2);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("AMBER_Prod.info");
            output.setType(DataType.URI);
            output.setValue("");
            exOut.add(output);

            DataObjectType output1 = new DataObjectType();
            output1.setKey("AMBER_Prod.mdcrd");
            output1.setType(DataType.URI);
            output1.setValue("");
            exOut.add(output1);
            DataObjectType output2 = new DataObjectType();
            output2.setKey("AMBER_Prod.out");
            output2.setType(DataType.URI);
            output2.setValue("");
            exOut.add(output2);
            DataObjectType output3 = new DataObjectType();
            output3.setKey("AMBER_Prod.rst");
            output3.setType(DataType.URI);
            output3.setValue("");
            exOut.add(output3);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", slurmAmberAppId.split(",")[1], exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(slurmAmberAppId.split(",")[0], 4, 1, 1, "development", 20, 0, 1, null);
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

    public static String createExperimentForTrestlesAmber(Airavata.Client client) throws TException {
           try {
               List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
               DataObjectType input = new DataObjectType();
               input.setKey("AMBER_HEAT_RST");
               input.setType(DataType.URI);
               input.setValue("/Users/lahirugunathilake/Downloads/02_Heat.rst");
               exInputs.add(input);

               DataObjectType input1 = new DataObjectType();
               input1.setKey("AMBER_PROD_IN");
               input1.setType(DataType.URI);
               input1.setValue("/Users/lahirugunathilake/Downloads/03_Prod.in");
               exInputs.add(input1);

               DataObjectType input2 = new DataObjectType();
               input2.setKey("AMBER_PRMTOP");
               input2.setType(DataType.URI);
               input2.setValue("/Users/lahirugunathilake/Downloads/prmtop");
               exInputs.add(input2);

               List<DataObjectType> exOut = new ArrayList<DataObjectType>();
               DataObjectType output = new DataObjectType();
               output.setKey("AMBER_Prod.info");
               output.setType(DataType.URI);
               output.setValue("");
               exOut.add(output);

               DataObjectType output1 = new DataObjectType();
               output1.setKey("AMBER_Prod.mdcrd");
               output1.setType(DataType.URI);
               output1.setValue("");
               exOut.add(output1);
               DataObjectType output2 = new DataObjectType();
               output2.setKey("AMBER_Prod.out");
               output2.setType(DataType.URI);
               output2.setValue("");
               exOut.add(output2);
               DataObjectType output3 = new DataObjectType();
               output3.setKey("AMBER_Prod.rst");
               output3.setType(DataType.URI);
               output3.setValue("");
               exOut.add(output3);

               Project project = ProjectModelUtil.createProject("default", "admin", "test project");
               String projectId = client.createProject(project);

               Experiment simpleExperiment =
                       ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", trestlesAmberAppId.split(",")[1], exInputs);
               simpleExperiment.setExperimentOutputs(exOut);

               ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(trestlesAmberAppId.split(",")[0], 4, 1, 1, "normal", 20, 0, 1, null);
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
            String sshTokenId = "61abd2ff-f92b-4901-a077-07b51abe2c5d";
            String gsisshTokenId = "61abd2ff-f92b-4901-a077-07b51abe2c5d";
            client.launchExperiment(expId, sshTokenId);
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

    public static void getExperiment (Airavata.Client client, String expId) throws Exception{
        try{
            Experiment experiment = client.getExperiment(expId);
            List<ErrorDetails> errors = experiment.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails error : errors){
                    System.out.println("ERROR MESSAGE : " + error.getActualErrorMessage());
                }
            }

        } catch (ExperimentNotFoundException e) {
            logger.error("Experiment does not exist", e);
            throw new ExperimentNotFoundException("Experiment does not exist");
        } catch (AiravataSystemException e) {
            logger.error("Error while retrieving experiment", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (InvalidRequestException e) {
            logger.error("Error while retrieving experiment", e);
            throw new InvalidRequestException("Error while retrieving experiment");
        } catch (AiravataClientException e) {
            logger.error("Error while retrieving experiment", e);
            throw new AiravataClientException(AiravataErrorType.INTERNAL_ERROR);
        }
    }
}

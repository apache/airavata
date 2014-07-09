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
    private static String localHostAppId;
    private static String sshHostAppId;
    private static String pbsEchoAppId="trestles.sdsc.edu_9fcdcdc7-119a-434b-a5ef-8ed67544e855,SimpleEcho2_141c96db-46de-446e-9c39-8234a8bf9d5c";
    private static String pbsWRFAppId;
    private static String slurmAppId;
    private static String sgeAppId;
    public static void main(String[] args) {
        try {
            AiravataUtils.setExecutionAsClient();
            client = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + client.getAPIVersion());
            addDescriptors();

//            final String expId = createExperimentForSSHHost(airavata);
//            final String expId = createExperimentForTrestles(client);
//            final String expId = createExperimentForStampede(airavata);
            final String expId = createExperimentForLocalHost(client);
//            final String expId = createExperimentForLonestar(airavata);
//            final String expId = createExperimentWRFTrestles(airavata);
            System.out.println("Experiment ID : " + expId);
//            updateExperiment(airavata, expId);
            launchExperiment(client, expId);
            System.out.println("Launched successfully");
            List<Experiment> experiments = getExperimentsForUser(client, "admin");
            List<ExperimentSummary> searchedExps1 = searchExperimentsByName(client, "admin", "echo");
            List<ExperimentSummary> searchedExps2 = searchExperimentsByDesc(client, "admin", "Echo");
            List<ExperimentSummary> searchedExps3 = searchExperimentsByApplication(client, "admin", "cho");
            List<Project> projects = getAllUserProject(client, "admin");
            List<Project> searchProjects1 = searchProjectsByProjectName(client, "admin", "project");
            List<Project> searchProjects2 = searchProjectsByProjectDesc(client, "admin", "test");
            for (Experiment exp : experiments) {
                System.out.println(" exp id : " + exp.getExperimentID());
                System.out.println("experiment Description : " + exp.getDescription());
                if (exp.getExperimentStatus() != null) {
                    System.out.println(" exp status : " + exp.getExperimentStatus().getExperimentState().toString());
                }
            }

            for (ExperimentSummary exp : searchedExps1) {
                System.out.println("search results by experiment name");
                System.out.println("experiment ID : " + exp.getExperimentID());
                System.out.println("experiment Description : " + exp.getDescription());
                if (exp.getExperimentStatus() != null) {
                    System.out.println(" exp status : " + exp.getExperimentStatus().getExperimentState().toString());
                }
            }

            for (ExperimentSummary exp : searchedExps2) {
                System.out.println("search results by experiment desc");
                System.out.println("experiment ID : " + exp.getExperimentID());
                if (exp.getExperimentStatus() != null) {
                    System.out.println(" exp status : " + exp.getExperimentStatus().getExperimentState().toString());
                }
            }

            for (ExperimentSummary exp : searchedExps3) {
                System.out.println("search results by application");
                System.out.println("experiment ID : " + exp.getExperimentID());
                if (exp.getExperimentStatus() != null) {
                    System.out.println(" exp status : " + exp.getExperimentStatus().getExperimentState().toString());
                }
            }

            for (Project pr : searchProjects1) {
                System.out.println(" project id : " + pr.getProjectID());
            }

            for (Project pr : searchProjects2) {
                System.out.println(" project id : " + pr.getProjectID());
                System.out.println(" project desc : " + pr.getDescription());
            }

            Thread monitor = (new Thread() {
                public void run() {
                    Map<String, JobStatus> jobStatuses = null;
                    while (true) {
                        try {
                            Thread.sleep(5000);
                            jobStatuses = client.getJobStatuses(expId);
                            Set<String> strings = jobStatuses.keySet();
                            for (String key : strings) {
                                JobStatus jobStatus = jobStatuses.get(key);
                                if (jobStatus == null) {
                                    return;
                                } else {
                                    if (JobState.COMPLETE.equals(jobStatus.getJobState())) {
                                        System.out.println("Job completed Job ID: " + key);
                                        return;
                                    } else {
                                        System.out.println("Job ID:" + key + jobStatuses.get(key).getJobState().toString());
                                    }
                                }
                            }
                            ExperimentStatus experimentStatus = client.getExperimentStatus(expId);
                            if (experimentStatus.getExperimentState().equals(ExperimentState.FAILED)) {
                                return;
                            }
                            System.out.println(experimentStatus);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            monitor.start();
            try {
                monitor.join();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace(); // To change body of catch statement use
                // File | Settings | File Templates.
            }

//            System.out.println(airavata.getExperimentStatus(expId));
            List<DataObjectType> output = client.getExperimentOutputs(expId);
            for (DataObjectType dataObjectType : output) {
                System.out.println(dataObjectType.getKey() + " : " + dataObjectType.getType() + " : " + dataObjectType.getValue());


            }
            String clonedExpId = cloneExperiment(client, expId);
            System.out.println("Cloned Experiment ID : " + clonedExpId);
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
//            documentCreator.createBigRedDocs();
            System.out.printf(localHostAppId);
            System.out.println(sshHostAppId);
            System.out.println(pbsEchoAppId);
            System.out.println(pbsWRFAppId);
            System.out.println(slurmAppId);
            System.out.println(sgeAppId);
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

    public static String createExperimentWRFTrestles(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("WRF_Namelist");
            input.setType(DataType.URI);
            input.setValue("/Users/raminder/Downloads/wrf_sample_inputs/namelist.input");

            DataObjectType input1 = new DataObjectType();
            input1.setKey("WRF_Input_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/raminder/Downloads/wrf_sample_inputs/wrfinput_d01");

            DataObjectType input2 = new DataObjectType();
            input2.setKey("WRF_Boundary_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/raminder/Downloads/wrf_sample_inputs/wrfbdy_d01");

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

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(pbsWRFAppId.split(",")[0], 32, 2, 1, "normal", 0, 0, 1, "sds128");
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

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(localHostAppId.split(",")[0], 1, 1, 1, "normal", 0, 0, 1, "");
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

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(sshHostAppId.split(",")[0], 1, 1, 1, "normal", 0, 0, 1, "sds128");
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
                    ExperimentModelUtil.createComputationResourceScheduling(slurmAppId.split(",")[0], 1, 1, 1, "normal", 0, 0, 1, "TG-STA110014S");
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
                    ExperimentModelUtil.createComputationResourceScheduling(sgeAppId.split(",")[0], 1, 1, 1, "normal", 0, 0, 1, "TG-STA110014S");
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
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", "SimpleEchoBR", exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("bigred2.uits.iu.edu", 1, 1, 1, "normal", 0, 0, 1, "sds128");
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
            client.launchExperiment(expId, "testToken");
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

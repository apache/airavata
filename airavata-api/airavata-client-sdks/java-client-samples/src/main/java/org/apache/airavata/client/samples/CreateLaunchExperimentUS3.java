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
package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CreateLaunchExperimentUS3 {
	
	 //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
    private final static Logger logger = LoggerFactory.getLogger(CreateLaunchExperiment.class);
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    public static void main(String[] args) {
        try {
            final Airavata.Client airavata = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + airavata.getAPIVersion());
//            addDescriptors();
//            final String expId = createUS3ExperimentForTrestles(airavata);
            final String expId = createUS3ExperimentForStampede(airavata);
//            final String expId = createUS3ExperimentForLonestar(airavata);
//            final String expId =  createUS3ExperimentForAlamo(airavata);
            System.out.println("Experiment ID : " + expId);
            launchExperiment(airavata, expId);
            System.out.println("Launched successfully");
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//            Thread monitor = (new Thread(){
//                 public void run() {
//                     Map<String, JobStatus> jobStatuses = null;
//                     while (true) {
//                         try {
//                             jobStatuses = airavata.getJobStatuses(expId);
//                             Set<String> strings = jobStatuses.keySet();
//                             for (String key : strings) {
//                                 JobStatus jobStatus = jobStatuses.get(key);
//                                 if(jobStatus == null){
//                                     return;
//                                 }else {
//                                     if (JobState.COMPLETE.equals(jobStatus.getJobState())) {
//                                         System.out.println("Job completed Job ID: " + jobStatus.getJobState().toString());
//                                         return;
//                                     }else{
//                                        System.out.println("Job ID:" + key + jobStatuses.get(key).getJobState().toString());
//                                     }
//                                 }
//                             }
//                             Thread.sleep(20000);
//                         } catch (Exception e) {
//                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                         }
//                     }
//                 }
//            });
//            monitor.start();
//            try {
//                monitor.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
            
//            airavata.terminateExperiment(expId);
     
//            Experiment experiment = airavata.getExperiment(expId);
//            System.out.println("retrieved exp id : " + experiment.getExperimentID());
        } catch (Exception e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        }
    }
//    public static void addDescriptors() throws AiravataAPIInvocationException,ApplicationSettingsException  {
//        try {
//            UltrascanDocumentCreator documentCreator = new UltrascanDocumentCreator(getAiravataAPI());
//            documentCreator.createMPIPBSDocsTrestles();
//            documentCreator.createEchoPBSDocsforTestles();
//            documentCreator.createEchoSlurmDocsofStampede();
//            documentCreator.createMPISLURMDocsStampede();
//        } catch (AiravataAPIInvocationException e) {
//            logger.error("Unable to create airavata API", e.getMessage());
//            throw new AiravataAPIInvocationException(e);
//        } catch (ApplicationSettingsException e) {
//            logger.error("Unable to create airavata API", e.getMessage());
//            throw new ApplicationSettingsException(e.getMessage());
//        }
//    }

//    private static AiravataAPI getAiravataAPI() throws AiravataAPIInvocationException, ApplicationSettingsException {
//        AiravataAPI airavataAPI;
//        try {
//            String sysUser = ClientSettings.getSetting(DEFAULT_USER);
//            String gateway = ClientSettings.getSetting(DEFAULT_GATEWAY);
//            airavataAPI = AiravataAPIFactory.getAPI(gateway, sysUser);
//        } catch (AiravataAPIInvocationException e) {
//            logger.error("Unable to create airavata API", e.getMessage());
//            throw new AiravataAPIInvocationException(e);
//        } catch (ApplicationSettingsException e) {
//            logger.error("Unable to create airavata API", e.getMessage());
//            throw new ApplicationSettingsException(e.getMessage());
//        }
//        return airavataAPI;
//    }

    public static String createExperimentForTrestles(Airavata.Client client) throws TException  {
        try{
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "admin",
					        "US3EchoExperimentTrestles", "US3EchoTrestles", "US3EchoTrestles", exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("trestles.sdsc.edu", 1, 1, 1, "shared", 0, 0);
            scheduling.setResourceHostId("gsissh-trestles");
            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }
    
    public static String createUS3ExperimentForTrestles (Airavata.Client client) throws AiravataSystemException, InvalidRequestException, AiravataClientException, TException  {
        try{
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("input");
            input.setType(DataType.URI);
            input.setValue("file:///home/sgg/chathuri/laptop_backup/airavata/ultrascan_input/hpcinput.tar");
            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("walltime");
            input1.setType(DataType.STRING);
            input1.setValue("-walltime=60");
            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("mgroupcount");
            input2.setType(DataType.STRING);
            input2.setValue("-mgroupcount=1");
            
            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("output");
            output.setType(DataType.URI);
            output.setValue("");
//            OutputDataObjectType output1 = new OutputDataObjectType();
//            output1.setName("stdout");
//            output1.setType(DataType.STDOUT);
//            output1.setValue("");
//            OutputDataObjectType output2 = new OutputDataObjectType();
//            output2.setName("stderr");
//            output2.setType(DataType.STDERR);
//            output2.setValue("");
            exOut.add(output);
//            exOut.add(output1);
//            exOut.add(output2);

            Project project = ProjectModelUtil.createProject("ultrascan", "ultrascan", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId,
			        "ultrascan", "US3ExperimentTrestles", "US3AppTrestles",
			        "ultrascan_7ce6cd43-622c-44e0-87c5-fb7a6528c799", exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("trestles.sdsc.xsede.org_72b9181b-7156-4975-a386-ed98b4949496", 32, 1, 0, "shared", 30, 0);
            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
            
            scheduling.setResourceHostId("trestles.sdsc.xsede.org_72b9181b-7156-4975-a386-ed98b4949496");
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
        
  /*          AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
            dataHandling.setOutputDataDir("/home/sgg/chathuri/laptop_backup/airavata");
            userConfigurationData.setAdvanceOutputDataHandling(dataHandling);*/
        
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }
    public static String createUS3ExperimentForStampede (Airavata.Client client) throws AiravataSystemException, InvalidRequestException, AiravataClientException, TException  {
        try{
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("input");
            input.setType(DataType.URI);
            input.setValue("file:///home/sgg/chathuri/laptop_backup/airavata/ultrascan_input/hpcinput.tar");
            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("walltime");
            input1.setType(DataType.STRING);
            input1.setValue("-walltime=60");
            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("mgroupcount");
            input2.setType(DataType.STRING);
            input2.setValue("-mgroupcount=1");
            
            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("output");
            output.setType(DataType.URI);
            output.setValue("");
//            OutputDataObjectType output1 = new OutputDataObjectType();
//            output1.setName("stdout");
//            output1.setType(DataType.STDOUT);
//            output1.setValue("");
//            OutputDataObjectType output2 = new OutputDataObjectType();
//            output2.setName("stderr");
//            output2.setType(DataType.STDERR);
//            output2.setValue("");
            exOut.add(output);
//            exOut.add(output1);
//            exOut.add(output2);

            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId,
			        "ultrascan", "US3ExperimentStampede", "US3AppStampede",
			        "ultrascan_7ce6cd43-622c-44e0-87c5-fb7a6528c799", exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("stampede.tacc.xsede.org_e59e046f-e0e1-49c4-8475-2fab2e35d044", 32, 2, 0, "normal", 30, 0);

            scheduling.setResourceHostId("stampede.tacc.xsede.org_e59e046f-e0e1-49c4-8475-2fab2e35d044");
            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
           
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
        
/*            AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
            dataHandling.setOutputDataDir("/home/sgg/chathuri/laptop_backup/airavata");
            userConfigurationData.setAdvanceOutputDataHandling(dataHandling);*/
        
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }
    public static String createUS3ExperimentForLonestar (Airavata.Client client) throws AiravataSystemException, InvalidRequestException, AiravataClientException, TException  {
        try{
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("input");
            input.setType(DataType.URI);
            input.setValue("file:///home/airavata/input/hpcinput.tar");
            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("walltime");
            input1.setType(DataType.STRING);
            input1.setValue("-walltime=60");
            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("mgroupcount");
            input2.setType(DataType.STRING);
            input2.setValue("-mgroupcount=1");
            
            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("output");
            output.setType(DataType.URI);
            output.setValue("");
//            OutputDataObjectType output1 = new OutputDataObjectType();
//            output1.setName("stdout");
//            output1.setType(DataType.STDOUT);
//            output1.setValue("");
//            OutputDataObjectType output2 = new OutputDataObjectType();
//            output2.setName("stderr");
//            output2.setType(DataType.STDERR);
//            output2.setValue("");
            exOut.add(output);
//            exOut.add(output1);
//            exOut.add(output2);

//            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
//            String projectId = client.createProject(project);

	        ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY,
			        "ultrascan_41574ef5-b054-4d03-ab20-2cfe768d5096", "ultrascan", "US3ExperimentLonestar",
			        "US3AppLonestar", "ultrascan_e76ab5cf-79f6-44df-a244-10a734183fec", exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("lonestar.tacc.teragrid.org_2e0273bc-324b-419b-9786-38a360d44772", 12, 2, 0, "normal", 30, 0);

            scheduling.setResourceHostId("lonestar.tacc.teragrid.org_2e0273bc-324b-419b-9786-38a360d44772");
            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
           
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
        
/*            AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
            dataHandling.setOutputDataDir("/home/airavata/output/");
            userConfigurationData.setAdvanceOutputDataHandling(dataHandling);*/
        
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }
    public static String createUS3ExperimentForAlamo (Airavata.Client client) throws AiravataSystemException, InvalidRequestException, AiravataClientException, TException  {
        try{
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("input");
            input.setType(DataType.URI);
            input.setValue("file:///home/airavata/input/hpcinput.tar");
            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("walltime");
            input1.setType(DataType.STRING);
            input1.setValue("-walltime=60");
            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("mgroupcount");
            input2.setType(DataType.STRING);
            input2.setValue("-mgroupcount=1");
            
            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("output");
            output.setType(DataType.URI);
            output.setValue("");
//            OutputDataObjectType output1 = new OutputDataObjectType();
//            output1.setName("stdout");
//            output1.setType(DataType.STDOUT);
//            output1.setValue("");
//            OutputDataObjectType output2 = new OutputDataObjectType();
//            output2.setName("stderr");
//            output2.setType(DataType.STDERR);
//            output2.setValue("");
            exOut.add(output);
//            exOut.add(output1);
//            exOut.add(output2);

//            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
//            String projectId = client.createProject(project);

	        ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY,
			        "ultrascan_41574ef5-b054-4d03-ab20-2cfe768d5096", "ultrascan", "US3ExperimentStampede",
			        "US3AppStampede", "ultrascan_e76ab5cf-79f6-44df-a244-10a734183fec", exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("alamo.uthscsa.edu_7b6cf99a-af2e-4e8b-9eff-998a5ef60fe5", 4, 2, 0, "default", 30, 0);

            scheduling.setResourceHostId("alamo.uthscsa.edu_7b6cf99a-af2e-4e8b-9eff-998a5ef60fe5");
            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
           
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
        
/*            AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
            dataHandling.setOutputDataDir("/home/airavata/output/");
            userConfigurationData.setAdvanceOutputDataHandling(dataHandling);*/
        
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }
    public static String createExperimentForStampede(Airavata.Client client) throws TException  {
        try{
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("echo_input");
            input.setType(DataType.STRING);
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

            ExperimentModel simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY,projectId, "admin", "US3EchoExperimentStatus", "US3EchoStampede", "US3EchoStampede", exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceSchedulingModel scheduling =
                    ExperimentModelUtil.createComputationResourceScheduling("stampede.tacc.xsede.org", 1, 1, 1, "development", 0, 0);
            scheduling.setResourceHostId("gsissh-stampede");
            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(scheduling);
            simpleExperiment.setUserConfigurationData(userConfigurationData);
            return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static void launchExperiment (Airavata.Client client, String expId)
            throws TException{
        try {
            client.launchExperiment(new AuthzToken(""), expId, DEFAULT_GATEWAY);
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
        }catch (TException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new TException(e);
        }
    }
}

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

import org.apache.airavata.api.error.ExperimentNotFoundException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ClientSettings;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.api.error.AiravataClientException;
import org.apache.airavata.api.error.AiravataSystemException;
import org.apache.airavata.api.error.InvalidRequestException;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.DocumentCreator;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.util.ExperimentModelUtil;
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
    private static final String DEFAULT_USER = "defauly.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";

    public static void main(String[] args) {
        try {
            AiravataUtils.setExecutionAsClient();
            final Airavata.Client airavata = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + airavata.GetAPIVersion());
            addDescriptors();
            final String expId = createExperiment(airavata);
            System.out.println("Experiment ID : " + expId);
            launchExperiment(airavata, expId);
            System.out.println("Launched successfully");
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
            Thread monitor = (new Thread(){
                 public void run() {
                     Map<String, JobStatus> jobStatuses = null;
                     while (true) {
                         try {
                             jobStatuses = airavata.getJobStatuses(expId);
                             Set<String> strings = jobStatuses.keySet();
                             for (String key : strings) {
                                 JobStatus jobStatus = jobStatuses.get(key);
                                 if(jobStatus == null){
                                     return;
                                 }else {
                                     if (JobState.COMPLETE.equals(jobStatus.getJobState())) {
                                         System.out.println("Job completed Job ID: " + key);
                                         return;
                                     }else{
                                        System.out.println("Job ID:" + key + jobStatuses.get(key).getJobState().toString());
                                     }
                                 }
                             }
                             Thread.sleep(5000);
                         } catch (Exception e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
                     }
                 }
            });
//            monitor.start();
            try {
                monitor.join();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

//            Experiment experiment = airavata.getExperiment(expId);
//            System.out.println("retrieved exp id : " + experiment.getExperimentID());
        } catch (TException e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
            logger.error("Error while creating airavata API object", e.getMessage());
            e.printStackTrace();
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error while creating airavata API object", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addDescriptors() throws AiravataAPIInvocationException,ApplicationSettingsException  {
        try {
            DocumentCreator documentCreator = new DocumentCreator(getAiravataAPI());
            documentCreator.createEchoAppLocalHostDocs();
            documentCreator.createEchoAppGramDocs();
            documentCreator.createEchoAppGSISSHDocs();
        } catch (AiravataAPIInvocationException e) {
            logger.error("Unable to create airavata API", e.getMessage());
            throw new AiravataAPIInvocationException(e);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to create airavata API", e.getMessage());
            throw new ApplicationSettingsException(e.getMessage());
        }
    }

    private static AiravataAPI getAiravataAPI() throws AiravataAPIInvocationException, ApplicationSettingsException {
        AiravataAPI airavataAPI;
        try {
            String sysUser = ClientSettings.getSetting(DEFAULT_USER);
            String gateway = ClientSettings.getSetting(DEFAULT_GATEWAY);
            airavataAPI = AiravataAPIFactory.getAPI(gateway, sysUser);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Unable to create airavata API", e.getMessage());
            throw new AiravataAPIInvocationException(e);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to create airavata API", e.getMessage());
            throw new ApplicationSettingsException(e.getMessage());
        }
        return airavataAPI;
    }

    public static String createExperiment (Airavata.Client client) throws AiravataSystemException, InvalidRequestException, AiravataClientException, TException  {
        try{
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING.toString());
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING.toString());
            output.setValue("");
            exOut.add(output);

            Experiment simpleExperiment = ExperimentModelUtil.createSimpleExperiment("project1", "admin", "echoExperiment", "SimpleEcho2", "SimpleEcho2", exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("trestles.sdsc.edu", 1, 1, 1, "normal", 0, 0, 1, "sds128");
            scheduling.setResourceHostId("gsissh-trestles");
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
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static String createLocalExperiment (Airavata.Client client) throws AiravataSystemException, InvalidRequestException, AiravataClientException, TException  {
        try{
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("echo_input");
            input.setType(DataType.STRING.toString());
            input.setValue("echo_output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING.toString());
            output.setValue("");
            exOut.add(output);

            Experiment simpleExperiment = ExperimentModelUtil.createSimpleExperiment("project1", "admin", "echoExperiment", "Echo", "Echo", exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("localhost", 1, 1, 1, "normal", 0, 0, 1, "sds128");
            scheduling.setResourceHostId("localhost");
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
        }catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static void launchExperiment (Airavata.Client client, String expId)
            throws ExperimentNotFoundException, AiravataSystemException, InvalidRequestException,AiravataClientException, TException{
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
        }catch (TException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new TException(e);
        }
    }
}

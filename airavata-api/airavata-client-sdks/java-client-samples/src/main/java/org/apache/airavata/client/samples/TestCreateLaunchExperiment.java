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

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TestCreateLaunchExperiment {

    //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
    private final static Logger logger = LoggerFactory.getLogger(TestCreateLaunchExperiment.class);
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";

    public static void main(String[] args) {
        try {
            final Airavata.Client airavata = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + airavata.getAPIVersion());
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.DATE, -1);
            Long from = cal1.getTimeInMillis();
            Calendar cal2 = Calendar.getInstance();
            Long to = cal2.getTimeInMillis();

            List<JobDetails> jobDetailsList = getJobDetails(airavata, "echoExperiment_14a83cee-5611-4e37-b90c-4444d28359b6");
            for (JobDetails job : jobDetailsList){
//                System.out.println(experimentSummary.getExperimentID());
                System.out.println("job description : " + job.getJobDescription());
//                System.out.println(experimentSummary.getExperimentStatus().getExperimentState().toString());
            }

            List<DataTransferDetails> dataTransferDetails = getDataTransferDetails(airavata, "echoExperiment_6c32f45d-7755-4d1f-9915-69b9a6972770");
            for (DataTransferDetails dt : dataTransferDetails){
//                System.out.println(experimentSummary.getExperimentID());
                System.out.println("data transfer description "  + dt.getTransferDescription());
//                System.out.println(experimentSummary.getExperimentStatus().getExperimentState().toString());
            }
//            getAllComputeResources(airavata);
//            getAppModule(airavata, "amber_c476de64-ca5c-415a-94e9-b77fbe67b806");
//            getAVailableComputeResourcesForApp(airavata, "Amber_0cecdf39-1ce2-4d98-bc76-87447e10fd4d");
//            for (int i = 0; i < 10 ; i++){
//                long time = System.currentTimeMillis();
//                List<ExperimentSummary> experiments = getExperimentsForApplication(airavata, "ultrascan", "US3Application");
//                int count = i+1;
//                System.out.println("Experiment count : " + experiments.size());
//                System.out.println("iteration : " + String.valueOf(count));
//                System.out.println(System.currentTimeMillis() - time);
//            }
//            List<Experiment> experiments = getExperimentsForUser(airavata, "admin");
//            System.out.println("Experiment count : " + experiments.size());
//            for (Experiment ex : experiments){
//                System.out.println(ex.getExperimentID());
//            }

        } catch (Exception e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        }
    }

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

    public static List<Experiment> getExperimentsForUser (Airavata.Client client, String user){
        try {
            return client.getAllUserExperiments(user);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<JobDetails> getJobDetails (Airavata.Client client, String experimentId){
        try {
            return client.getJobDetails(experimentId);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<DataTransferDetails> getDataTransferDetails (Airavata.Client client, String experimentId){
        try {
            return client.getDataTransferDetails(experimentId);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExperimentSummary> getExperimentsForApplication (Airavata.Client client, String user, String application){
        try {
            return client.searchExperimentsByApplication(user, application);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExperimentSummary> searchExperimentsByStatus (Airavata.Client client, String user, ExperimentState experimentState){
        try {
            return client.searchExperimentsByStatus(user, experimentState);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExperimentSummary> searchExperimentsByDate (Airavata.Client client, String user, Long from, Long to){
        try {
            return client.searchExperimentsByCreationTime(user, from, to);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e){
            e.printStackTrace();
        }
        return null;
    }


    public static void getAllComputeResources (Airavata.Client client){
        try {
            Map<String, String> allComputeResourceNames = client.getAllComputeResourceNames();
            for (String id : allComputeResourceNames.keySet()){
                System.out.println("resource id : " + id);
                System.out.println("resource name : " + allComputeResourceNames.get(id));
            }
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void getAllAppInterfaces (Airavata.Client client){
        try {
            Map<String, String> allApps = client.getAllApplicationInterfaceNames();
            for (String id : allApps.keySet()){
                System.out.println("app id : " + id);
                System.out.println("app name : " + allApps.get(id));
            }
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void getAppModule (Airavata.Client client, String moduleId){
        try {
            ApplicationModule applicationModule = client.getApplicationModule(moduleId);
            if (applicationModule != null){
                System.out.println("module name :" + applicationModule.getAppModuleName());
                System.out.println("module version :" + applicationModule.getAppModuleVersion());
            }
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e) {
            e.printStackTrace();
        }
    }


    public static void getAVailableComputeResourcesForApp (Airavata.Client client, String applicationName){
        try {
            Map<String, String> resources = client.getAvailableAppInterfaceComputeResources(applicationName);
            for (Map.Entry<String, String> entry : resources.entrySet()) {
                System.out.println("resource id : = " + entry.getKey() + ", resource name : = " + entry.getValue());
            }
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        }catch (TException e) {
            e.printStackTrace();
        }
    }

}

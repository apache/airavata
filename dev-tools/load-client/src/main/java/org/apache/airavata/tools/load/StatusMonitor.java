/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.tools.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.JobState;
import org.apache.thrift.TException;

public class StatusMonitor {

    private String apiHost;
    private int apiPort;
    private String trustStorePath;
    private String trustStorePassword;
    private AuthzToken authzToken;

    public StatusMonitor(
            String apiHost, int apiPort, String trustStorePath, String trustStorePassword, AuthzToken authzToken)
            throws AiravataClientException {
        this.apiHost = apiHost;
        this.apiPort = apiPort;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.authzToken = authzToken;
    }

    public void monitorExperiments(List<String> experiments) throws TException {

        Map<String, JobModel> jobModelMap = new HashMap<>();
        Map<String, ExperimentModel> experimentModelMap = new HashMap<>();

        Airavata.Client airavataClient;
        long monitoringStartTime = System.currentTimeMillis();
        while (experiments.size() > jobModelMap.size()) {
            System.out.println("Running a monitoring round....");
            airavataClient = AiravataClientFactory.createAiravataSecureClient(
                    apiHost, apiPort, trustStorePath, trustStorePassword, 100000);

            for (String experiment : experiments) {

                try {
                    if (jobModelMap.containsKey(experiment)) {
                        continue;
                    }
                    List<JobModel> jobDetails = airavataClient.getJobDetails(authzToken, experiment);
                    if (jobDetails.size() > 0) {
                        jobModelMap.put(experiment, jobDetails.get(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error while monitoring experiment " + experiment);
                }
            }

            System.out.println("Jobs " + jobModelMap.size() + "/" + experiments.size() + " submitted");
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        airavataClient = AiravataClientFactory.createAiravataSecureClient(
                apiHost, apiPort, trustStorePath, trustStorePassword, 100000);

        for (String experiment : experiments) {
            experimentModelMap.put(experiment, airavataClient.getExperiment(authzToken, experiment));
        }

        long totalTime = 0;
        long totalExperiments = 0;

        System.out.println("EXP ID,CREATE_TIME,LAUNCHED_TIME,EXECUTING_TIME,JOB_SUBMIT_TIME");
        List<String> lines = new ArrayList<>();
        for (String experiment : experiments) {
            try {

                long expCreatedTime = experimentModelMap.get(experiment).getExperimentStatus().stream()
                        .filter(es -> es.getState() == ExperimentState.CREATED)
                        .findFirst()
                        .get()
                        .getTimeOfStateChange();

                long expLaunchedTime = experimentModelMap.get(experiment).getExperimentStatus().stream()
                        .filter(es -> es.getState() == ExperimentState.LAUNCHED)
                        .findFirst()
                        .get()
                        .getTimeOfStateChange();

                long expExecutedTime = experimentModelMap.get(experiment).getExperimentStatus().stream()
                        .filter(es -> es.getState() == ExperimentState.EXECUTING)
                        .findFirst()
                        .get()
                        .getTimeOfStateChange();

                long jobSubmittedTime = jobModelMap.get(experiment).getJobStatuses().stream()
                        .filter(st -> st.getJobState() == JobState.SUBMITTED)
                        .findFirst()
                        .get()
                        .getTimeOfStateChange();

                // long jobCompletedTime = jobModelMap.get(experiment)
                //        .getJobStatuses().stream().filter(st -> st.getJobState() == JobState.COMPLETE).findFirst()
                //        .get().getTimeOfStateChange();

                // long expCompletedTime = experimentModelMap.get(experiment)
                //        .getExperimentStatus().stream().filter(es -> es.getState() ==
                // ExperimentState.COMPLETED).findFirst()
                //        .get().getTimeOfStateChange();
                lines.add(experiment + "," + expCreatedTime + "," + expLaunchedTime + "," + expExecutedTime + ","
                        + jobSubmittedTime);
                totalTime += jobSubmittedTime - expExecutedTime;
                totalExperiments++;
            } catch (Exception e) {
                System.out.println("Error parsing " + experiment + ". Err " + e.getMessage());
                e.printStackTrace();
            }
        }
        long monitoringStopTime = System.currentTimeMillis();

        for (String line : lines) {
            System.out.println(line);
        }
        System.out.println("All jobs completed");
        System.out.println("Average time " + (totalTime * 1.0 / totalExperiments) / 1000 + " s");
        System.out.println("Time for monitoring " + (monitoringStopTime - monitoringStartTime) / 1000 + "s");
    }
}

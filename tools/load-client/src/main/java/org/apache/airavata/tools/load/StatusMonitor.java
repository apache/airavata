package org.apache.airavata.tools.load;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusMonitor {

    private String apiHost;
    private int apiPort;
    private String trustStorePath;
    private String trustStorePassword;
    private AuthzToken authzToken;

    public StatusMonitor(String apiHost, int apiPort, String trustStorePath, String trustStorePassword, AuthzToken authzToken) throws AiravataClientException {
        this.apiHost = apiHost;
        this.apiPort = apiPort;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.authzToken = authzToken;
    }

    public void monitorExperiments(List<String> experiments) throws TException {

        Airavata.Client airavataClient = AiravataClientFactory.createAiravataSecureClient(apiHost, apiPort, trustStorePath, trustStorePassword, 100000);

        Map<String, JobModel> jobModelMap = new HashMap<>();
        Map<String, ExperimentModel> experimentModelMap = new HashMap<>();


        while(experiments.size() > jobModelMap.size()) {
            System.out.println("Running a monitoring round....");

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
            try {
                Thread.sleep(20*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (String experiment : experiments) {
            experimentModelMap.put(experiment, airavataClient.getExperiment(authzToken, experiment));
        }

        long totalTime = 0;

        for (String experiment : experiments) {
            long jobSubmittedTime = jobModelMap.get(experiment)
                    .getJobStatuses().stream().filter(st -> st.getJobState() == JobState.SUBMITTED).findFirst()
                    .get().getTimeOfStateChange();
            long expExecutedTime = experimentModelMap.get(experiment)
                    .getExperimentStatus().stream().filter(es -> es.getState() == ExperimentState.EXECUTING).findFirst()
                    .get().getTimeOfStateChange();
            System.out.println("Experiment " + experiment + " executed " + expExecutedTime + " job submitted " + jobSubmittedTime);
            totalTime += jobSubmittedTime - expExecutedTime;
        }
        System.out.println("All jobs created");
        System.out.println("Average time " + (totalTime *1.0/experiments.size())/1000 + " s");
    }
}

package org.apache.airavata.tools.load;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.security.AuthzToken;
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

        for (String experiment : experiments) {
            experimentModelMap.put(experiment, airavataClient.getExperiment(authzToken, experiment));
        }

        while(experiments.size() > jobModelMap.size()) {
            for (String experiment : experiments) {
                if (jobModelMap.containsKey(experiment)) {
                    continue;
                }
                List<JobModel> jobDetails = airavataClient.getJobDetails(authzToken, experiment);
                if (jobDetails.size() > 0) {
                    jobModelMap.put(experiment, jobDetails.get(0));
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long totalTime = 0;

        for (String experiment : experiments) {
            totalTime += jobModelMap.get(experiment).getCreationTime() - experimentModelMap.get(experiment).getCreationTime();
        }
        System.out.println("All jobs created");
        System.out.println("Average time " + (totalTime *1.0/experiments.size())/1000 + " s");
    }
}

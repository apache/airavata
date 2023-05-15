package org.apache.airavata.apis.scheduling;

import org.apache.airavata.api.gateway.ExperimentLaunchRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MetaScheduler {
    private final ExecutorService schedulerPool = Executors.newFixedThreadPool(10);
    public void scheduleExperiment(ExperimentLaunchRequest request) {
        schedulerPool.submit(() -> scheduleAsync(request));
    }

    private void scheduleAsync(ExperimentLaunchRequest request) {
        // Persist state in zk or DB
        // Run meta scheduler logic
        // Submit to Helix
    }
}

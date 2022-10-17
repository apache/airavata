package org.apache.airavata.metascheduler.process.scheduling.api;

import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.model.process.ProcessModel;

import java.util.Optional;

/**
 * This class provides implementation of the ProcessSchedule Interface
 */
public  class ProcessSchedulerImpl implements ProcessScheduler {

    @Override
    public Optional<ProcessModel> schedule(String processId) {
        return Optional.empty();
    }


}

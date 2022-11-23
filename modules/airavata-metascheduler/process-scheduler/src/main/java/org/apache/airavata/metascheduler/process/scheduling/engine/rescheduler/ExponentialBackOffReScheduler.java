package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.model.process.ProcessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExponentialBackOffReScheduler implements ReScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackOffReScheduler.class);

    @Override
    public void reschedule(ProcessModel processModel) {

        LOGGER.info("Rescheduling process with Id " + processModel.getProcessId() + " experimentId " + processModel.getExperimentId());

    }

}

package org.apache.airavata.metascheduler.core.engine;

import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;

/**
 * This is the interface class for ReScheduling
 * algorithm.
 */
public interface ReScheduler {

    void reschedule(ProcessModel processModel, ProcessState processState);
}

package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

/**
 * This is the interface class for ReScheduling
 * algorithm.
 */
public interface ReScheduler {

    void reschedule(String processId);
}

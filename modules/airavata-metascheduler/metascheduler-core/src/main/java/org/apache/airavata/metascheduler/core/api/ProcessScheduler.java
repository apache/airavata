package org.apache.airavata.metascheduler.core.api;

import org.apache.airavata.model.process.ProcessModel;

import java.util.Optional;

/**
 * Provides interfaces for Process related scheduling operations
 */
public interface ProcessScheduler {


    /**
     * This method checks experiment can be instantly scheduled to a computer resource,
     * If it can be scheduled, Processes are updated with selected Scheduling resource otherwise all are
     *  moved to Queued state
     * @param experimentId
     * @return boolean
     */
      boolean  schedule(String experimentId);


    /**
     * This method can be used to reschedule a failed experiment.
     * If experiment can be scheduled instantly, Processes are updated with scheduling resources, otherwise
     * is moved to Queued state
     * @param experimentId
     * @return boolean
     */
    boolean  reschedule(String experimentId);

}

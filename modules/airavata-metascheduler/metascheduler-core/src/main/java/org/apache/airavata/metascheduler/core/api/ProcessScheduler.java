package org.apache.airavata.metascheduler.core.api;

import org.apache.airavata.model.process.ProcessModel;

import java.util.Optional;

/**
 * Provides interfaces for Process related scheduling operations
 */
public interface ProcessScheduler {


    /**
     * This method checks process can be instantly scheduled to a computer resource,
     * If it can be scheduled to a process, configured ProcessModel is returned otherwise Process
     * is moved to Queued state
     * @param processId
     * @return Optional<ProcessModel>
     */
    Optional<ProcessModel>  schedule(String processId);


    /**
     * This method can be used to reschedule a failed process.
     * If prcoess can be scheduled instantly, configured ProcessModel is returned otherwise Process
     * is moved to Queued state
     * @param processId
     * @return Optional<ProcessModel>
     */
    Optional<ProcessModel>  reschedule(String processId);

}

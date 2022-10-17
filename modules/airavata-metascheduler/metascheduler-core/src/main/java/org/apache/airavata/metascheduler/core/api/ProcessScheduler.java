package org.apache.airavata.metascheduler.core.api;

import org.apache.airavata.model.process.ProcessModel;

import java.util.Optional;

/**
 * Provides interfaces for Process related scheduling operations
 */
public interface ProcessScheduler {


    /**
     * This method checks process can be instantly schedule to a computer resource,
     * If it can be schedule to a process configured ProcessModel is returned otherwise Process
     * is moved to Queued state
     * @param processId
     * @return if instant scheduling is possible return ProcessModel  else null
     */
    Optional<ProcessModel>  schedule(String processId);

}

package org.apache.airavata.metascheduler.core.engine;

import org.quartz.Job;

/**
 * This class scans all queued processes and relaunch them based on
 * activated rescheduling algorithm
 */
public interface ProcessScanner extends Job {


}

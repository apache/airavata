package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

import org.quartz.Job;

/**
 * This class scans all queued processes and relaunch them based on
 * activated rescheduling algorithm
 */
public interface ProcessScanner extends Job {


}

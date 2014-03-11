/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.job.monitor.impl.pull.qstat;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.core.PullMonitor;
import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * This monitor is based on qstat command which can be run
 * in grid resources and retrieve the job status.
 */
public class QstatMonitor extends PullMonitor {
    private final static Logger logger = LoggerFactory.getLogger(QstatMonitor.class);

    // I think this should use DelayedBlocking Queue to do the monitoring*/
    private BlockingQueue<MonitorID> queue;

    private boolean startPulling = false;

    private Map<String, ResourceConnection> connections;

    private MonitorPublisher publisher;

    public QstatMonitor(){
        connections = new HashMap<String, ResourceConnection>();
    }
    public QstatMonitor(BlockingQueue<MonitorID> queue, MonitorPublisher publisher) {
        this.queue = queue;
        this.publisher = publisher;
        connections = new HashMap<String, ResourceConnection>();
    }

    public void run() {
        /* implement a logic to pick each monitorID object from the queue and do the
        monitoring
         */
        this.startPulling = true;
        while (this.startPulling || !ServerSettings.isStopAllThreads()) {
            try {
                startPulling();
                // After finishing one iteration of the full queue this thread sleeps 1 second
                Thread.sleep(5000);
            } catch (Exception e){
                // we catch all the exceptions here because no matter what happens we do not stop running this
                // thread, but ideally we should report proper error messages, but this is handled in startPulling
                // method, incase something happen in Thread.sleep we handle it with this catch block.
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * This method will can invoke when PullMonitor needs to start
     * and it has to invoke in the frequency specified below,
     *
     * @return if the start process is successful return true else false
     */
    public boolean startPulling() throws AiravataMonitorException {
        // take the top element in the queue and pull the data and put that element
        // at the tail of the queue
        MonitorID take = null;
        JobStatus jobStatus = new JobStatus();
        try {
                take = this.queue.take();
                if((take.getHost().getType() instanceof GsisshHostType)){
                    long monitorDiff = 0;
                    long startedDiff = 0;
                    if (take.getLastMonitored() != null) {
                        monitorDiff = (new Timestamp((new Date()).getTime())).getTime() - take.getLastMonitored().getTime();
                        startedDiff = (new Timestamp((new Date()).getTime())).getTime() - take.getJobStartedTime().getTime();
                        //todo implement an algorithm to delay the monitor based no start time, we have to delay monitoring
                        //todo  for long running jobs
//                    System.out.println(monitorDiff + "-" + startedDiff);
                        if ((monitorDiff / 1000) < 5) {
                            // its too early to monitor this job, so we put it at the tail of the queue
                            this.queue.put(take);
                        }
                    }
                    if (take.getLastMonitored() == null || ((monitorDiff / 1000) >= 5)) {
                        GsisshHostType gsisshHostType = (GsisshHostType) take.getHost().getType();
                        String hostName = gsisshHostType.getHostAddress();
                        ResourceConnection connection = null;
                        if (connections.containsKey(hostName)) {
                            logger.debug("We already have this connection so not going to create one");
                            connection = connections.get(hostName);
                        } else {
                            connection = new ResourceConnection(take, gsisshHostType.getInstalledPath());
                            connections.put(hostName, connection);
                        }
                        take.setStatus(connection.getJobStatus(take));
                        jobStatus.setMonitorID(take);
                        jobStatus.setState(take.getStatus());
                        // we have this JobStatus class to handle amqp monitoring
                        publisher.publish(jobStatus);
                        // if the job is completed we do not have to put the job to the queue again
                        if (!jobStatus.getState().equals(JobState.COMPLETE)) {
                            take.setLastMonitored(new Timestamp((new Date()).getTime()));
                            this.queue.put(take);
                        }
                    }
                } else {
                    logger.debug("Qstat Monitor doesn't handle non-gsissh hosts");
                }
            } catch (InterruptedException e) {
                if(!this.queue.contains(take)){
                    try {
                        this.queue.put(take);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                logger.error("Error handling the job with Job ID:" + take.getJobID());
                throw new AiravataMonitorException(e);
            } catch (SSHApiException e) {
                logger.error(e.getMessage());
                if(e.getMessage().contains("Unknown Job Id Error")){
                    // in this case job is finished or may be the given job ID is wrong
                    jobStatus.setState(JobState.UNKNOWN);
                    publisher.publish(jobStatus);
                }else if(e.getMessage().contains("illegally formed job identifier")){
                   logger.error("Wrong job ID is given so dropping the job from monitoring system");
                } else if (!this.queue.contains(take)) {   // we put the job back to the queue only if its state is not unknown
                    if (take.getFailedCount() < 2) {
                        try {
                            take.setFailedCount(take.getFailedCount() + 1);
                            this.queue.put(take);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        logger.error(e.getMessage());
                        logger.error("Tried to monitor the job 3 times, so dropping of the the Job with ID: " + take.getJobID());
                    }
                }
                throw new AiravataMonitorException("Error retrieving the job status", e);
            } catch (Exception e){
                if (take.getFailedCount() < 3) {
                    try {
                        take.setFailedCount(take.getFailedCount() + 1);
                        this.queue.put(take);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    logger.error(e.getMessage());
                    logger.error("Tryied to monitor the job 3 times, so dropping of the the Job with ID: " + take.getJobID());
                }
                throw new AiravataMonitorException("Error retrieving the job status", e);
            }



        return true;
    }


    /**
     * This is the method to stop the polling process
     *
     * @return if the stopping process is successful return true else false
     */
    public boolean stopPulling() {
        this.startPulling = false;
        return true;
    }

    public MonitorPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(MonitorPublisher publisher) {
        this.publisher = publisher;
    }

    public BlockingQueue<MonitorID> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<MonitorID> queue) {
        this.queue = queue;
    }

    public boolean authenticate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

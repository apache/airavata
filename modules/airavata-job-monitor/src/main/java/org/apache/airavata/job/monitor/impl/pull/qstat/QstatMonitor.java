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

import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.core.PullMonitor;
import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * This monitor is based on qstat command which can be run
 * in grid resources and retrieve the job status.
 */
public class QstatMonitor extends PullMonitor implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(QstatMonitor.class);

    // I think this should use DelayedBlocking Queue to do the monitoring*/
    private BlockingQueue<MonitorID> queue;

    private boolean startPulling = false;

    private Map<String, ResourceConnection> connections;

    private MonitorPublisher publisher;

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
        while (this.startPulling) {
            try {
                startPulling();
                // After finishing one iteration of the full queue this thread sleeps 1 second
                Thread.sleep(1000);
            } catch (AiravataMonitorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
        while (!this.queue.isEmpty()) {
            try {
                take = this.queue.take();
                long monitorDiff = 0;
                long startedDiff = 0;
                if (take.getLastMonitored() != null) {
                    monitorDiff = (new Timestamp((new Date()).getTime())).getTime() - take.getLastMonitored().getTime();
                    startedDiff = (new Timestamp((new Date()).getTime())).getTime() - take.getJobStartedTime().getTime();
//                    System.out.println(monitorDiff + "-" + startedDiff);
                    if ((monitorDiff / 1000) < 5) {
                        // its too early to monitor this job, so we put it at the tail of the queue
                        this.queue.put(take);
                    }
                }
                if(take.getLastMonitored() == null || ((monitorDiff/1000) >= 5)){
                        String hostName = take.getHost().getType().getHostAddress();
                        ResourceConnection connection = null;
                        if (connections.containsKey(hostName)) {
                            logger.debug("We already have this connection so not going to create one");
                            connection = connections.get(hostName);
                        } else {
                            connection = new ResourceConnection(take, "/opt/torque/bin");
                        }
                        jobStatus.setMonitorID(take);
                        jobStatus.setState(connection.getJobStatus(take));
                        publisher.publish(jobStatus);
                        // if the job is completed we do not have to put the job to the queue again
                        if (!jobStatus.getState().equals(JobState.COMPLETE)) {
                            take.setLastMonitored(new Timestamp((new Date()).getTime()));
                            this.queue.put(take);
                        }
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
                if(e.getMessage().contains("Unknown Job Id Error")){
                    // in this case job is finished or may be the given job ID is wrong
                    jobStatus.setState(JobState.UNKNOWN);
                    publisher.publish(jobStatus);
                }else if(e.getMessage().contains("illegally formed job identifier")){
                   logger.error("Wrong job ID is given so dropping the job from monitoring system");
                }
                else if(!this.queue.contains(take)){   // we put the job back to the queue only if its state is not unknown
                    try {
                        this.queue.put(take);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                logger.error("Error retrieving the job status");
                throw new AiravataMonitorException("Error retrieving the job status", e);
            }
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

    public boolean authenticate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

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
package org.apache.airavata.gfac.monitor.impl.pull.qstat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.core.monitor.TaskIdentity;
import org.apache.airavata.gfac.core.monitor.state.JobStatusChangeRequest;
import org.apache.airavata.gfac.core.monitor.state.TaskStatusChangeRequest;
import org.apache.airavata.gfac.monitor.HostMonitorData;
import org.apache.airavata.gfac.monitor.UserMonitorData;
import org.apache.airavata.gfac.monitor.core.PullMonitor;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.gfac.monitor.util.CommonUtils;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

/**
 * This monitor is based on qstat command which can be run
 * in grid resources and retrieve the job status.
 */
public class HPCPullMonitor extends PullMonitor {
    private final static Logger logger = LoggerFactory.getLogger(HPCPullMonitor.class);

    // I think this should use DelayedBlocking Queue to do the monitoring*/
    private BlockingQueue<UserMonitorData> queue;

    private boolean startPulling = false;

    private Map<String, ResourceConnection> connections;

    private MonitorPublisher publisher;


    private GFac gfac;

    private AuthenticationInfo authenticationInfo;

    public HPCPullMonitor() {
        connections = new HashMap<String, ResourceConnection>();
        this.queue = new LinkedBlockingDeque<UserMonitorData>();
        publisher = new MonitorPublisher(new EventBus());
    }

    public HPCPullMonitor(MonitorPublisher monitorPublisher, AuthenticationInfo authInfo) {
        connections = new HashMap<String, ResourceConnection>();
        this.queue = new LinkedBlockingDeque<UserMonitorData>();
        publisher = monitorPublisher;
        authenticationInfo = authInfo;
    }

    public HPCPullMonitor(BlockingQueue<UserMonitorData> queue, MonitorPublisher publisher) {
        this.queue = queue;
        this.publisher = publisher;
        connections = new HashMap<String, ResourceConnection>();
    }


    public void run() {
        /* implement a logic to pick each monitorID object from the queue and do the
        monitoring
         */
        this.startPulling = true;
        while (this.startPulling && !ServerSettings.isStopAllThreads()) {
            try {
                startPulling();
                // After finishing one iteration of the full queue this thread sleeps 1 second
                Thread.sleep(10000);
            } catch (Exception e) {
                // we catch all the exceptions here because no matter what happens we do not stop running this
                // thread, but ideally we should report proper error messages, but this is handled in startPulling
                // method, incase something happen in Thread.sleep we handle it with this catch block.
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
        // thread is going to return so we close all the connections
        Iterator<String> iterator = connections.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            ResourceConnection resourceConnection = connections.get(next);
            try {
                resourceConnection.getCluster().disconnect();
            } catch (SSHApiException e) {
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
        //todo this polling will not work with multiple usernames but with single user
        // and multiple hosts, currently monitoring will work
        UserMonitorData take = null;
        JobStatusChangeRequest jobStatus = new JobStatusChangeRequest();
        MonitorID currentMonitorID = null;
        HostDescription currentHostDescription = null;
        try {
            take = this.queue.take();
            List<MonitorID> completedJobs = new ArrayList<MonitorID>();
            List<HostMonitorData> hostMonitorData = take.getHostMonitorData();
            for (HostMonitorData iHostMonitorData : hostMonitorData) {
                if (iHostMonitorData.getHost().getType() instanceof GsisshHostType
                        || iHostMonitorData.getHost().getType() instanceof SSHHostType) {
                    currentHostDescription = iHostMonitorData.getHost();
                    String hostName =  iHostMonitorData.getHost().getType().getHostAddress();
                    ResourceConnection connection = null;
                    if (connections.containsKey(hostName)) {
                        logger.debug("We already have this connection so not going to create one");
                        connection = connections.get(hostName);
                    } else {
                        connection = new ResourceConnection(iHostMonitorData,getAuthenticationInfo());
                        connections.put(hostName, connection);
                    }
                    List<MonitorID> monitorID = iHostMonitorData.getMonitorIDs();
                    Map<String, JobState> jobStatuses = connection.getJobStatuses(monitorID);
                    for (MonitorID iMonitorID : monitorID) {
                        currentMonitorID = iMonitorID;
                        iMonitorID.setStatus(jobStatuses.get(iMonitorID.getJobID()+","+iMonitorID.getJobName()));    //IMPORTANT this is not a simple setter we have a logic
                        jobStatus = new JobStatusChangeRequest(iMonitorID);
                        // we have this JobStatus class to handle amqp monitoring

                        publisher.publish(jobStatus);
                        // if the job is completed we do not have to put the job to the queue again
                        iMonitorID.setLastMonitored(new Timestamp((new Date()).getTime()));

                        // After successful monitoring perform follow   ing actions to cleanup the queue, if necessary
                        if (jobStatus.getState().equals(JobState.COMPLETE)) {
                            completedJobs.add(iMonitorID);
                            try {
                                gfac.invokeOutFlowHandlers(iMonitorID.getJobExecutionContext());
                            } catch (GFacException e) {
                            	publisher.publish(new TaskStatusChangeRequest(new TaskIdentity(iMonitorID.getExperimentID(), iMonitorID.getWorkflowNodeID(),
										iMonitorID.getTaskID()), TaskState.FAILED));
                            	//FIXME this is a case where the output retrieving fails even if the job execution was a success. Thus updating the task status 
                            	//should be done understanding whole workflow of job submission and data transfer
//                            	publisher.publish(new ExperimentStatusChangedEvent(new ExperimentIdentity(iMonitorID.getExperimentID()),
//										ExperimentState.FAILED));
                                logger.info(e.getLocalizedMessage(), e);
                            }
                        } else if (iMonitorID.getFailedCount() > 2) {
                            logger.error("Tried to monitor the job with ID " + iMonitorID.getJobID() + " But failed 3 times, so skip this Job from Monitor");
                            iMonitorID.setLastMonitored(new Timestamp((new Date()).getTime()));
                            completedJobs.add(iMonitorID);
                            try {
                                logger.error("Launching outflow handlers to check output are genereated or not");
                                gfac.invokeOutFlowHandlers(iMonitorID.getJobExecutionContext());
                            } catch (GFacException e) {
                                publisher.publish(new TaskStatusChangeRequest(new TaskIdentity(iMonitorID.getExperimentID(), iMonitorID.getWorkflowNodeID(),
                                        iMonitorID.getTaskID()), TaskState.FAILED));
                                logger.info(e.getLocalizedMessage(), e);
                            }
                        } else {
                            // Evey
                            iMonitorID.setLastMonitored(new Timestamp((new Date()).getTime()));
                            // if the job is complete we remove it from the Map, if any of these maps
                            // get empty this userMonitorData will get delete from the queue
                        }
                    }
                } else {
                    logger.debug("Qstat Monitor doesn't handle non-gsissh hosts");
                }
            }
            // We have finished all the HostMonitorData object in userMonitorData, now we need to put it back
            // now the userMonitorData goes back to the tail of the queue
            queue.put(take);
            // cleaning up the completed jobs, this method will remove some of the userMonitorData from the queue if
            // they become empty
            for (MonitorID completedJob : completedJobs) {
                CommonUtils.removeMonitorFromQueue(queue, completedJob);
            }
        } catch (InterruptedException e) {
            if (!this.queue.contains(take)) {
                try {
                    this.queue.put(take);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            logger.error("Error handling the job with Job ID:" + currentMonitorID.getJobID());
            throw new AiravataMonitorException(e);
        } catch (SSHApiException e) {
            logger.error(e.getMessage());
            if (e.getMessage().contains("Unknown Job Id Error")) {
                // in this case job is finished or may be the given job ID is wrong
                jobStatus.setState(JobState.UNKNOWN);
                publisher.publish(jobStatus);
            } else if (e.getMessage().contains("illegally formed job identifier")) {
                logger.error("Wrong job ID is given so dropping the job from monitoring system");
            } else if (!this.queue.contains(take)) {   // we put the job back to the queue only if its state is not unknown
                if (currentMonitorID == null) {
                    logger.error("Monitoring the jobs failed, for user: " + take.getUserName()
                            + " in Host: " + currentHostDescription.getType().getHostAddress());
                } else {
                    if (currentMonitorID != null) {
                        if (currentMonitorID.getFailedCount() < 2) {
                            try {
                                currentMonitorID.setFailedCount(currentMonitorID.getFailedCount() + 1);
                                this.queue.put(take);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            logger.error(e.getMessage());
                            logger.error("Tried to monitor the job 3 times, so dropping of the the Job with ID: " + currentMonitorID.getJobID());
                        }
                    }
                }
            }
            throw new AiravataMonitorException("Error retrieving the job status", e);
        } catch (Exception e) {
            if (currentMonitorID != null) {
                if (currentMonitorID.getFailedCount() < 3) {
                    try {
                        currentMonitorID.setFailedCount(currentMonitorID.getFailedCount() + 1);
                        this.queue.put(take);
                        // if we get a wrong status we wait for a while and request again
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    logger.error(e.getMessage());
                    logger.error("Tryied to monitor the job 3 times, so dropping of the the Job with ID: " + currentMonitorID.getJobID());
                }
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

    public BlockingQueue<UserMonitorData> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<UserMonitorData> queue) {
        this.queue = queue;
    }

    public boolean authenticate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, ResourceConnection> getConnections() {
        return connections;
    }

    public boolean isStartPulling() {
        return startPulling;
    }

    public void setConnections(Map<String, ResourceConnection> connections) {
        this.connections = connections;
    }

    public void setStartPulling(boolean startPulling) {
        this.startPulling = startPulling;
    }

    public GFac getGfac() {
        return gfac;
    }

    public void setGfac(GFac gfac) {
        this.gfac = gfac;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }
}

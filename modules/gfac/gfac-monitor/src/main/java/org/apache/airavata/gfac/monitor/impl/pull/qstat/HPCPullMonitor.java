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

import com.google.common.eventbus.EventBus;
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.core.utils.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.utils.OutHandlerWorker;
import org.apache.airavata.gfac.monitor.HostMonitorData;
import org.apache.airavata.gfac.monitor.UserMonitorData;
import org.apache.airavata.gfac.monitor.core.PullMonitor;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.gfac.monitor.impl.push.amqp.SimpleJobFinishConsumer;
import org.apache.airavata.gfac.monitor.util.CommonUtils;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeRequestEvent;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.apache.zookeeper.ZooKeeper;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This monitor is based on qstat command which can be run
 * in grid resources and retrieve the job status.
 */
public class HPCPullMonitor extends PullMonitor {

    private final static AiravataLogger logger = AiravataLoggerFactory.getLogger(HPCPullMonitor.class);
    public static final int FAILED_COUNT = 1;

    // I think this should use DelayedBlocking Queue to do the monitoring*/
    private BlockingQueue<UserMonitorData> queue;

    private boolean startPulling = false;

    private Map<String, ResourceConnection> connections;

    private MonitorPublisher publisher;

    private LinkedBlockingQueue<String> cancelJobList;

    private List<String> completedJobsFromPush;

    private GFac gfac;

    private AuthenticationInfo authenticationInfo;

    public HPCPullMonitor() {
        connections = new HashMap<String, ResourceConnection>();
        queue = new LinkedBlockingDeque<UserMonitorData>();
        publisher = new MonitorPublisher(new EventBus());
        cancelJobList = new LinkedBlockingQueue<String>();
        completedJobsFromPush = new ArrayList<String>();
        (new SimpleJobFinishConsumer(this.completedJobsFromPush)).listen();
    }

    public HPCPullMonitor(MonitorPublisher monitorPublisher, AuthenticationInfo authInfo) {
        connections = new HashMap<String, ResourceConnection>();
        queue = new LinkedBlockingDeque<UserMonitorData>();
        publisher = monitorPublisher;
        authenticationInfo = authInfo;
        cancelJobList = new LinkedBlockingQueue<String>();
        this.completedJobsFromPush = new ArrayList<String>();
        (new SimpleJobFinishConsumer(this.completedJobsFromPush)).listen();
    }

    public HPCPullMonitor(BlockingQueue<UserMonitorData> queue, MonitorPublisher publisher) {
        this.queue = queue;
        this.publisher = publisher;
        connections = new HashMap<String, ResourceConnection>();
        cancelJobList = new LinkedBlockingQueue<String>();
        this.completedJobsFromPush = new ArrayList<String>();
        (new SimpleJobFinishConsumer(this.completedJobsFromPush)).listen();
    }


    public void run() {
        /* implement a logic to pick each monitorID object from the queue and do the
        monitoring
         */
        this.startPulling = true;
        while (this.startPulling && !ServerSettings.isStopAllThreads()) {
            try {
                // After finishing one iteration of the full queue this thread sleeps 1 second
                synchronized (this.queue) {
                    if (this.queue.size() > 0) {
                        startPulling();
                }
            }
                Thread.sleep(10000);
            } catch (Exception e) {
                // we catch all the exceptions here because no matter what happens we do not stop running this
                // thread, but ideally we should report proper error messages, but this is handled in startPulling
                // method, incase something happen in Thread.sleep we handle it with this catch block.
                logger.error(e.getMessage(),e);
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
        JobStatusChangeRequestEvent jobStatus = new JobStatusChangeRequestEvent();
        MonitorID currentMonitorID = null;
        HostDescription currentHostDescription = null;
        try {
            take = this.queue.take();
            Map<String,MonitorID> completedJobs = new HashMap<String,MonitorID>();
            List<HostMonitorData> hostMonitorData = take.getHostMonitorData();
            for (HostMonitorData iHostMonitorData : hostMonitorData) {
                if (iHostMonitorData.getHost().getType() instanceof GsisshHostType
                        || iHostMonitorData.getHost().getType() instanceof SSHHostType) {
                    currentHostDescription = iHostMonitorData.getHost();
                    String hostName =  iHostMonitorData.getHost().getType().getHostAddress();
                    ResourceConnection connection = null;
                    if (connections.containsKey(hostName)) {
                        if(!connections.get(hostName).isConnected()){
                            connection = new ResourceConnection(iHostMonitorData,getAuthenticationInfo());
                            connections.put(hostName, connection);
                        }else{
                            logger.debug("We already have this connection so not going to create one");
                            connection = connections.get(hostName);
                        }
                    } else {
                        connection = new ResourceConnection(iHostMonitorData,getAuthenticationInfo());
                        connections.put(hostName, connection);
                    }

                    // before we get the statuses, we check the cancel job list and remove them permanently
                    List<MonitorID> monitorID = iHostMonitorData.getMonitorIDs();
                    Iterator<String> iterator1 = cancelJobList.iterator();

                    for(MonitorID iMonitorID:monitorID){
                        while(iterator1.hasNext()) {
                            String cancelMId = iterator1.next();
                            if (cancelMId.equals(iMonitorID.getExperimentID() + "+" + iMonitorID.getTaskID())) {
                                iMonitorID.setStatus(JobState.CANCELED);
                                completedJobs.put(iMonitorID.getJobName(), iMonitorID);
                                iterator1.remove();
                                logger.debugId(cancelMId, "Found a match in cancel monitor queue, hence moved to the " +
                                                "completed job queue, experiment {}, task {} , job {}",
                                        iMonitorID.getExperimentID(), iMonitorID.getTaskID(), iMonitorID.getJobID());
                                break;
                            }
                        }
                        iterator1 = cancelJobList.iterator();
                    }
                    synchronized (completedJobsFromPush) {
                        ListIterator<String> iterator = completedJobsFromPush.listIterator();
                        for (MonitorID iMonitorID : monitorID) {
                            String completeId = null;
                            while (iterator.hasNext()) {
                                 completeId = iterator.next();
                                if (completeId.equals(iMonitorID.getUserName() + "," + iMonitorID.getJobName())) {
                                    logger.info("This job is finished because push notification came with <username,jobName> " + completeId);
                                    completedJobs.put(iMonitorID.getJobName(), iMonitorID);
                                    iMonitorID.setStatus(JobState.COMPLETE);
                                    iterator.remove();//we have to make this empty everytime we iterate, otherwise this list will accumulate and will lead to a memory leak
                                    logger.debugId(completeId, "Push notification updated job {} status to {}. " +
                                                    "experiment {} , task {}.", iMonitorID.getJobID(), JobState.COMPLETE.toString(),
                                            iMonitorID.getExperimentID(), iMonitorID.getTaskID());
                                    break;
                                }
                            }
                            iterator = completedJobsFromPush.listIterator();
                        }
                    }
                    Map<String, JobState> jobStatuses = connection.getJobStatuses(monitorID);
                    Iterator<MonitorID> iterator = monitorID.iterator();
                    while (iterator.hasNext()) {
                        MonitorID iMonitorID = iterator.next();
                        currentMonitorID = iMonitorID;
                        if (!JobState.CANCELED.equals(iMonitorID.getStatus())&&
                                !JobState.COMPLETE.equals(iMonitorID.getStatus())) {
                            iMonitorID.setStatus(jobStatuses.get(iMonitorID.getJobID() + "," + iMonitorID.getJobName()));    //IMPORTANT this is NOT a simple setter we have a logic
                        }else if(JobState.COMPLETE.equals(iMonitorID.getStatus())){
                            completedJobs.put(iMonitorID.getJobName(), iMonitorID);
                            logger.debugId(iMonitorID.getJobID(), "Moved job {} to completed jobs map, experiment {}, " +
                                    "task {}", iMonitorID.getJobID(), iMonitorID.getExperimentID(), iMonitorID.getTaskID());
                        }
                        jobStatus = new JobStatusChangeRequestEvent();
                        iMonitorID.setStatus(jobStatuses.get(iMonitorID.getJobID()+","+iMonitorID.getJobName()));    //IMPORTANT this is not a simple setter we have a logic

                        if (iMonitorID.getFailedCount() > FAILED_COUNT) {
                            iMonitorID.setLastMonitored(new Timestamp((new Date()).getTime()));
                            String outputDir = iMonitorID.getJobExecutionContext().getApplicationContext()
                                    .getApplicationDeploymentDescription().getType().getOutputDataDirectory();
                            List<String> stdOut = null;
                            try {
                                stdOut = connection.getCluster().listDirectory(outputDir); // check the outputs directory
                            } catch (SSHApiException e) {
                                if (e.getMessage().contains("No such file or directory")) {
                                    // this is because while we run output handler something failed and during exception
                                    // we store all the jobs in the monitor queue again
                                    logger.error("We know this  job is already attempted to run out-handlers");
                                    CommonUtils.removeMonitorFromQueue(queue, iMonitorID);
                                }
                            }
                            if (stdOut != null && stdOut.size() > 0 && !stdOut.get(0).isEmpty()) { // have to be careful with this
                                iMonitorID.setStatus(JobState.COMPLETE);
                                completedJobs.put(iMonitorID.getJobName(), iMonitorID);
                                logger.errorId(iMonitorID.getJobID(), "Job monitoring failed {} times, removed job {} from " +
                                                "monitor queue. Experiment {} , task {}", iMonitorID.getFailedCount(),
                                        iMonitorID.getExperimentID(), iMonitorID.getTaskID());
                            } else {
                                iMonitorID.setFailedCount(0);
                            }
                        } else {
                            // Evey
                            iMonitorID.setLastMonitored(new Timestamp((new Date()).getTime()));
                            // if the job is complete we remove it from the Map, if any of these maps
                            // get empty this userMonitorData will get delete from the queue
                        }
                        JobIdentifier jobIdentity = new JobIdentifier(iMonitorID.getJobID(),
                                iMonitorID.getTaskID(),
                                iMonitorID.getWorkflowNodeID(),
                                iMonitorID.getExperimentID(),
                                iMonitorID.getJobExecutionContext().getGatewayID());
                        jobStatus.setJobIdentity(jobIdentity);
                        jobStatus.setState(iMonitorID.getStatus());
                        // we have this JobStatus class to handle amqp monitoring

                        publisher.publish(jobStatus);
                        logger.debugId(jobStatus.getJobIdentity().getJobId(), "Published job status change request, " +
                                        "experiment {} , task {}", jobStatus.getJobIdentity().getExperimentId(),
                                jobStatus.getJobIdentity().getTaskId());
                        // if the job is completed we do not have to put the job to the queue again
                        iMonitorID.setLastMonitored(new Timestamp((new Date()).getTime()));
                    }
                } else {
                    logger.debug("Qstat Monitor doesn't handle non-gsissh hosts , host {}", iHostMonitorData.getHost()
                            .getType().getHostAddress());
                }
            }
            // We have finished all the HostMonitorData object in userMonitorData, now we need to put it back
            // now the userMonitorData goes back to the tail of the queue
            queue.put(take);
            // cleaning up the completed jobs, this method will remove some of the userMonitorData from the queue if
            // they become empty
            Map<String, Integer> jobRemoveCountMap = new HashMap<String, Integer>();
            ZooKeeper zk = null;
            Set<String> keys = completedJobs.keySet();
            for (String jobName: keys) {
                MonitorID completedJob = completedJobs.get(jobName);
                CommonUtils.removeMonitorFromQueue(queue, completedJob);
//                    gfac.invokeOutFlowHandlers(completedJob.getJobExecutionContext());
                  GFacThreadPoolExecutor.getFixedThreadPool().submit(new OutHandlerWorker(gfac, completedJob, publisher));
                if (zk == null) {
                    zk = completedJob.getJobExecutionContext().getZk();
                }
                String key = CommonUtils.getJobCountUpdatePath(completedJob);
                int i = 0;
                if (jobRemoveCountMap.containsKey(key)) {
                    i = Integer.valueOf(jobRemoveCountMap.get(key));
                }
                jobRemoveCountMap.put(key, ++i);
            }
            if (completedJobs.size() > 0) {
                // reduce completed job count from zookeeper
                CommonUtils.updateZkWithJobCount(zk, jobRemoveCountMap, false);
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
                JobIdentifier jobIdentifier = new JobIdentifier("UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN");
                if (currentMonitorID != null){
                    jobIdentifier.setExperimentId(currentMonitorID.getExperimentID());
                    jobIdentifier.setTaskId(currentMonitorID.getTaskID());
                    jobIdentifier.setWorkflowNodeId(currentMonitorID.getWorkflowNodeID());
                    jobIdentifier.setJobId(currentMonitorID.getJobID());
                    jobIdentifier.setGatewayId(currentMonitorID.getJobExecutionContext().getGatewayID());
                }
                jobStatus.setJobIdentity(jobIdentifier);
                publisher.publish(jobStatus);
            } else if (e.getMessage().contains("illegally formed job identifier")) {
                logger.error("Wrong job ID is given so dropping the job from monitoring system");
            } else if (!this.queue.contains(take)) {
                try {
                    queue.put(take);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            throw new AiravataMonitorException("Error retrieving the job status", e);
        } catch (Exception e) {
            try {
                queue.put(take);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
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

    public LinkedBlockingQueue<String> getCancelJobList() {
        return cancelJobList;
    }

    public void setCancelJobList(LinkedBlockingQueue<String> cancelJobList) {
        this.cancelJobList = cancelJobList;
    }
}

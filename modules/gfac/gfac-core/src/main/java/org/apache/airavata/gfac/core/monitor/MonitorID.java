/**
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
 */
package org.apache.airavata.gfac.core.monitor;

import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.status.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/*
This is the object which contains the data to identify a particular
Job to start the monitoring
*/
public class MonitorID {
    private final static Logger logger = LoggerFactory.getLogger(MonitorID.class);

    private String userName;

    private Timestamp jobStartedTime;

    private Timestamp lastMonitored;

    private ComputeResourceDescription computeResourceDescription;

    private Map<String, Object> parameters;

    private String experimentID;

    private String workflowNodeID;

    private String taskID;

    private String jobID;

    private String jobName;

    private int failedCount = 0;

    private JobState state;

    private ProcessContext processContext;

    public MonitorID() {
    }
    public MonitorID(MonitorID monitorID){
        this.computeResourceDescription = monitorID.getComputeResourceDescription();
        this.jobStartedTime = new Timestamp((new Date()).getTime());
        this.userName = monitorID.getUserName();
        this.jobID = monitorID.getJobID();
        this.taskID = monitorID.getTaskID();
        this.experimentID = monitorID.getExperimentID();
        this.workflowNodeID = monitorID.getWorkflowNodeID();
        this.jobName = monitorID.getJobName();
    }
    public MonitorID(ComputeResourceDescription computeResourceDescription, String jobID, String taskID, String workflowNodeID, String experimentID, String userName,String jobName) {
        this.computeResourceDescription = computeResourceDescription;
        this.jobStartedTime = new Timestamp((new Date()).getTime());
        this.userName = userName;
        this.jobID = jobID;
        this.taskID = taskID;
        this.experimentID = experimentID;
        this.workflowNodeID = workflowNodeID;
        this.jobName = jobName;
    }

    public MonitorID(ProcessContext processContext) {
/*        this.processContext = processContext;
        this.computeResourceDescription = processContext.getApplicationContext().getComputeResourceDescription();
        userName = processContext.getExperiment().getUserName();
        taskID = processContext.getTaskData().getTaskID();
        experimentID = processContext.getExperiment().getExperimentID();
        workflowNodeID = processContext.getWorkflowNodeDetails().getNodeInstanceId();// at this point we only have one node todo: fix this
        try {
            jobName = processContext.getJobDetails().getJobName();
            jobID = processContext.getJobDetails().getJobID();
        }catch(NullPointerException e){
            logger.error("There is not job created at this point");
            // this is not a big deal we create MonitorId before having a jobId or job Name
        }*/
    }

    public ComputeResourceDescription getComputeResourceDescription() {
        return computeResourceDescription;
    }

    public void setComputeResourceDescription(ComputeResourceDescription computeResourceDescription) {
        this.computeResourceDescription = computeResourceDescription;
    }

    public Timestamp getLastMonitored() {
        return lastMonitored;
    }

    public void setLastMonitored(Timestamp lastMonitored) {
        this.lastMonitored = lastMonitored;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public Timestamp getJobStartedTime() {
        return jobStartedTime;
    }

    public void setJobStartedTime(Timestamp jobStartedTime) {
        this.jobStartedTime = jobStartedTime;
    }

    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return this.parameters.get(key);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getExperimentID() {
        return experimentID;
    }

    public void setExperimentID(String experimentID) {
        this.experimentID = experimentID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public JobState getStatus() {
        return state;
    }

    public void setStatus(JobState status) {
        // this logic is going to be useful for fast finishing jobs
        // because in some machines job state vanishes quicckly when the job is done
        // during that case job state comes as unknown.so we handle it here.
        if (this.state != null && status.equals(JobState.UNKNOWN)) {
            this.failedCount++;
            logger.info(this.getJobID(), "{} status came for job {}, Increasing the failed count to: {}.",
                    status.toString(), this.jobID, this.failedCount);
        }else {
            // normal scenario
            logger.info(this.getJobID(), "Valid status {} came for job {}, resetting fail count to 0", status.toString(), this.jobID);
            setFailedCount(0);
            this.state = status;
        }
    }

    public String getWorkflowNodeID() {
        return workflowNodeID;
    }

    public void setWorkflowNodeID(String workflowNodeID) {
        this.workflowNodeID = workflowNodeID;
    }

    public ProcessContext getProcessContext() {
        return processContext;
    }

    public void setProcessContext(ProcessContext processContext) {
        this.processContext = processContext;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}

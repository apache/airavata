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
package org.apache.airavata.gfac.monitor;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
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

    private HostDescription host;

    private AuthenticationInfo authenticationInfo = null;

    private Map<String, Object> parameters;

    private String experimentID;

    private String workflowNodeID;

    private String taskID;

    private String jobID;

    private int failedCount = 0;

    private JobState state;

    private JobExecutionContext jobExecutionContext;

    public MonitorID(HostDescription host, String jobID,String taskID, String workflowNodeID, String experimentID, String userName) {
        this.host = host;
        this.jobStartedTime = new Timestamp((new Date()).getTime());
        this.userName = userName;
        this.jobID = jobID;
        this.taskID = taskID;
        this.experimentID = experimentID;
        this.workflowNodeID = workflowNodeID;
    }

    public MonitorID(AuthenticationInfo authenticationInfo, JobExecutionContext jobExecutionContext) {
        this.authenticationInfo = authenticationInfo;
        this.jobExecutionContext = jobExecutionContext;
        host = jobExecutionContext.getApplicationContext().getHostDescription();
        userName = jobExecutionContext.getExperiment().getUserName();
        jobID = jobExecutionContext.getJobDetails().getJobID();
        taskID = jobExecutionContext.getTaskData().getTaskID();
        experimentID = jobExecutionContext.getExperiment().getExperimentID();
        workflowNodeID =  jobExecutionContext.getExperiment().getWorkflowNodeDetailsList().get(0).getNodeInstanceId();// at this point we only have one node todo: fix this

    }

    public MonitorID(HostDescription host, String jobID,String taskID, String workflowNodeID, String experimentID, String userName,AuthenticationInfo authenticationInfo) {
        this.host = host;
        this.jobStartedTime = new Timestamp((new Date()).getTime());
        this.authenticationInfo = authenticationInfo;
        this.userName = userName;
        // if we give myproxyauthenticationInfo, so we try to use myproxy user as the user
        if(this.authenticationInfo != null){
            if(this.authenticationInfo instanceof MyProxyAuthenticationInfo){
                this.userName = ((MyProxyAuthenticationInfo)this.authenticationInfo).getUserName();
            }
        }
        this.workflowNodeID = workflowNodeID;
        this.jobID = jobID;
        this.taskID = taskID;
        this.experimentID = experimentID;
    }
    public HostDescription getHost() {
        return host;
    }

    public void setHost(HostDescription host) {
        this.host = host;
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

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }

    public void addParameter(String key,Object value) {
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
                if (getFailedCount() > 2) {
                    switch (this.state) {
                        case ACTIVE:
                            this.state = JobState.COMPLETE;
                            break;
                        case QUEUED:
                            this.state = JobState.COMPLETE;
                            break;
                    }
                } else {
                    try {
                        // when state becomes unknown we sleep for a while
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    setFailedCount(getFailedCount() + 1);
                }
            } else {
                // normal scenario
                this.state = status;
            }
    }

	public String getWorkflowNodeID() {
		return workflowNodeID;
	}

	public void setWorkflowNodeID(String workflowNodeID) {
		this.workflowNodeID = workflowNodeID;
	}

    public JobExecutionContext getJobExecutionContext() {
        return jobExecutionContext;
    }

    public void setJobExecutionContext(JobExecutionContext jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
    }

    //	public String getWorkflowNodeID() {
//		return workflowNodeID;
//	}
//
//	public void setWorkflowNodeID(String workflowNodeID) {
//		this.workflowNodeID = workflowNodeID;
//	}
}

///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.gfac.core.monitor.state;
//
//import org.apache.airavata.common.utils.listener.AbstractStateChangeRequest;
//import org.apache.airavata.gfac.core.monitor.JobIdentity;
//import org.apache.airavata.gfac.core.monitor.MonitorID;
//import org.apache.airavata.model.workspace.experiment.JobState;
//
///**
// * This is the primary job state object used in
// * through out the monitor module. This use airavata-data-model JobState enum
// * Ideally after processing each event or monitoring message from remote system
// * Each monitoring implementation has to return this object with a state and
// * the monitoring ID
// */
//public class JobStatusChangedEvent  extends AbstractStateChangeRequest {
//    private JobState state;
//    private JobIdentity identity;
//
//    private MonitorID monitorID;
//
//    // this constructor can be used in Qstat monitor to handle errors
//    public JobStatusChangedEvent() {
//    }
//
//    public JobStatusChangedEvent(MonitorID monitorID) {
//        setIdentity(new JobIdentity(monitorID.getExperimentId(),monitorID.getWorkflowNodeID(),
//                monitorID.getTaskId(),monitorID.getJobId()));
//    	setMonitorID(monitorID);
//    	this.state = monitorID.getStatus();
//    }
//    public JobStatusChangedEvent(MonitorID monitorID, JobIdentity jobId, JobState state) {
//    	setIdentity(jobId);
//    	setMonitorID(monitorID);
//    	this.state = state;
//    }
//
//    public JobState getState() {
//        return state;
//    }
//
//    public void setState(JobState state) {
//       this.state = state;
//    }
//
//	public JobIdentity getIdentity() {
//		return identity;
//	}
//
//	public void setIdentity(JobIdentity identity) {
//		this.identity = identity;
//	}
//
//	public MonitorID getMonitorID() {
//		return monitorID;
//	}
//
//	public void setMonitorID(MonitorID monitorID) {
//		this.monitorID = monitorID;
//	}
//
//}

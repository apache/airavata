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
package org.apache.airavata.gfac.core.monitor.state;

import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.model.messaging.event.JobIdentifier;

public class GfacExperimentStateChangeRequest {
    private GfacExperimentState state;

    private JobIdentifier identity;

    private MonitorID monitorID;

    public GfacExperimentStateChangeRequest(MonitorID monitorID, GfacExperimentState state) {
        setIdentity(new JobIdentifier(monitorID.getJobId(),
                monitorID.getTaskId(),
                monitorID.getWorkflowNodeID(),
                monitorID.getExperimentId(),
                monitorID.getJobExecutionContext().getGatewayID()));
        setMonitorID(monitorID);
        this.state = state;
    }

    public GfacExperimentStateChangeRequest(MonitorID monitorID, JobIdentifier jobId, GfacExperimentState state) {
        setIdentity(jobId);
        setMonitorID(monitorID);
        this.state = state;
    }


    public GfacExperimentState getState() {
        return state;
    }

    public void setState(GfacExperimentState state) {
        this.state = state;
    }

    public JobIdentifier getIdentity() {
        return identity;
    }

    public void setIdentity(JobIdentifier identity) {
        this.identity = identity;
    }

    public MonitorID getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(MonitorID monitorID) {
        this.monitorID = monitorID;
    }
}

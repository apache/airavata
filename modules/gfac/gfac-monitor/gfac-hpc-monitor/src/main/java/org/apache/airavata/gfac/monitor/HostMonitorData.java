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

import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;

import java.util.ArrayList;
import java.util.List;

public class HostMonitorData {
//    private HostDescription host;
    private ComputeResourceDescription computeResourceDescription;
    private JobSubmissionProtocol jobSubmissionProtocol;
    private DataMovementProtocol dataMovementProtocol;

    private List<MonitorID> monitorIDs;

    public HostMonitorData(JobExecutionContext jobExecutionContext) {
        this.computeResourceDescription = jobExecutionContext.getApplicationContext().getComputeResourceDescription();
        this.jobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
        this.dataMovementProtocol = jobExecutionContext.getPreferredDataMovementProtocol();
        this.monitorIDs = new ArrayList<MonitorID>();
    }

    public HostMonitorData(JobExecutionContext jobExecutionContext, List<MonitorID> monitorIDs) {
        this.computeResourceDescription = jobExecutionContext.getApplicationContext().getComputeResourceDescription();
        this.jobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
        this.dataMovementProtocol = jobExecutionContext.getPreferredDataMovementProtocol();
        this.monitorIDs = monitorIDs;
    }

    public ComputeResourceDescription getComputeResourceDescription() {
        return computeResourceDescription;
    }

    public void setComputeResourceDescription(ComputeResourceDescription computeResourceDescription) {
        this.computeResourceDescription = computeResourceDescription;
    }

    public List<MonitorID> getMonitorIDs() {
        return monitorIDs;
    }

    public void setMonitorIDs(List<MonitorID> monitorIDs) {
        this.monitorIDs = monitorIDs;
    }

    /**
     * this method get called by CommonUtils and it will check the right place before adding
     * so there will not be a mismatch between this.host and monitorID.host
     * @param monitorID
     * @throws org.apache.airavata.gfac.monitor.exception.AiravataMonitorException
     */
    public void addMonitorIDForHost(MonitorID monitorID)throws AiravataMonitorException {
        monitorIDs.add(monitorID);
    }

    public JobSubmissionProtocol getJobSubmissionProtocol() {
        return jobSubmissionProtocol;
    }

    public DataMovementProtocol getDataMovementProtocol() {
        return dataMovementProtocol;
    }
}

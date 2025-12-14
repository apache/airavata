/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: SSHJobSubmission
 */
public class SSHJobSubmission {
    private String jobSubmissionInterfaceId;
    private SecurityProtocol securityProtocol;
    private ResourceJobManager resourceJobManager;
    private String alternativeSSHHostName;
    private int sshPort;
    private MonitorMode monitorMode;
    private List<String> batchQueueEmailSenders;

    public SSHJobSubmission() {}

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }

    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public ResourceJobManager getResourceJobManager() {
        return resourceJobManager;
    }

    public void setResourceJobManager(ResourceJobManager resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
    }

    public String getAlternativeSSHHostName() {
        return alternativeSSHHostName;
    }

    public void setAlternativeSSHHostName(String alternativeSSHHostName) {
        this.alternativeSSHHostName = alternativeSSHHostName;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public MonitorMode getMonitorMode() {
        return monitorMode;
    }

    public void setMonitorMode(MonitorMode monitorMode) {
        this.monitorMode = monitorMode;
    }

    public List<String> getBatchQueueEmailSenders() {
        return batchQueueEmailSenders;
    }

    public void setBatchQueueEmailSenders(List<String> batchQueueEmailSenders) {
        this.batchQueueEmailSenders = batchQueueEmailSenders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSHJobSubmission that = (SSHJobSubmission) o;
        return Objects.equals(jobSubmissionInterfaceId, that.jobSubmissionInterfaceId)
                && Objects.equals(securityProtocol, that.securityProtocol)
                && Objects.equals(resourceJobManager, that.resourceJobManager)
                && Objects.equals(alternativeSSHHostName, that.alternativeSSHHostName)
                && Objects.equals(sshPort, that.sshPort)
                && Objects.equals(monitorMode, that.monitorMode)
                && Objects.equals(batchQueueEmailSenders, that.batchQueueEmailSenders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                jobSubmissionInterfaceId,
                securityProtocol,
                resourceJobManager,
                alternativeSSHHostName,
                sshPort,
                monitorMode,
                batchQueueEmailSenders);
    }

    @Override
    public String toString() {
        return "SSHJobSubmission{" + "jobSubmissionInterfaceId=" + jobSubmissionInterfaceId + ", securityProtocol="
                + securityProtocol + ", resourceJobManager=" + resourceJobManager + ", alternativeSSHHostName="
                + alternativeSSHHostName + ", sshPort=" + sshPort + ", monitorMode=" + monitorMode
                + ", batchQueueEmailSenders=" + batchQueueEmailSenders + "}";
    }
}

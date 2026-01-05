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
 * Domain model: SlurmComputeResourcePreference
 */
public class SlurmComputeResourcePreference {
    private String allocationProjectNumber;
    private String preferredBatchQueue;
    private String qualityOfService;
    private String usageReportingGatewayId;
    private String sshAccountProvisioner;
    private List<GroupAccountSSHProvisionerConfig> groupSSHAccountProvisionerConfigs;
    private String sshAccountProvisionerAdditionalInfo;
    private List<ComputeResourceReservation> reservations;

    public SlurmComputeResourcePreference() {}

    public String getAllocationProjectNumber() {
        return allocationProjectNumber;
    }

    public void setAllocationProjectNumber(String allocationProjectNumber) {
        this.allocationProjectNumber = allocationProjectNumber;
    }

    public String getPreferredBatchQueue() {
        return preferredBatchQueue;
    }

    public void setPreferredBatchQueue(String preferredBatchQueue) {
        this.preferredBatchQueue = preferredBatchQueue;
    }

    public String getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(String qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public String getUsageReportingGatewayId() {
        return usageReportingGatewayId;
    }

    public void setUsageReportingGatewayId(String usageReportingGatewayId) {
        this.usageReportingGatewayId = usageReportingGatewayId;
    }

    public String getSshAccountProvisioner() {
        return sshAccountProvisioner;
    }

    public void setSshAccountProvisioner(String sshAccountProvisioner) {
        this.sshAccountProvisioner = sshAccountProvisioner;
    }

    public List<GroupAccountSSHProvisionerConfig> getGroupSSHAccountProvisionerConfigs() {
        return groupSSHAccountProvisionerConfigs;
    }

    public void setGroupSSHAccountProvisionerConfigs(
            List<GroupAccountSSHProvisionerConfig> groupSSHAccountProvisionerConfigs) {
        this.groupSSHAccountProvisionerConfigs = groupSSHAccountProvisionerConfigs;
    }

    public String getSshAccountProvisionerAdditionalInfo() {
        return sshAccountProvisionerAdditionalInfo;
    }

    public void setSshAccountProvisionerAdditionalInfo(String sshAccountProvisionerAdditionalInfo) {
        this.sshAccountProvisionerAdditionalInfo = sshAccountProvisionerAdditionalInfo;
    }

    public List<ComputeResourceReservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<ComputeResourceReservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlurmComputeResourcePreference that = (SlurmComputeResourcePreference) o;
        return Objects.equals(allocationProjectNumber, that.allocationProjectNumber)
                && Objects.equals(preferredBatchQueue, that.preferredBatchQueue)
                && Objects.equals(qualityOfService, that.qualityOfService)
                && Objects.equals(usageReportingGatewayId, that.usageReportingGatewayId)
                && Objects.equals(sshAccountProvisioner, that.sshAccountProvisioner)
                && Objects.equals(groupSSHAccountProvisionerConfigs, that.groupSSHAccountProvisionerConfigs)
                && Objects.equals(sshAccountProvisionerAdditionalInfo, that.sshAccountProvisionerAdditionalInfo)
                && Objects.equals(reservations, that.reservations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                allocationProjectNumber,
                preferredBatchQueue,
                qualityOfService,
                usageReportingGatewayId,
                sshAccountProvisioner,
                groupSSHAccountProvisionerConfigs,
                sshAccountProvisionerAdditionalInfo,
                reservations);
    }

    @Override
    public String toString() {
        return "SlurmComputeResourcePreference{" + "allocationProjectNumber=" + allocationProjectNumber
                + ", preferredBatchQueue=" + preferredBatchQueue + ", qualityOfService=" + qualityOfService
                + ", usageReportingGatewayId=" + usageReportingGatewayId + ", sshAccountProvisioner="
                + sshAccountProvisioner + ", groupSSHAccountProvisionerConfigs=" + groupSSHAccountProvisionerConfigs
                + ", sshAccountProvisionerAdditionalInfo=" + sshAccountProvisionerAdditionalInfo + ", reservations="
                + reservations + "}";
    }
}

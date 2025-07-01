/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.entities.appcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumns;
import jakarta.persistence.Table;

import java.util.List;

/**
 * The persistent class for the slurm_group_compute_resource_preference database table.
 */
@Entity
@DiscriminatorValue("SLURM")
@Table(name = "SLURM_GROUP_COMPUTE_RESOURCE_PREFERENCE")
@PrimaryKeyJoinColumns({
        @PrimaryKeyJoinColumn(name = "RESOURCE_ID", referencedColumnName = "RESOURCE_ID"),
        @PrimaryKeyJoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", referencedColumnName = "GROUP_RESOURCE_PROFILE_ID")
})
public class SlurmGroupComputeResourcePrefEntity extends GroupComputeResourcePrefEntity {

    @Column(name = "ALLOCATION_PROJECT_NUMBER")
    private String allocationProjectNumber;

    @Column(name = "PREFERED_BATCH_QUEUE")
    private String preferredBatchQueue;

    @Column(name = "QUALITY_OF_SERVICE")
    private String qualityOfService;

    @Column(name = "USAGE_REPORTING_GATEWAY_ID")
    private String usageReportingGatewayId;

    @Column(name = "SSH_ACCOUNT_PROVISIONER")
    private String sshAccountProvisioner;

    @Column(name = "SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO")
    private String sshAccountProvisionerAdditionalInfo;

    @OneToMany(
            targetEntity = GroupSSHAccountProvisionerConfig.class,
            mappedBy = "groupComputeResourcePref",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private List<GroupSSHAccountProvisionerConfig> groupSSHAccountProvisionerConfigs;

    @OneToMany(
            targetEntity = ComputeResourceReservationEntity.class,
            mappedBy = "groupComputeResourcePref",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true)
    @OrderBy("startTime ASC")
    private List<ComputeResourceReservationEntity> reservations;

    public SlurmGroupComputeResourcePrefEntity() {
    }

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

    public String getSshAccountProvisionerAdditionalInfo() {
        return sshAccountProvisionerAdditionalInfo;
    }

    public void setSshAccountProvisionerAdditionalInfo(String sshAccountProvisionerAdditionalInfo) {
        this.sshAccountProvisionerAdditionalInfo = sshAccountProvisionerAdditionalInfo;
    }

    public List<GroupSSHAccountProvisionerConfig> getGroupSSHAccountProvisionerConfigs() {
        return groupSSHAccountProvisionerConfigs;
    }

    public void setGroupSSHAccountProvisionerConfigs(
            List<GroupSSHAccountProvisionerConfig> groupSSHAccountProvisionerConfigs) {
        this.groupSSHAccountProvisionerConfigs = groupSSHAccountProvisionerConfigs;
    }

    public List<ComputeResourceReservationEntity> getReservations() {
        return reservations;
    }

    public void setReservations(List<ComputeResourceReservationEntity> reservations) {
        this.reservations = reservations;
    }
}

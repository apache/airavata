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
package org.apache.airavata.registry.core.entities.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.data.movement.DataMovementProtocol;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the compute_resource_preference database table.
 */
@Entity
@Table(name = "COMPUTE_RESOURCE_PREFERENCE")
@IdClass(ComputeResourcePreferencePK.class)
public class ComputeResourcePreferenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "GATEWAY_ID")
    @Id
    private String gatewayId;

    @Column(name = "RESOURCE_ID")
    @Id
    private String computeResourceId;

    @Column(name = "ALLOCATION_PROJECT_NUMBER")
    private String allocationProjectNumber;

    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;

    @Column(name = "OVERRIDE_BY_AIRAVATA")
    private boolean overridebyAiravata;

    @Column(name = "PREFERED_BATCH_QUEUE")
    private String preferredBatchQueue;

    @Column(name = "PREFERED_DATA_MOVE_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private DataMovementProtocol preferredDataMovementProtocol;

    @Column(name = "PREFERED_JOB_SUB_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private JobSubmissionProtocol preferredJobSubmissionProtocol;

    @Column(name = "QUALITY_OF_SERVICE")
    private String qualityOfService;

    @Column(name = "RESERVATION")
    private String reservation;

    @Column(name = "RESERVATION_END_TIME")
    private Timestamp reservationEndTime;

    @Column(name = "RESERVATION_START_TIME")
    private Timestamp reservationStartTime;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialStoreToken;

    @Column(name = "SCRATCH_LOCATION")
    private String scratchLocation;

    @Column(name = "USAGE_REPORTING_GATEWAY_ID")
    private String usageReportingGatewayId;

    @Column(name = "SSH_ACCOUNT_PROVISIONER")
    private String sshAccountProvisioner;

    @Column(name = "SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO")
    private String sshAccountProvisionerAdditionalInfo;

    @OneToMany(targetEntity = SSHAccountProvisionerConfiguration.class, mappedBy = "computeResourcePreference", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<SSHAccountProvisionerConfiguration> sshAccountProvisionerConfigurations;

    @ManyToOne(targetEntity = GatewayProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_ID")
    private GatewayProfileEntity gatewayProfileResource;

    public ComputeResourcePreferenceEntity() {
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getAllocationProjectNumber() {
        return allocationProjectNumber;
    }

    public void setAllocationProjectNumber(String allocationProjectNumber) {
        this.allocationProjectNumber = allocationProjectNumber;
    }

    public String getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(String qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public String getReservation() {
        return reservation;
    }

    public void setReservation(String reservation) {
        this.reservation = reservation;
    }

    public Timestamp getReservationEndTime() {
        return reservationEndTime;
    }

    public void setReservationEndTime(Timestamp reservationEndTime) {
        this.reservationEndTime = reservationEndTime;
    }

    public Timestamp getReservationStartTime() {
        return reservationStartTime;
    }

    public void setReservationStartTime(Timestamp reservationStartTime) {
        this.reservationStartTime = reservationStartTime;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
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


    public GatewayProfileEntity getGatewayProfileResource() {
        return gatewayProfileResource;
    }

    public void setGatewayProfileResource(GatewayProfileEntity gatewayProfileResource) {
        this.gatewayProfileResource = gatewayProfileResource;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public boolean isOverridebyAiravata() {
        return overridebyAiravata;
    }

    public void setOverridebyAiravata(boolean overridebyAiravata) {
        this.overridebyAiravata = overridebyAiravata;
    }

    public String getPreferredBatchQueue() {
        return preferredBatchQueue;
    }

    public void setPreferredBatchQueue(String preferredBatchQueue) {
        this.preferredBatchQueue = preferredBatchQueue;
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() {
        return preferredDataMovementProtocol;
    }

    public void setPreferredDataMovementProtocol(DataMovementProtocol preferredDataMovementProtocol) {
        this.preferredDataMovementProtocol = preferredDataMovementProtocol;
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(JobSubmissionProtocol preferredJobSubmissionProtocol) {
        this.preferredJobSubmissionProtocol = preferredJobSubmissionProtocol;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public List<SSHAccountProvisionerConfiguration> getSshAccountProvisionerConfigurations() {
        return sshAccountProvisionerConfigurations;
    }

    public void setSshAccountProvisionerConfigurations(List<SSHAccountProvisionerConfiguration> sshAccountProvisionerConfigurations) {
        this.sshAccountProvisionerConfigurations = sshAccountProvisionerConfigurations;
    }
}

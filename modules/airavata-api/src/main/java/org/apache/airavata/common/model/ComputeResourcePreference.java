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

import java.util.Objects;

/**
 * Domain model: ComputeResourcePreference
 */
public class ComputeResourcePreference {
    private String computeResourceId;
    private boolean overridebyAiravata;
    private String loginUserName;
    private JobSubmissionProtocol preferredJobSubmissionProtocol;
    private DataMovementProtocol preferredDataMovementProtocol;
    private String preferredBatchQueue;
    private String scratchLocation;
    private String allocationProjectNumber;
    private String resourceSpecificCredentialStoreToken;
    private String usageReportingGatewayId;
    private String qualityOfService;
    private String reservation;
    private long reservationStartTime;
    private long reservationEndTime;
    private String sshAccountProvisioner;
    private java.util.Map<java.lang.String, java.lang.String> sshAccountProvisionerConfig;
    private String sshAccountProvisionerAdditionalInfo;

    public ComputeResourcePreference() {}

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public boolean getOverridebyAiravata() {
        return overridebyAiravata;
    }

    public void setOverridebyAiravata(boolean overridebyAiravata) {
        this.overridebyAiravata = overridebyAiravata;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(JobSubmissionProtocol preferredJobSubmissionProtocol) {
        this.preferredJobSubmissionProtocol = preferredJobSubmissionProtocol;
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() {
        return preferredDataMovementProtocol;
    }

    public void setPreferredDataMovementProtocol(DataMovementProtocol preferredDataMovementProtocol) {
        this.preferredDataMovementProtocol = preferredDataMovementProtocol;
    }

    public String getPreferredBatchQueue() {
        return preferredBatchQueue;
    }

    public void setPreferredBatchQueue(String preferredBatchQueue) {
        this.preferredBatchQueue = preferredBatchQueue;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public String getAllocationProjectNumber() {
        return allocationProjectNumber;
    }

    public void setAllocationProjectNumber(String allocationProjectNumber) {
        this.allocationProjectNumber = allocationProjectNumber;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public String getUsageReportingGatewayId() {
        return usageReportingGatewayId;
    }

    public void setUsageReportingGatewayId(String usageReportingGatewayId) {
        this.usageReportingGatewayId = usageReportingGatewayId;
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

    public long getReservationStartTime() {
        return reservationStartTime;
    }

    public void setReservationStartTime(long reservationStartTime) {
        this.reservationStartTime = reservationStartTime;
    }

    public long getReservationEndTime() {
        return reservationEndTime;
    }

    public void setReservationEndTime(long reservationEndTime) {
        this.reservationEndTime = reservationEndTime;
    }

    public String getSshAccountProvisioner() {
        return sshAccountProvisioner;
    }

    public void setSshAccountProvisioner(String sshAccountProvisioner) {
        this.sshAccountProvisioner = sshAccountProvisioner;
    }

    public java.util.Map<java.lang.String, java.lang.String> getSshAccountProvisionerConfig() {
        return sshAccountProvisionerConfig;
    }

    public void setSshAccountProvisionerConfig(
            java.util.Map<java.lang.String, java.lang.String> sshAccountProvisionerConfig) {
        this.sshAccountProvisionerConfig = sshAccountProvisionerConfig;
    }

    public String getSshAccountProvisionerAdditionalInfo() {
        return sshAccountProvisionerAdditionalInfo;
    }

    public void setSshAccountProvisionerAdditionalInfo(String sshAccountProvisionerAdditionalInfo) {
        this.sshAccountProvisionerAdditionalInfo = sshAccountProvisionerAdditionalInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputeResourcePreference that = (ComputeResourcePreference) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(overridebyAiravata, that.overridebyAiravata)
                && Objects.equals(loginUserName, that.loginUserName)
                && Objects.equals(preferredJobSubmissionProtocol, that.preferredJobSubmissionProtocol)
                && Objects.equals(preferredDataMovementProtocol, that.preferredDataMovementProtocol)
                && Objects.equals(preferredBatchQueue, that.preferredBatchQueue)
                && Objects.equals(scratchLocation, that.scratchLocation)
                && Objects.equals(allocationProjectNumber, that.allocationProjectNumber)
                && Objects.equals(resourceSpecificCredentialStoreToken, that.resourceSpecificCredentialStoreToken)
                && Objects.equals(usageReportingGatewayId, that.usageReportingGatewayId)
                && Objects.equals(qualityOfService, that.qualityOfService)
                && Objects.equals(reservation, that.reservation)
                && Objects.equals(reservationStartTime, that.reservationStartTime)
                && Objects.equals(reservationEndTime, that.reservationEndTime)
                && Objects.equals(sshAccountProvisioner, that.sshAccountProvisioner)
                && Objects.equals(sshAccountProvisionerConfig, that.sshAccountProvisionerConfig)
                && Objects.equals(sshAccountProvisionerAdditionalInfo, that.sshAccountProvisionerAdditionalInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                computeResourceId,
                overridebyAiravata,
                loginUserName,
                preferredJobSubmissionProtocol,
                preferredDataMovementProtocol,
                preferredBatchQueue,
                scratchLocation,
                allocationProjectNumber,
                resourceSpecificCredentialStoreToken,
                usageReportingGatewayId,
                qualityOfService,
                reservation,
                reservationStartTime,
                reservationEndTime,
                sshAccountProvisioner,
                sshAccountProvisionerConfig,
                sshAccountProvisionerAdditionalInfo);
    }

    @Override
    public String toString() {
        return "ComputeResourcePreference{" + "computeResourceId=" + computeResourceId + ", overridebyAiravata="
                + overridebyAiravata + ", loginUserName=" + loginUserName + ", preferredJobSubmissionProtocol="
                + preferredJobSubmissionProtocol + ", preferredDataMovementProtocol=" + preferredDataMovementProtocol
                + ", preferredBatchQueue=" + preferredBatchQueue + ", scratchLocation=" + scratchLocation
                + ", allocationProjectNumber=" + allocationProjectNumber + ", resourceSpecificCredentialStoreToken="
                + resourceSpecificCredentialStoreToken + ", usageReportingGatewayId=" + usageReportingGatewayId
                + ", qualityOfService=" + qualityOfService + ", reservation=" + reservation + ", reservationStartTime="
                + reservationStartTime + ", reservationEndTime=" + reservationEndTime + ", sshAccountProvisioner="
                + sshAccountProvisioner + ", sshAccountProvisionerConfig=" + sshAccountProvisionerConfig
                + ", sshAccountProvisionerAdditionalInfo=" + sshAccountProvisionerAdditionalInfo + "}";
    }
}

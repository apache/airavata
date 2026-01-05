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
 * Domain model: UserComputeResourcePreference
 */
public class UserComputeResourcePreference {
    private String computeResourceId;
    private String loginUserName;
    private String preferredBatchQueue;
    private String scratchLocation;
    private String allocationProjectNumber;
    private String resourceSpecificCredentialStoreToken;
    private String qualityOfService;
    private String reservation;
    private long reservationStartTime;
    private long reservationEndTime;
    private boolean validated;
    private SSHAccountProvisionerDescription sshAccountProvisioner;

    public UserComputeResourcePreference() {}

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
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

    public boolean getValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public SSHAccountProvisionerDescription getSshAccountProvisioner() {
        return sshAccountProvisioner;
    }

    public void setSshAccountProvisioner(SSHAccountProvisionerDescription sshAccountProvisioner) {
        this.sshAccountProvisioner = sshAccountProvisioner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserComputeResourcePreference that = (UserComputeResourcePreference) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(loginUserName, that.loginUserName)
                && Objects.equals(preferredBatchQueue, that.preferredBatchQueue)
                && Objects.equals(scratchLocation, that.scratchLocation)
                && Objects.equals(allocationProjectNumber, that.allocationProjectNumber)
                && Objects.equals(resourceSpecificCredentialStoreToken, that.resourceSpecificCredentialStoreToken)
                && Objects.equals(qualityOfService, that.qualityOfService)
                && Objects.equals(reservation, that.reservation)
                && Objects.equals(reservationStartTime, that.reservationStartTime)
                && Objects.equals(reservationEndTime, that.reservationEndTime)
                && Objects.equals(validated, that.validated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                computeResourceId,
                loginUserName,
                preferredBatchQueue,
                scratchLocation,
                allocationProjectNumber,
                resourceSpecificCredentialStoreToken,
                qualityOfService,
                reservation,
                reservationStartTime,
                reservationEndTime,
                validated);
    }

    @Override
    public String toString() {
        return "UserComputeResourcePreference{" + "computeResourceId=" + computeResourceId + ", loginUserName="
                + loginUserName + ", preferredBatchQueue=" + preferredBatchQueue + ", scratchLocation="
                + scratchLocation + ", allocationProjectNumber=" + allocationProjectNumber
                + ", resourceSpecificCredentialStoreToken=" + resourceSpecificCredentialStoreToken
                + ", qualityOfService=" + qualityOfService + ", reservation=" + reservation + ", reservationStartTime="
                + reservationStartTime + ", reservationEndTime=" + reservationEndTime + ", validated=" + validated
                + "}";
    }
}

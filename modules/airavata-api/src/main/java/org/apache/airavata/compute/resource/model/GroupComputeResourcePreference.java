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
package org.apache.airavata.compute.resource.model;

/**
 * Minimal stub for GroupComputeResourcePreference.
 * Temporary placeholder pending pipeline rewrite.
 */
public class GroupComputeResourcePreference {

    private String computeResourceId;
    private String groupResourceProfileId;
    private String loginUserName;
    private String preferredBatchQueue;
    private String scratchLocation;
    private String allocationProjectNumber;
    private String resourceSpecificCredentialStoreToken;
    private String usageReportingGatewayId;
    private String qualityOfService;
    private String reservation;
    private long reservationStartTime;
    private long reservationEndTime;

    /**
     * Resource type discriminator used by activity classes to choose the correct
     * provider (SLURM, AWS, PLAIN). Defaults to PLAIN when unset.
     */
    private ComputeResourceType resourceType;

    public GroupComputeResourcePreference() {}

    public ComputeResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ComputeResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
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
}

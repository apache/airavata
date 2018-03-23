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

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * The persistent class for the user_compute_resource_preference database table.
 */
@Entity
@Table(name = "USER_COMPUTE_RESOURCE_PREFERENCE")
@IdClass(UserComputeResourcePreferencePK.class)
public class UserComputeResourcePreferenceEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_ID")
    private String computeResourceId;

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "PREFERED_BATCH_QUEUE")
    private String preferredBatchQueue;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialStoreToken;

    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;

    @Column(name = "ALLOCATION_PROJECT_NUMBER")
    private String allocationProjectNumber;

    @Column(name = "QUALITY_OF_SERVICE")
    private String qualityOfService;

    @Column(name = "RESERVATION")
    private String reservation;

    @Column(name = "RESERVATION_START_TIME")
    private Timestamp reservationStartTime;

    @Column(name = "RESERVATION_END_TIME")
    private Timestamp reservationEndTime;

    @Column(name = "SCRATCH_LOCATION")
    private String scratchLocation;

    @Column(name = "VALIDATED")
    private boolean validated;

    @ManyToOne(targetEntity = UserResourceProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "USER_ID"),
            @JoinColumn(name="GATEWAY_ID")
    })
    private UserResourceProfileEntity userResourceProfile;

    public UserComputeResourcePreferenceEntity() {
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getPreferredBatchQueue() {
        return preferredBatchQueue;
    }

    public void setPreferredBatchQueue(String preferredBatchQueue) {
        this.preferredBatchQueue = preferredBatchQueue;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
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

    public Timestamp getReservationStartTime() {
        return reservationStartTime;
    }

    public void setReservationStartTime(Timestamp reservationStartTime) {
        this.reservationStartTime = reservationStartTime;
    }

    public Timestamp getReservationEndTime() {
        return reservationEndTime;
    }

    public void setReservationEndTime(Timestamp reservationEndTime) {
        this.reservationEndTime = reservationEndTime;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public UserResourceProfileEntity getUserResourceProfile() {
        return userResourceProfile;
    }

    public void setUserResourceProfile(UserResourceProfileEntity userResourceProfile) {
        this.userResourceProfile = userResourceProfile;
    }
}

/**
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
 */
package org.apache.airavata.registry.core.app.catalog.model;


import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "USER_COMPUTE_RESOURCE_PREFERENCE")
@IdClass(UserComputeResourcePreferencePK.class)
public class UserComputeResourcePreference {
    @Id
    @Column(name = "USER_ID")
    private String userId;
    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceId;
    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayID;
    @Column(name = "PREFERED_BATCH_QUEUE")
    private String batchQueue;
    @Column(name = "SCRATCH_LOCATION")
    private String scratchLocation;
    @Column(name = "ALLOCATION_PROJECT_NUMBER")
    private String projectNumber;
    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;
    @Column(name = "RESOURCE_CS_TOKEN")
    private String computeResourceCSToken;
    @Column(name = "QUALITY_OF_SERVICE")
    private String qualityOfService;
    @Column(name = "RESERVATION")
    private String reservation;
    @Column(name = "RESERVATION_START_TIME")
    private Timestamp reservationStartTime;
    @Column(name = "RESERVATION_END_TIME")
    private Timestamp reservationEndTime;
    @Column(name = "VALIDATED")
    private boolean validated;


    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "RESOURCE_ID")
    private ComputeResource computeHostResource;


    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumns({
        @JoinColumn(name = "USER_ID"),
        @JoinColumn(name = "GATEWAY_ID")
    })
    private UserResourceProfile userResouceProfile;

    /*User Id should be linked to user profile table once it is finalized and created*/

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    public String getBatchQueue() {
        return batchQueue;
    }

    public void setBatchQueue(String batchQueue) {
        this.batchQueue = batchQueue;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public ComputeResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }

    public UserResourceProfile getUserResouceProfile() {
        return userResouceProfile;
    }

    public void setUserResouceProfile(UserResourceProfile userResouceProfile) {
        this.userResouceProfile = userResouceProfile;
    }


    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getComputeResourceCSToken() {
        return computeResourceCSToken;
    }

    public void setComputeResourceCSToken(String computeResourceCSToken) {
        this.computeResourceCSToken = computeResourceCSToken;
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

    public void setReservationStartTime(Timestamp reservationStratTime) {
        this.reservationStartTime = reservationStratTime;
    }

    public Timestamp getReservationEndTime() {
        return reservationEndTime;
    }

    public void setReservationEndTime(Timestamp reservationEndTime) {
        this.reservationEndTime = reservationEndTime;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }
}

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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;


/**
 * The persistent class for the compute_resource_preference database table.
 */
@Entity
@Table(name = "compute_resource_preference")
public class ComputeResourcePreferenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ComputeResourcePreferencePK id;

    @Column(name = "ALLOCATION_PROJECT_NUMBER")
    private String allocationProjectNumber;

    @Column(name = "LOGIN_USERNAME")
    private String loginUsername;

    @Column(name = "OVERRIDE_BY_AIRAVATA")
    private short overrideByAiravata;

    @Column(name = "PREFERED_BATCH_QUEUE")
    private String preferedBatchQueue;

    @Column(name = "PREFERED_DATA_MOVE_PROTOCOL")
    private String preferedDataMoveProtocol;

    @Column(name = "PREFERED_JOB_SUB_PROTOCOL")
    private String preferedJobSubProtocol;

    @Column(name = "QUALITY_OF_SERVICE")
    private String qualityOfService;

    private String reservation;

    @Column(name = "RESERVATION_END_TIME")
    private Timestamp reservationEndTime;

    @Column(name = "RESERVATION_START_TIME")
    private Timestamp reservationStartTime;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceCsToken;

    @Column(name = "SCRATCH_LOCATION")
    private String scratchLocation;

    @Column(name = "USAGE_REPORTING_GATEWAY_ID")
    private String usageReportingGatewayId;

    public ComputeResourcePreferenceEntity() {
    }

    public ComputeResourcePreferencePK getId() {
        return id;
    }

    public void setId(ComputeResourcePreferencePK id) {
        this.id = id;
    }

    public String getAllocationProjectNumber() {
        return allocationProjectNumber;
    }

    public void setAllocationProjectNumber(String allocationProjectNumber) {
        this.allocationProjectNumber = allocationProjectNumber;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public short getOverrideByAiravata() {
        return overrideByAiravata;
    }

    public void setOverrideByAiravata(short overrideByAiravata) {
        this.overrideByAiravata = overrideByAiravata;
    }

    public String getPreferedBatchQueue() {
        return preferedBatchQueue;
    }

    public void setPreferedBatchQueue(String preferedBatchQueue) {
        this.preferedBatchQueue = preferedBatchQueue;
    }

    public String getPreferedDataMoveProtocol() {
        return preferedDataMoveProtocol;
    }

    public void setPreferedDataMoveProtocol(String preferedDataMoveProtocol) {
        this.preferedDataMoveProtocol = preferedDataMoveProtocol;
    }

    public String getPreferedJobSubProtocol() {
        return preferedJobSubProtocol;
    }

    public void setPreferedJobSubProtocol(String preferedJobSubProtocol) {
        this.preferedJobSubProtocol = preferedJobSubProtocol;
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

    public String getResourceCsToken() {
        return resourceCsToken;
    }

    public void setResourceCsToken(String resourceCsToken) {
        this.resourceCsToken = resourceCsToken;
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
}
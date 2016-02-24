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

package org.apache.airavata.registry.core.app.catalog.model;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "COMPUTE_RESOURCE_PREFERENCE")
@IdClass(ComputeResourcePreferencePK.class)
public class ComputeResourcePreference {
    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;
    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceId;
    @Column(name = "OVERRIDE_BY_AIRAVATA")
    private boolean overrideByAiravata;
    @Column(name = "PREFERED_JOB_SUB_PROTOCOL")
    private String preferedJobSubmissionProtocol;
    @Column(name = "PREFERED_DATA_MOVE_PROTOCOL")
    private String preferedDataMoveProtocol;
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
    @Column(name = "USAGE_REPORTING_GATEWAY_ID")
    private String usageReportingGWId;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "RESOURCE_ID")
    private ComputeResource computeHostResource;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_ID")
    private GatewayProfile gatewayProfile;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public boolean isOverrideByAiravata() {
        return overrideByAiravata;
    }

    public void setOverrideByAiravata(boolean overrideByAiravata) {
        this.overrideByAiravata = overrideByAiravata;
    }

    public String getPreferedJobSubmissionProtocol() {
        return preferedJobSubmissionProtocol;
    }

    public void setPreferedJobSubmissionProtocol(String preferedJobSubmissionProtocol) {
        this.preferedJobSubmissionProtocol = preferedJobSubmissionProtocol;
    }

    public String getPreferedDataMoveProtocol() {
        return preferedDataMoveProtocol;
    }

    public void setPreferedDataMoveProtocol(String preferedDataMoveProtocol) {
        this.preferedDataMoveProtocol = preferedDataMoveProtocol;
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

    public GatewayProfile getGatewayProfile() {
        return gatewayProfile;
    }

    public void setGatewayProfile(GatewayProfile gatewayProfile) {
        this.gatewayProfile = gatewayProfile;
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

    public String getUsageReportingGWId() {
        return usageReportingGWId;
    }

    public void setUsageReportingGWId(String usageReportingGWId) {
        this.usageReportingGWId = usageReportingGWId;
    }
}

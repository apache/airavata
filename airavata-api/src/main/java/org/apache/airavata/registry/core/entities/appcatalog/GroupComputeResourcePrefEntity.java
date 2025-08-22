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
package org.apache.airavata.registry.core.entities.appcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.data.movement.DataMovementProtocol;


/**
 * The persistent class for the group_compute_resource_preference database table.
 */
@Entity
@Table(name = "GROUP_COMPUTE_RESOURCE_PREFERENCE")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "RESOURCE_TYPE", discriminatorType = DiscriminatorType.STRING)
@IdClass(GroupComputeResourcePrefPK.class)
public abstract class GroupComputeResourcePrefEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "RESOURCE_ID")
    @Id
    private String computeResourceId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    @Id
    private String groupResourceProfileId;

    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;

    @Column(name = "SCRATCH_LOCATION")
    private String scratchLocation;

    @Column(name = "OVERRIDE_BY_AIRAVATA")
    private short overridebyAiravata;

    @Column(name = "PREFERED_DATA_MOVE_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private DataMovementProtocol preferredDataMovementProtocol; // TODO introduce S3

    @Column(name = "PREFERED_JOB_SUB_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private JobSubmissionProtocol preferredJobSubmissionProtocol; // TODO introduce CLOUD

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialStoreToken;

    @ManyToOne(targetEntity = GroupResourceProfileEntity.class, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", nullable = false, updatable = false)
    private GroupResourceProfileEntity groupResourceProfile;

    public GroupComputeResourcePrefEntity() {}

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

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public short getOverridebyAiravata() {
        return overridebyAiravata;
    }

    public void setOverridebyAiravata(short overridebyAiravata) {
        this.overridebyAiravata = overridebyAiravata;
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

    public GroupResourceProfileEntity getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfileEntity groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }
}

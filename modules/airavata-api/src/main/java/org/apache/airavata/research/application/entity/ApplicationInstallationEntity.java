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
package org.apache.airavata.research.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import org.apache.airavata.compute.resource.entity.ResourceEntity;

/**
 * Entity tracking the installation state of an application on a specific compute resource.
 *
 * <p>When an {@link ApplicationEntity} is deployed to a {@link ResourceEntity}, an installation
 * record is created to track the progress and outcome of the deployment process. The
 * {@code status} field follows a lifecycle of {@code PENDING -> INSTALLING -> INSTALLED}
 * (or {@code FAILED} on error). The {@code installPath} records where on the remote
 * filesystem the application was installed.
 *
 * <p>This entity does not use Spring Data auditing; {@code installedAt} is set explicitly
 * by the service layer when the installation completes.
 */
@Entity
@Table(name = "application_installation")
public class ApplicationInstallationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "installation_id")
    private String installationId;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "login_username", nullable = false)
    private String loginUsername;

    @Column(name = "install_path", length = 500)
    private String installPath;

    @Column(name = "status", length = 50)
    private String status = "PENDING";

    @Column(name = "installed_at")
    private Instant installedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private ApplicationEntity application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", insertable = false, updatable = false)
    private ResourceEntity resource;

    public ApplicationInstallationEntity() {}

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(Instant installedAt) {
        this.installedAt = installedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ApplicationEntity getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntity application) {
        this.application = application;
    }

    public ResourceEntity getResource() {
        return resource;
    }

    public void setResource(ResourceEntity resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ApplicationInstallationEntity{"
                + "installationId='" + installationId + '\''
                + ", applicationId='" + applicationId + '\''
                + ", resourceId='" + resourceId + '\''
                + ", loginUsername='" + loginUsername + '\''
                + ", status='" + status + '\''
                + '}';
    }
}

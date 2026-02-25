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
package org.apache.airavata.research.application.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model: ApplicationInstallation
 * Records the deployment of an {@link Application} onto a specific {@link Resource}.
 * The {@code installPath} is the absolute directory on the remote resource where the
 * application binary or environment resides after the install script completes.
 * Lifecycle: PENDING &rarr; INSTALLING &rarr; INSTALLED (or FAILED on error).
 */
public class ApplicationInstallation {
    private String installationId;
    private String applicationId;
    private String resourceId;
    /** The OS-level username under whose home the application is installed. */
    private String loginUsername;
    /** Absolute path on the remote resource where the application is installed. */
    private String installPath;
    /**
     * Current installation lifecycle status.
     * Accepted values: {@code "PENDING"}, {@code "INSTALLING"}, {@code "INSTALLED"}, {@code "FAILED"}.
     */
    private String status;
    /** Timestamp at which the installation reached {@code "INSTALLED"} status. Null otherwise. */
    private Instant installedAt;
    /** Human-readable error message populated when {@code status} is {@code "FAILED"}. */
    private String errorMessage;

    private Instant createdAt;

    public ApplicationInstallation() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationInstallation that = (ApplicationInstallation) o;
        return Objects.equals(installationId, that.installationId)
                && Objects.equals(applicationId, that.applicationId)
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(loginUsername, that.loginUsername)
                && Objects.equals(installPath, that.installPath)
                && Objects.equals(status, that.status)
                && Objects.equals(installedAt, that.installedAt)
                && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                installationId,
                applicationId,
                resourceId,
                loginUsername,
                installPath,
                status,
                installedAt,
                errorMessage,
                createdAt);
    }

    @Override
    public String toString() {
        return "ApplicationInstallation{" + "installationId=" + installationId + ", applicationId=" + applicationId
                + ", resourceId=" + resourceId + ", loginUsername=" + loginUsername + ", installPath=" + installPath
                + ", status=" + status + ", installedAt=" + installedAt + ", errorMessage=" + errorMessage
                + ", createdAt=" + createdAt + "}";
    }
}

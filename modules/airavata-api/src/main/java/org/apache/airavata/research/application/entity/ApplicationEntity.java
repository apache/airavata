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
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import org.apache.airavata.research.application.model.ApplicationField;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity representing a science application registered in the Airavata registry.
 *
 * <p>An application defines the computational workflow template that experiments are
 * based on. It captures input/output schemas ({@code inputs}, {@code outputs}) as JSON
 * lists of {@link ApplicationField} descriptors, along with the install and run scripts used
 * during application deployment and execution on target resources.
 *
 * <p>The {@code scope} field controls visibility: {@code GATEWAY} scope makes the
 * application available to all users within the gateway, while narrower scopes (e.g.
 * {@code USER}) restrict access to the owning user.
 */
@Entity
@Table(name = "application")
@EntityListeners(AuditingEntityListener.class)
public class ApplicationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "gateway_id", nullable = false)
    private String gatewayId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", length = 100)
    private String version;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inputs", columnDefinition = "json")
    private List<ApplicationField> inputs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outputs", columnDefinition = "json")
    private List<ApplicationField> outputs;

    @Column(name = "install_script", columnDefinition = "MEDIUMTEXT")
    private String installScript;

    @Column(name = "run_script", columnDefinition = "MEDIUMTEXT")
    private String runScript;

    @Column(name = "scope", length = 50)
    private String scope = "GATEWAY";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public ApplicationEntity() {}

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ApplicationField> getInputs() {
        return inputs;
    }

    public void setInputs(List<ApplicationField> inputs) {
        this.inputs = inputs;
    }

    public List<ApplicationField> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ApplicationField> outputs) {
        this.outputs = outputs;
    }

    public String getInstallScript() {
        return installScript;
    }

    public void setInstallScript(String installScript) {
        this.installScript = installScript;
    }

    public String getRunScript() {
        return runScript;
    }

    public void setRunScript(String runScript) {
        this.runScript = runScript;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ApplicationEntity{"
                + "applicationId='" + applicationId + '\''
                + ", gatewayId='" + gatewayId + '\''
                + ", ownerName='" + ownerName + '\''
                + ", name='" + name + '\''
                + ", version='" + version + '\''
                + ", scope='" + scope + '\''
                + '}';
    }
}

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
import java.util.List;
import java.util.Objects;

/**
 * Domain model: Application
 * Represents a scientific application registered in the gateway. An application defines its
 * interface (inputs/outputs via {@link ApplicationField}), an optional install script run once per
 * resource, and a run script executed for every experiment. The {@code scope} field controls
 * visibility: {@link ApplicationScope#GATEWAY} for all users or {@link ApplicationScope#USER}
 * for the owner only.
 */
public class Application {
    private String applicationId;
    private String gatewayId;
    /** Gateway username of the application owner. */
    private String ownerName;

    private String name;
    private String version;
    private String description;
    /** Ordered list of input fields presented to experiment submitters. */
    private List<ApplicationField> inputs;
    /** Ordered list of output fields produced by the application. */
    private List<ApplicationField> outputs;
    /**
     * Shell script used to install the application on a resource.
     * Run once during {@link ApplicationInstallation}. May be null for pre-installed apps.
     */
    private String installScript;
    /**
     * Shell script template executed for each experiment run.
     * May reference input field names as template variables.
     */
    private String runScript;
    /** Visibility scope controlling who can see and use this application. */
    private ApplicationScope scope;

    private Instant createdAt;
    private Instant updatedAt;

    public Application() {}

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

    public ApplicationScope getScope() {
        return scope;
    }

    public void setScope(ApplicationScope scope) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(applicationId, that.applicationId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(ownerName, that.ownerName)
                && Objects.equals(name, that.name)
                && Objects.equals(version, that.version)
                && Objects.equals(description, that.description)
                && Objects.equals(inputs, that.inputs)
                && Objects.equals(outputs, that.outputs)
                && Objects.equals(installScript, that.installScript)
                && Objects.equals(runScript, that.runScript)
                && Objects.equals(scope, that.scope)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                applicationId,
                gatewayId,
                ownerName,
                name,
                version,
                description,
                inputs,
                outputs,
                installScript,
                runScript,
                scope,
                createdAt,
                updatedAt);
    }

    @Override
    public String toString() {
        return "Application{" + "applicationId=" + applicationId + ", gatewayId=" + gatewayId
                + ", ownerName=" + ownerName + ", name=" + name + ", version=" + version
                + ", description=" + description + ", inputs=" + inputs + ", outputs=" + outputs
                + ", installScript=" + installScript + ", runScript=" + runScript + ", scope=" + scope
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}

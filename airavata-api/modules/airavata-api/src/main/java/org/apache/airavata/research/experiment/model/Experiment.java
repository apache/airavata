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
package org.apache.airavata.research.experiment.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.airavata.execution.process.ProcessModel;

/**
 * Domain model: Experiment
 *
 * <p>Inputs and outputs are represented as typed {@link ExperimentInput} and
 * {@link ExperimentOutput} lists. Experiment state is a direct field (not event-driven).
 * The scheduling field carries compute resource scheduling parameters as a plain map
 * so that no generated preference types leak into the core domain.
 */
public class Experiment {

    private String experimentId;

    @NotBlank(message = "projectId is required")
    private String projectId;

    private String gatewayId;
    private String userName;

    @NotBlank(message = "experimentName is required")
    private String experimentName;

    private String description;

    /** Identifier of the registered application this experiment runs. */
    private String applicationId;

    /** Identifier of the credential/binding used for resource access. */
    private String bindingId;

    /** Current experiment state (CREATED, LAUNCHED, EXECUTING, COMPLETED, FAILED, etc.). */
    private ExperimentState state;

    /** Structured experiment input parameters. */
    private List<ExperimentInput> inputs;

    /** Structured experiment output parameters. */
    private List<ExperimentOutput> outputs;

    /**
     * Compute resource scheduling parameters (queue, node count, wall time, etc.) expressed as a
     * plain map so no generated preference types are required in the domain layer.
     */
    private Map<String, Object> scheduling;

    /** Instant when the experiment was created. */
    private Instant createdAt;

    /** Optional reference to a parent experiment (e.g. for cloned or derived experiments). */
    private String parentExperimentId;

    /** User-defined tags for categorisation and search. */
    private List<String> tags;

    private List<ProcessModel> processes;

    private UserConfigurationData userConfigurationData;
    private boolean enableEmailNotification;
    private List<String> emailAddresses;

    public Experiment() {}

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public ExperimentState getState() {
        return state;
    }

    public void setState(ExperimentState state) {
        this.state = state;
    }

    public List<ExperimentInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<ExperimentInput> inputs) {
        this.inputs = inputs;
    }

    public List<ExperimentOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ExperimentOutput> outputs) {
        this.outputs = outputs;
    }

    public Map<String, Object> getScheduling() {
        return scheduling;
    }

    public void setScheduling(Map<String, Object> scheduling) {
        this.scheduling = scheduling;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getParentExperimentId() {
        return parentExperimentId;
    }

    public void setParentExperimentId(String parentExperimentId) {
        this.parentExperimentId = parentExperimentId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ProcessModel> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessModel> processes) {
        this.processes = processes;
    }

    public UserConfigurationData getUserConfigurationData() {
        return userConfigurationData;
    }

    public void setUserConfigurationData(UserConfigurationData userConfigurationData) {
        this.userConfigurationData = userConfigurationData;
    }

    public boolean getEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Experiment that = (Experiment) o;
        return Objects.equals(experimentId, that.experimentId)
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(userName, that.userName)
                && Objects.equals(experimentName, that.experimentName)
                && Objects.equals(description, that.description)
                && Objects.equals(applicationId, that.applicationId)
                && Objects.equals(bindingId, that.bindingId)
                && state == that.state
                && Objects.equals(inputs, that.inputs)
                && Objects.equals(outputs, that.outputs)
                && Objects.equals(scheduling, that.scheduling)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(parentExperimentId, that.parentExperimentId)
                && Objects.equals(tags, that.tags)
                && Objects.equals(processes, that.processes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                experimentId,
                projectId,
                gatewayId,
                userName,
                experimentName,
                description,
                applicationId,
                bindingId,
                state,
                inputs,
                outputs,
                scheduling,
                createdAt,
                parentExperimentId,
                tags,
                processes);
    }

    @Override
    public String toString() {
        return "Experiment{"
                + "experimentId=" + experimentId
                + ", projectId=" + projectId
                + ", gatewayId=" + gatewayId
                + ", userName=" + userName
                + ", experimentName=" + experimentName
                + ", description=" + description
                + ", applicationId=" + applicationId
                + ", bindingId=" + bindingId
                + ", state=" + state
                + ", inputs=" + inputs
                + ", outputs=" + outputs
                + ", scheduling=" + scheduling
                + ", createdAt=" + createdAt
                + ", parentExperimentId=" + parentExperimentId
                + ", tags=" + tags
                + ", processes=" + processes
                + "}";
    }
}

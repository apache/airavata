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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: WorkflowHandler
 */
public class WorkflowHandler {
    private String id;
    private HandlerType type;
    private List<InputDataObjectType> inputs;
    private List<OutputDataObjectType> outputs;
    private List<HandlerStatus> statuses;
    private List<ErrorModel> errors;
    private long createdAt;
    private long updatedAt;

    public WorkflowHandler() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HandlerType getType() {
        return type;
    }

    public void setType(HandlerType type) {
        this.type = type;
    }

    public List<InputDataObjectType> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputDataObjectType> inputs) {
        this.inputs = inputs;
    }

    public List<OutputDataObjectType> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<OutputDataObjectType> outputs) {
        this.outputs = outputs;
    }

    public List<HandlerStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<HandlerStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowHandler that = (WorkflowHandler) o;
        return Objects.equals(id, that.id)
                && Objects.equals(type, that.type)
                && Objects.equals(inputs, that.inputs)
                && Objects.equals(outputs, that.outputs)
                && Objects.equals(statuses, that.statuses)
                && Objects.equals(errors, that.errors)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, inputs, outputs, statuses, errors, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "WorkflowHandler{" + "id=" + id + ", type=" + type + ", inputs=" + inputs + ", outputs=" + outputs
                + ", statuses=" + statuses + ", errors=" + errors + ", createdAt=" + createdAt + ", updatedAt="
                + updatedAt + "}";
    }
}

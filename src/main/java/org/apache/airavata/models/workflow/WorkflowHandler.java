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
package org.apache.airavata.models.workflow;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;
import org.apache.airavata.models.commons.ErrorModel;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.workflow.proto.WorkflowHandler}.
 */
public record WorkflowHandler(
        String id,
        HandlerType type,
        List<InputDataObjectType> inputs,
        List<OutputDataObjectType> outputs,
        List<HandlerStatus> statuses,
        List<ErrorModel> errors,
        long createdAt,
        long updatedAt) {

    public WorkflowHandler {
        inputs = inputs == null ? List.of() : List.copyOf(inputs);
        outputs = outputs == null ? List.of() : List.copyOf(outputs);
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private HandlerType type = HandlerType.HANDLER_TYPE_UNKNOWN;
        private List<InputDataObjectType> inputs = new ArrayList<>();
        private List<OutputDataObjectType> outputs = new ArrayList<>();
        private List<HandlerStatus> statuses = new ArrayList<>();
        private List<ErrorModel> errors = new ArrayList<>();
        private long createdAt;
        private long updatedAt;

        private Builder() {}

        private Builder(WorkflowHandler source) {
            this.id = source.id;
            this.type = source.type;
            this.inputs = new ArrayList<>(source.inputs);
            this.outputs = new ArrayList<>(source.outputs);
            this.statuses = new ArrayList<>(source.statuses);
            this.errors = new ArrayList<>(source.errors);
            this.createdAt = source.createdAt;
            this.updatedAt = source.updatedAt;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setType(HandlerType type) {
            this.type = type;
            return this;
        }

        public Builder addInputs(InputDataObjectType value) {
            this.inputs.add(value);
            return this;
        }

        public Builder addAllInputs(Iterable<InputDataObjectType> values) {
            values.forEach(this.inputs::add);
            return this;
        }

        public Builder clearInputs() {
            this.inputs.clear();
            return this;
        }

        public Builder addOutputs(OutputDataObjectType value) {
            this.outputs.add(value);
            return this;
        }

        public Builder addAllOutputs(Iterable<OutputDataObjectType> values) {
            values.forEach(this.outputs::add);
            return this;
        }

        public Builder clearOutputs() {
            this.outputs.clear();
            return this;
        }

        public Builder addStatuses(HandlerStatus value) {
            this.statuses.add(value);
            return this;
        }

        public Builder addAllStatuses(Iterable<HandlerStatus> values) {
            values.forEach(this.statuses::add);
            return this;
        }

        public Builder clearStatuses() {
            this.statuses.clear();
            return this;
        }

        public Builder addErrors(ErrorModel value) {
            this.errors.add(value);
            return this;
        }

        public Builder addAllErrors(Iterable<ErrorModel> values) {
            values.forEach(this.errors::add);
            return this;
        }

        public Builder clearErrors() {
            this.errors.clear();
            return this;
        }

        public Builder setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public WorkflowHandler build() {
            return new WorkflowHandler(id, type, inputs, outputs, statuses, errors, createdAt, updatedAt);
        }
    }
}

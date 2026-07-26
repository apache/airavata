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

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.workflow.proto.WorkflowConnection}.
 */
public record WorkflowConnection(
        String id,
        DataBlock dataBlock,
        ComponentType fromType,
        String fromId,
        String fromOutputName,
        ComponentType toType,
        String toId,
        String toInputName,
        long createdAt,
        long updatedAt) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private DataBlock dataBlock;
        private ComponentType fromType = ComponentType.COMPONENT_TYPE_UNKNOWN;
        private String fromId = "";
        private String fromOutputName = "";
        private ComponentType toType = ComponentType.COMPONENT_TYPE_UNKNOWN;
        private String toId = "";
        private String toInputName = "";
        private long createdAt;
        private long updatedAt;

        private Builder() {}

        private Builder(WorkflowConnection source) {
            this.id = source.id;
            this.dataBlock = source.dataBlock;
            this.fromType = source.fromType;
            this.fromId = source.fromId;
            this.fromOutputName = source.fromOutputName;
            this.toType = source.toType;
            this.toId = source.toId;
            this.toInputName = source.toInputName;
            this.createdAt = source.createdAt;
            this.updatedAt = source.updatedAt;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setDataBlock(DataBlock dataBlock) {
            this.dataBlock = dataBlock;
            return this;
        }

        public Builder setFromType(ComponentType fromType) {
            this.fromType = fromType;
            return this;
        }

        public Builder setFromId(String fromId) {
            this.fromId = fromId;
            return this;
        }

        public Builder setFromOutputName(String fromOutputName) {
            this.fromOutputName = fromOutputName;
            return this;
        }

        public Builder setToType(ComponentType toType) {
            this.toType = toType;
            return this;
        }

        public Builder setToId(String toId) {
            this.toId = toId;
            return this;
        }

        public Builder setToInputName(String toInputName) {
            this.toInputName = toInputName;
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

        public WorkflowConnection build() {
            return new WorkflowConnection(
                    id, dataBlock, fromType, fromId, fromOutputName, toType, toId, toInputName, createdAt,
                    updatedAt);
        }
    }
}

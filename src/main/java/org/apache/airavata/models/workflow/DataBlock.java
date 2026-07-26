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

import org.apache.airavata.models.application.io.DataType;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.workflow.proto.DataBlock}.
 */
public record DataBlock(String id, String value, DataType type, long createdAt, long updatedAt) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String value = "";
        private DataType type = DataType.DATA_TYPE_UNKNOWN;
        private long createdAt;
        private long updatedAt;

        private Builder() {}

        private Builder(DataBlock source) {
            this.id = source.id;
            this.value = source.value;
            this.type = source.type;
            this.createdAt = source.createdAt;
            this.updatedAt = source.updatedAt;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setType(DataType type) {
            this.type = type;
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

        public DataBlock build() {
            return new DataBlock(id, value, type, createdAt, updatedAt);
        }
    }
}

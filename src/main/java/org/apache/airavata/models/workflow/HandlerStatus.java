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
 * {@code org.apache.airavata.model.workflow.proto.HandlerStatus}.
 */
public record HandlerStatus(String id, HandlerState state, String description, long updatedAt) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private HandlerState state = HandlerState.HANDLER_STATE_UNKNOWN;
        private String description = "";
        private long updatedAt;

        private Builder() {}

        private Builder(HandlerStatus source) {
            this.id = source.id;
            this.state = source.state;
            this.description = source.description;
            this.updatedAt = source.updatedAt;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setState(HandlerState state) {
            this.state = state;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public HandlerStatus build() {
            return new HandlerStatus(id, state, description, updatedAt);
        }
    }
}

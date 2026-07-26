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
package org.apache.airavata.models.appcatalog.parser;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.parser.proto.ParserInput}.
 */
public record ParserInput(String id, String name, boolean requiredInput, String parserId, IOType type) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String name = "";
        private boolean requiredInput;
        private String parserId = "";
        private IOType type = IOType.IO_TYPE_UNKNOWN;

        private Builder() {}

        private Builder(ParserInput source) {
            this.id = source.id;
            this.name = source.name;
            this.requiredInput = source.requiredInput;
            this.parserId = source.parserId;
            this.type = source.type;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRequiredInput(boolean requiredInput) {
            this.requiredInput = requiredInput;
            return this;
        }

        public Builder setParserId(String parserId) {
            this.parserId = parserId;
            return this;
        }

        public Builder setType(IOType type) {
            this.type = type;
            return this;
        }

        public ParserInput build() {
            return new ParserInput(id, name, requiredInput, parserId, type);
        }
    }
}

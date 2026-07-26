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
 * {@code org.apache.airavata.model.appcatalog.parser.proto.ParserConnectorInput}.
 */
public record ParserConnectorInput(
        String id, String inputId, String parentOutputId, String value, String parserConnectorId) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String inputId = "";
        private String parentOutputId = "";
        private String value = "";
        private String parserConnectorId = "";

        private Builder() {}

        private Builder(ParserConnectorInput source) {
            this.id = source.id;
            this.inputId = source.inputId;
            this.parentOutputId = source.parentOutputId;
            this.value = source.value;
            this.parserConnectorId = source.parserConnectorId;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setInputId(String inputId) {
            this.inputId = inputId;
            return this;
        }

        public Builder setParentOutputId(String parentOutputId) {
            this.parentOutputId = parentOutputId;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setParserConnectorId(String parserConnectorId) {
            this.parserConnectorId = parserConnectorId;
            return this;
        }

        public ParserConnectorInput build() {
            return new ParserConnectorInput(id, inputId, parentOutputId, value, parserConnectorId);
        }
    }
}

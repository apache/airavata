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

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate}.
 */
public record ParsingTemplate(
        String id,
        String applicationInterface,
        List<ParsingTemplateInput> initialInputs,
        List<ParserConnector> parserConnections,
        String gatewayId) {

    public ParsingTemplate {
        initialInputs = initialInputs == null ? List.of() : List.copyOf(initialInputs);
        parserConnections = parserConnections == null ? List.of() : List.copyOf(parserConnections);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String applicationInterface = "";
        private List<ParsingTemplateInput> initialInputs = new ArrayList<>();
        private List<ParserConnector> parserConnections = new ArrayList<>();
        private String gatewayId = "";

        private Builder() {}

        private Builder(ParsingTemplate source) {
            this.id = source.id;
            this.applicationInterface = source.applicationInterface;
            this.initialInputs = new ArrayList<>(source.initialInputs);
            this.parserConnections = new ArrayList<>(source.parserConnections);
            this.gatewayId = source.gatewayId;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setApplicationInterface(String applicationInterface) {
            this.applicationInterface = applicationInterface;
            return this;
        }

        public Builder addInitialInputs(ParsingTemplateInput value) {
            this.initialInputs.add(value);
            return this;
        }

        public Builder addAllInitialInputs(Iterable<ParsingTemplateInput> values) {
            values.forEach(this.initialInputs::add);
            return this;
        }

        public Builder clearInitialInputs() {
            this.initialInputs.clear();
            return this;
        }

        public Builder addParserConnections(ParserConnector value) {
            this.parserConnections.add(value);
            return this;
        }

        public Builder addAllParserConnections(Iterable<ParserConnector> values) {
            values.forEach(this.parserConnections::add);
            return this;
        }

        public Builder clearParserConnections() {
            this.parserConnections.clear();
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public ParsingTemplate build() {
            return new ParsingTemplate(
                    id, applicationInterface, initialInputs, parserConnections, gatewayId);
        }
    }
}

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
 * {@code org.apache.airavata.model.appcatalog.parser.proto.ParserConnector}.
 */
public record ParserConnector(
        String id,
        String parentParserId,
        String childParserId,
        List<ParserConnectorInput> connectorInputs,
        String parsingTemplateId) {

    public ParserConnector {
        connectorInputs = connectorInputs == null ? List.of() : List.copyOf(connectorInputs);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String parentParserId = "";
        private String childParserId = "";
        private List<ParserConnectorInput> connectorInputs = new ArrayList<>();
        private String parsingTemplateId = "";

        private Builder() {}

        private Builder(ParserConnector source) {
            this.id = source.id;
            this.parentParserId = source.parentParserId;
            this.childParserId = source.childParserId;
            this.connectorInputs = new ArrayList<>(source.connectorInputs);
            this.parsingTemplateId = source.parsingTemplateId;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setParentParserId(String parentParserId) {
            this.parentParserId = parentParserId;
            return this;
        }

        public Builder setChildParserId(String childParserId) {
            this.childParserId = childParserId;
            return this;
        }

        public Builder addConnectorInputs(ParserConnectorInput value) {
            this.connectorInputs.add(value);
            return this;
        }

        public Builder addAllConnectorInputs(Iterable<ParserConnectorInput> values) {
            values.forEach(this.connectorInputs::add);
            return this;
        }

        public Builder clearConnectorInputs() {
            this.connectorInputs.clear();
            return this;
        }

        public Builder setParsingTemplateId(String parsingTemplateId) {
            this.parsingTemplateId = parsingTemplateId;
            return this;
        }

        public ParserConnector build() {
            return new ParserConnector(id, parentParserId, childParserId, connectorInputs, parsingTemplateId);
        }
    }
}

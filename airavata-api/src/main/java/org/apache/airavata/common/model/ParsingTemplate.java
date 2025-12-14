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
 * Domain model: ParsingTemplate
 */
public class ParsingTemplate {
    private String id;
    private String applicationInterface;
    private List<ParsingTemplateInput> initialInputs;
    private List<ParserConnector> parserConnections;
    private String gatewayId;

    public ParsingTemplate() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationInterface() {
        return applicationInterface;
    }

    public void setApplicationInterface(String applicationInterface) {
        this.applicationInterface = applicationInterface;
    }

    public List<ParsingTemplateInput> getInitialInputs() {
        return initialInputs;
    }

    public void setInitialInputs(List<ParsingTemplateInput> initialInputs) {
        this.initialInputs = initialInputs;
    }

    public List<ParserConnector> getParserConnections() {
        return parserConnections;
    }

    public void setParserConnections(List<ParserConnector> parserConnections) {
        this.parserConnections = parserConnections;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsingTemplate that = (ParsingTemplate) o;
        return Objects.equals(id, that.id)
                && Objects.equals(applicationInterface, that.applicationInterface)
                && Objects.equals(initialInputs, that.initialInputs)
                && Objects.equals(parserConnections, that.parserConnections)
                && Objects.equals(gatewayId, that.gatewayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, applicationInterface, initialInputs, parserConnections, gatewayId);
    }

    @Override
    public String toString() {
        return "ParsingTemplate{" + "id=" + id + ", applicationInterface=" + applicationInterface + ", initialInputs="
                + initialInputs + ", parserConnections=" + parserConnections + ", gatewayId=" + gatewayId + "}";
    }
}

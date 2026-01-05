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
 * Domain model: ParserConnector
 */
public class ParserConnector {
    private String id;
    private String parentParserId;
    private String childParserId;
    private List<ParserConnectorInput> connectorInputs;
    private String parsingTemplateId;

    public ParserConnector() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentParserId() {
        return parentParserId;
    }

    public void setParentParserId(String parentParserId) {
        this.parentParserId = parentParserId;
    }

    public String getChildParserId() {
        return childParserId;
    }

    public void setChildParserId(String childParserId) {
        this.childParserId = childParserId;
    }

    public List<ParserConnectorInput> getConnectorInputs() {
        return connectorInputs;
    }

    public void setConnectorInputs(List<ParserConnectorInput> connectorInputs) {
        this.connectorInputs = connectorInputs;
    }

    public String getParsingTemplateId() {
        return parsingTemplateId;
    }

    public void setParsingTemplateId(String parsingTemplateId) {
        this.parsingTemplateId = parsingTemplateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParserConnector that = (ParserConnector) o;
        return Objects.equals(id, that.id)
                && Objects.equals(parentParserId, that.parentParserId)
                && Objects.equals(childParserId, that.childParserId)
                && Objects.equals(connectorInputs, that.connectorInputs)
                && Objects.equals(parsingTemplateId, that.parsingTemplateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentParserId, childParserId, connectorInputs, parsingTemplateId);
    }

    @Override
    public String toString() {
        return "ParserConnector{" + "id=" + id + ", parentParserId=" + parentParserId + ", childParserId="
                + childParserId + ", connectorInputs=" + connectorInputs + ", parsingTemplateId=" + parsingTemplateId
                + "}";
    }
}

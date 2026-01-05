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

import java.util.Objects;

/**
 * Domain model: ParserConnectorInput
 */
public class ParserConnectorInput {
    private String id;
    private String inputId;
    private String parentOutputId;
    private String value;
    private String parserConnectorId;

    public ParserConnectorInput() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public String getParentOutputId() {
        return parentOutputId;
    }

    public void setParentOutputId(String parentOutputId) {
        this.parentOutputId = parentOutputId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParserConnectorId() {
        return parserConnectorId;
    }

    public void setParserConnectorId(String parserConnectorId) {
        this.parserConnectorId = parserConnectorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParserConnectorInput that = (ParserConnectorInput) o;
        return Objects.equals(id, that.id)
                && Objects.equals(inputId, that.inputId)
                && Objects.equals(parentOutputId, that.parentOutputId)
                && Objects.equals(value, that.value)
                && Objects.equals(parserConnectorId, that.parserConnectorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, inputId, parentOutputId, value, parserConnectorId);
    }

    @Override
    public String toString() {
        return "ParserConnectorInput{" + "id=" + id + ", inputId=" + inputId + ", parentOutputId=" + parentOutputId
                + ", value=" + value + ", parserConnectorId=" + parserConnectorId + "}";
    }
}

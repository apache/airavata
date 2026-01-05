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
 * Domain model: ParsingTemplateInput
 */
public class ParsingTemplateInput {
    private String id;
    private String targetInputId;
    private String applicationOutputName;
    private String value;
    private String parsingTemplateId;

    public ParsingTemplateInput() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetInputId() {
        return targetInputId;
    }

    public void setTargetInputId(String targetInputId) {
        this.targetInputId = targetInputId;
    }

    public String getApplicationOutputName() {
        return applicationOutputName;
    }

    public void setApplicationOutputName(String applicationOutputName) {
        this.applicationOutputName = applicationOutputName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        ParsingTemplateInput that = (ParsingTemplateInput) o;
        return Objects.equals(id, that.id)
                && Objects.equals(targetInputId, that.targetInputId)
                && Objects.equals(applicationOutputName, that.applicationOutputName)
                && Objects.equals(value, that.value)
                && Objects.equals(parsingTemplateId, that.parsingTemplateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, targetInputId, applicationOutputName, value, parsingTemplateId);
    }

    @Override
    public String toString() {
        return "ParsingTemplateInput{" + "id=" + id + ", targetInputId=" + targetInputId + ", applicationOutputName="
                + applicationOutputName + ", value=" + value + ", parsingTemplateId=" + parsingTemplateId + "}";
    }
}

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
 * Domain model: ParserOutput
 */
public class ParserOutput {
    private String id;
    private String name;
    private boolean requiredOutput;
    private String parserId;
    private IOType type;

    public ParserOutput() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getRequiredOutput() {
        return requiredOutput;
    }

    public void setRequiredOutput(boolean requiredOutput) {
        this.requiredOutput = requiredOutput;
    }

    public String getParserId() {
        return parserId;
    }

    public void setParserId(String parserId) {
        this.parserId = parserId;
    }

    public IOType getType() {
        return type;
    }

    public void setType(IOType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParserOutput that = (ParserOutput) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(requiredOutput, that.requiredOutput)
                && Objects.equals(parserId, that.parserId)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, requiredOutput, parserId, type);
    }

    @Override
    public String toString() {
        return "ParserOutput{" + "id=" + id + ", name=" + name + ", requiredOutput=" + requiredOutput + ", parserId="
                + parserId + ", type=" + type + "}";
    }
}

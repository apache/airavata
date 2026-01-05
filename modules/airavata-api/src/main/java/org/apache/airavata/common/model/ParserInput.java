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
 * Domain model: ParserInput
 */
public class ParserInput {
    private String id;
    private String name;
    private boolean requiredInput;
    private String parserId;
    private IOType type;

    public ParserInput() {}

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

    public boolean getRequiredInput() {
        return requiredInput;
    }

    public void setRequiredInput(boolean requiredInput) {
        this.requiredInput = requiredInput;
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
        ParserInput that = (ParserInput) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(requiredInput, that.requiredInput)
                && Objects.equals(parserId, that.parserId)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, requiredInput, parserId, type);
    }

    @Override
    public String toString() {
        return "ParserInput{" + "id=" + id + ", name=" + name + ", requiredInput=" + requiredInput + ", parserId="
                + parserId + ", type=" + type + "}";
    }
}

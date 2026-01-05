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
 * Domain model: SSHAccountProvisionerConfigParam
 */
public class SSHAccountProvisionerConfigParam {
    private String name;
    private SSHAccountProvisionerConfigParamType type;
    private boolean isOptional;
    private String description;

    public SSHAccountProvisionerConfigParam() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SSHAccountProvisionerConfigParamType getType() {
        return type;
    }

    public void setType(SSHAccountProvisionerConfigParamType type) {
        this.type = type;
    }

    public boolean getIsOptional() {
        return isOptional;
    }

    public void setIsOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSHAccountProvisionerConfigParam that = (SSHAccountProvisionerConfigParam) o;
        return Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(isOptional, that.isOptional)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, isOptional, description);
    }

    @Override
    public String toString() {
        return "SSHAccountProvisionerConfigParam{" + "name=" + name + ", type=" + type + ", isOptional=" + isOptional
                + ", description=" + description + "}";
    }
}

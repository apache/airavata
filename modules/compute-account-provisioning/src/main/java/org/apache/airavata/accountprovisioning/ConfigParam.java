/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.accountprovisioning;

public class ConfigParam {

    public enum ConfigParamType {
        STRING,
        CRED_STORE_PASSWORD_TOKEN,
    }

    private boolean optional = false;
    private String name;
    private String description;
    private ConfigParamType type = ConfigParamType.STRING;

    public ConfigParam(String name) {
        this.name = name;
    }

    public boolean isOptional() {
        return optional;
    }

    public ConfigParam setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public String getName() {
        return name;
    }

    public ConfigParam setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ConfigParam setDescription(String description) {
        this.description = description;
        return this;
    }

    public ConfigParamType getType() {
        return type;
    }

    public ConfigParam setType(ConfigParamType type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigParam)) return false;

        ConfigParam that = (ConfigParam) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

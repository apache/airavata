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
 * Domain model: SetEnvPaths
 */
public class SetEnvPaths {
    private String name;
    private String value;
    private int envPathOrder;

    public SetEnvPaths() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getEnvPathOrder() {
        return envPathOrder;
    }

    public void setEnvPathOrder(int envPathOrder) {
        this.envPathOrder = envPathOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetEnvPaths that = (SetEnvPaths) o;
        return Objects.equals(name, that.name)
                && Objects.equals(value, that.value)
                && Objects.equals(envPathOrder, that.envPathOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, envPathOrder);
    }

    @Override
    public String toString() {
        return "SetEnvPaths{" + "name=" + name + ", value=" + value + ", envPathOrder=" + envPathOrder + "}";
    }
}

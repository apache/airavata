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
package org.apache.airavata.research.application.model;

import java.util.Objects;

/**
 * Domain model: ApplicationField
 * Describes a single input or output field of an {@link Application}.
 * The {@code type} is a string token (e.g., {@code "STRING"}, {@code "INTEGER"},
 * {@code "FILE"}) interpreted by the workflow engine and front-end form builder.
 */
public class ApplicationField {
    private String name;
    /** Data type token (e.g., "STRING", "INTEGER", "FLOAT", "FILE", "URI"). */
    private String type;

    private String description;
    private boolean required;
    /** Default value expressed as a string regardless of the field's {@code type}. */
    private String defaultValue;

    public ApplicationField() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationField that = (ApplicationField) o;
        return required == that.required
                && Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(description, that.description)
                && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, description, required, defaultValue);
    }

    @Override
    public String toString() {
        return "ApplicationField{" + "name=" + name + ", type=" + type + ", description=" + description + ", required="
                + required + ", defaultValue=" + defaultValue + "}";
    }
}

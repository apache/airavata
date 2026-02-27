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
package org.apache.airavata.compute.resource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.compute.resource.model.PreferenceLevel;
import org.apache.airavata.compute.resource.model.PreferenceResourceType;
import org.apache.airavata.compute.resource.model.PreferenceValueType;

/**
 * JPA entity for the RESOURCE_PREFERENCE table.
 *
 * <p>Stores multi-level, multi-resource preferences for gateways, groups, and users.
 * The tuple (resourceType, resourceId, ownerId, level, key) uniquely identifies a preference value.
 */
@Entity
@Table(name = "resource_preference")
public class ResourcePreferenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 64)
    private PreferenceResourceType resourceType;

    @Column(name = "resource_id", nullable = false, length = 512)
    private String resourceId;

    @Column(name = "owner_id", nullable = false, length = 512)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_level", nullable = false, length = 32)
    private PreferenceLevel level;

    @Column(name = "pref_key", nullable = false, length = 255)
    private String key;

    @Column(name = "pref_value", columnDefinition = "MEDIUMTEXT")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 32)
    private PreferenceValueType valueType;

    @Column(name = "enforced", nullable = false)
    private boolean enforced;

    public ResourcePreferenceEntity() {}

    public Long getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }

    public PreferenceResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(PreferenceResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public PreferenceLevel getLevel() {
        return level;
    }

    public void setLevel(PreferenceLevel level) {
        this.level = level;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PreferenceValueType getValueType() {
        return valueType;
    }

    public void setValueType(PreferenceValueType valueType) {
        this.valueType = valueType;
    }

    public boolean isEnforced() {
        return enforced;
    }

    public void setEnforced(boolean enforced) {
        this.enforced = enforced;
    }
}

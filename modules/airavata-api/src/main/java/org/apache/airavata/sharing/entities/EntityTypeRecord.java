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
package org.apache.airavata.sharing.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.airavata.registry.entities.GatewayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA entity representing an entity type in the sharing registry (table ENTITY_TYPE).
 * The domainId references the GatewayEntity's gatewayId (formerly via DomainEntity).
 */
@Entity
@Table(name = "ENTITY_TYPE", schema = "")
@IdClass(EntityTypePK.class)
public class EntityTypeRecord {
    private static final Logger logger = LoggerFactory.getLogger(EntityTypeRecord.class);
    private String entityTypeId;
    private String domainId;
    private GatewayEntity gateway;
    private String name;
    private String description;
    private Long createdTime;
    private Long updatedTime;

    @Id
    @Column(name = "ENTITY_TYPE_ID", nullable = false)
    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    @Id
    @Column(name = "DOMAIN_ID", nullable = false)
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    /**
     * Returns the associated gateway for this entity type's domain.
     * The domainId references the gateway's gatewayId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOMAIN_ID", referencedColumnName = "GATEWAY_ID", insertable = false, updatable = false)
    public GatewayEntity getGateway() {
        return gateway;
    }

    public void setGateway(GatewayEntity gateway) {
        this.gateway = gateway;
    }

    @Basic
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "CREATED_TIME", nullable = false)
    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    @Basic
    @Column(name = "UPDATED_TIME", nullable = false)
    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityTypeRecord that = (EntityTypeRecord) o;

        if (getEntityTypeId() != null
                ? !getEntityTypeId().equals(that.getEntityTypeId())
                : that.getEntityTypeId() != null) return false;
        if (getDomainId() != null ? !getDomainId().equals(that.getDomainId()) : that.getDomainId() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getCreatedTime() != null ? !getCreatedTime().equals(that.getCreatedTime()) : that.getCreatedTime() != null)
            return false;
        if (getUpdatedTime() != null ? !getUpdatedTime().equals(that.getUpdatedTime()) : that.getUpdatedTime() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getEntityTypeId() != null ? getEntityTypeId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getCreatedTime() != null ? getCreatedTime().hashCode() : 0);
        result = 31 * result + (getUpdatedTime() != null ? getUpdatedTime().hashCode() : 0);
        return result;
    }
}

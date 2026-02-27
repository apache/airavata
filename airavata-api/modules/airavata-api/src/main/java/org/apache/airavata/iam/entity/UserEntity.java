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
package org.apache.airavata.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import org.apache.airavata.gateway.entity.GatewayEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * User entity linking an OIDC subject to a gateway.
 *
 * <p>Stores the OIDC subject identifier (sub), gateway association, and optional
 * profile fields (givenName, familyName, email). Profile fields are populated from
 * the identity provider (Keycloak) at runtime and are not persisted to the database.
 *
 * <p>Primary key format: {@code sub@gatewayId}
 */
@Entity(name = "UserEntity")
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "user",
        indexes = {
            @Index(name = "idx_user_sub", columnList = "sub"),
            @Index(name = "idx_user_gateway_id", columnList = "gateway_id"),
            @Index(name = "idx_user_sub_gateway", columnList = "sub, gateway_id")
        })
public class UserEntity {

    @Id
    @Column(name = "user_id", nullable = false, length = 512)
    private String userId;

    @Column(name = "sub", nullable = false, length = 255)
    private String sub;

    @Column(name = "gateway_id", nullable = false, length = 255)
    private String gatewayId;

    @Column(name = "personal_group_id", length = 512)
    private String personalGroupId;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(targetEntity = GatewayEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "gateway_id",
            referencedColumnName = "gateway_id",
            nullable = false,
            insertable = false,
            updatable = false)
    private GatewayEntity gateway;

    public UserEntity() {}

    public UserEntity(String sub, String gatewayId) {
        this.sub = sub;
        this.gatewayId = gatewayId;
        this.userId = createUserId(sub, gatewayId);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getPersonalGroupId() {
        return personalGroupId;
    }

    public void setPersonalGroupId(String personalGroupId) {
        this.personalGroupId = personalGroupId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public GatewayEntity getGateway() {
        return gateway;
    }

    public void setGateway(GatewayEntity gateway) {
        this.gateway = gateway;
    }

    @PrePersist
    void onCreate() {
        if (this.userId == null && this.sub != null && this.gatewayId != null) {
            this.userId = createUserId(this.sub, this.gatewayId);
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public static String createUserId(String sub, String gatewayId) {
        return sub + "@" + gatewayId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserEntity{userId='" + userId + "', sub='" + sub + "', gatewayId='" + gatewayId + "'}";
    }
}

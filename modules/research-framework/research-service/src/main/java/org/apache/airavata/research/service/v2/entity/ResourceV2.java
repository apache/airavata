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
package org.apache.airavata.research.service.v2.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.apache.airavata.research.service.v2.enums.PrivacyEnumV2;
import org.apache.airavata.research.service.v2.enums.ResourceTypeEnumV2;
import org.apache.airavata.research.service.v2.enums.StateEnumV2;
import org.apache.airavata.research.service.v2.enums.StatusEnumV2;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "RESOURCE_V2")
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditingEntityListener.class)
public abstract class ResourceV2 {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false, length = 48)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String headerImage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resource_v2_authors", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "author_id")
    private Set<String> authors = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "resource_v2_tags",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<TagV2> tags = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEnumV2 status = StatusEnumV2.NONE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StateEnumV2 state = StateEnumV2.ACTIVE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrivacyEnumV2 privacy = PrivacyEnumV2.PUBLIC;

    @Column(nullable = false)
    private Integer starCount = 0;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    public abstract ResourceTypeEnumV2 getType();

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<String> authors) {
        this.authors = authors;
    }

    public Set<TagV2> getTags() {
        return tags;
    }

    public void setTags(Set<TagV2> tags) {
        this.tags = tags;
    }

    public StatusEnumV2 getStatus() {
        return status;
    }

    public void setStatus(StatusEnumV2 status) {
        this.status = status;
    }

    public StateEnumV2 getState() {
        return state;
    }

    public void setState(StateEnumV2 state) {
        this.state = state;
    }

    public PrivacyEnumV2 getPrivacy() {
        return privacy;
    }

    public void setPrivacy(PrivacyEnumV2 privacy) {
        this.privacy = privacy;
    }

    public Integer getStarCount() {
        return starCount;
    }

    public void setStarCount(Integer starCount) {
        this.starCount = starCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
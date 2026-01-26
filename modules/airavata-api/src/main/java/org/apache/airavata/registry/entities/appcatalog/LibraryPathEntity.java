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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.common.model.LibraryPathType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Unified entity for library path configurations in application deployments.
 *
 * <p>This entity consolidates the following legacy entities:
 * <ul>
 *   <li>LibraryPrependPathEntity - Paths prepended to library search path</li>
 *   <li>LibraryApendPathEntity - Paths appended to library search path</li>
 * </ul>
 *
 * <p>The pathType discriminator identifies whether the path should be prepended
 * or appended to the library search path (e.g., LD_LIBRARY_PATH on Linux).
 *
 * @see LibraryPathType
 * @see LibraryPathEntityPK
 */
@Entity(name = "LibraryPathEntity")
@Table(
        name = "LIBRARY_PATH",
        indexes = {
            @Index(name = "idx_lib_path_deployment", columnList = "DEPLOYMENT_ID"),
            @Index(name = "idx_lib_path_type", columnList = "PATH_TYPE"),
            @Index(name = "idx_lib_path_deployment_type", columnList = "DEPLOYMENT_ID, PATH_TYPE")
        })
@IdClass(LibraryPathEntityPK.class)
public class LibraryPathEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // ============================================
    // PRIMARY KEY FIELDS
    // ============================================

    @Id
    @Column(name = "DEPLOYMENT_ID", nullable = false)
    private String deploymentId;

    @Id
    @Column(name = "PATH_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private LibraryPathType pathType;

    @Id
    @Column(name = "NAME", nullable = false)
    private String name;

    // ============================================
    // DATA FIELDS
    // ============================================

    @Column(name = "PATH_VALUE")
    private String value;

    // ============================================
    // RELATIONSHIPS
    // ============================================

    @ManyToOne(targetEntity = ApplicationDeploymentEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "DEPLOYMENT_ID", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ApplicationDeploymentEntity applicationDeployment;

    public LibraryPathEntity() {}

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public LibraryPathType getPathType() {
        return pathType;
    }

    public void setPathType(LibraryPathType pathType) {
        this.pathType = pathType;
    }

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

    public ApplicationDeploymentEntity getApplicationDeployment() {
        return applicationDeployment;
    }

    public void setApplicationDeployment(ApplicationDeploymentEntity applicationDeployment) {
        this.applicationDeployment = applicationDeployment;
    }
}

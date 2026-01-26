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

import java.io.Serializable;
import java.util.Objects;
import org.apache.airavata.common.model.LibraryPathType;

/**
 * Composite primary key class for the LIBRARY_PATH table.
 *
 * <p>The key consists of:
 * <ul>
 *   <li>deploymentId - the application deployment this path belongs to</li>
 *   <li>pathType - the type of path (PREPEND or APPEND)</li>
 *   <li>name - the environment variable name (e.g., LD_LIBRARY_PATH)</li>
 * </ul>
 */
public class LibraryPathEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String deploymentId;
    private LibraryPathType pathType;
    private String name;

    public LibraryPathEntityPK() {}

    public LibraryPathEntityPK(String deploymentId, LibraryPathType pathType, String name) {
        this.deploymentId = deploymentId;
        this.pathType = pathType;
        this.name = name;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryPathEntityPK that = (LibraryPathEntityPK) o;
        return Objects.equals(deploymentId, that.deploymentId)
                && pathType == that.pathType
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, pathType, name);
    }

    @Override
    public String toString() {
        return "LibraryPathEntityPK{"
                + "deploymentId='" + deploymentId + '\''
                + ", pathType=" + pathType
                + ", name='" + name + '\''
                + '}';
    }
}

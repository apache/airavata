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
package org.apache.airavata.registry.entities.expcatalog;

import java.io.Serializable;
import java.util.Objects;

/**
 * The primary key class for the project_user database table.
 */
public class ProjectUserPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String projectID;
    private String userName;

    public ProjectUserPK() {}

    public ProjectUserPK(String projectID, String userName) {
        this.projectID = projectID;
        this.userName = userName;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectUserPK that = (ProjectUserPK) o;
        return Objects.equals(projectID, that.projectID)
                && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectID, userName);
    }

    @Override
    public String toString() {
        return "ProjectUserPK{"
                + "projectID='" + projectID + '\''
                + ", userName='" + userName + '\''
                + '}';
    }
}

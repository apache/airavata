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
package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;

/**
 * The primary key class for the group_compute_resource_preference database table.
 */
public class GroupComputeResourcePrefPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String computeResourceId;
    private String groupResourceProfileId;

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupComputeResourcePrefPK that = (GroupComputeResourcePrefPK) o;

        if (computeResourceId != null ? !computeResourceId.equals(that.computeResourceId) : that.computeResourceId != null)
            return false;
        return groupResourceProfileId != null ? groupResourceProfileId.equals(that.groupResourceProfileId) : that.groupResourceProfileId == null;
    }

    @Override
    public int hashCode() {
        int result = computeResourceId != null ? computeResourceId.hashCode() : 0;
        result = 31 * result + (groupResourceProfileId != null ? groupResourceProfileId.hashCode() : 0);
        return result;
    }
}

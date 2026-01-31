/**
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

/**
 * Composite primary key for {@link AllocationPoolGroupEntity}.
 */
public class AllocationPoolGroupPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String allocationPoolId;
    private String groupResourceProfileId;

    public AllocationPoolGroupPK() {}

    public AllocationPoolGroupPK(String allocationPoolId, String groupResourceProfileId) {
        this.allocationPoolId = allocationPoolId;
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getAllocationPoolId() {
        return allocationPoolId;
    }

    public void setAllocationPoolId(String allocationPoolId) {
        this.allocationPoolId = allocationPoolId;
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
        AllocationPoolGroupPK that = (AllocationPoolGroupPK) o;
        return Objects.equals(allocationPoolId, that.allocationPoolId)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocationPoolId, groupResourceProfileId);
    }
}

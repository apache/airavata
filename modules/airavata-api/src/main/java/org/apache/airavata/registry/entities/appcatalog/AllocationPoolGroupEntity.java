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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Join entity linking an allocation pool to a group resource profile.
 * Order is used to present member groups in a stable order (e.g. for selection).
 */
@Entity
@Table(name = "ALLOCATION_POOL_GROUP")
@IdClass(AllocationPoolGroupPK.class)
public class AllocationPoolGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ALLOCATION_POOL_ID", nullable = false, length = 255)
    private String allocationPoolId;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID", nullable = false, length = 255)
    private String groupResourceProfileId;

    @Column(name = "DISPLAY_ORDER", nullable = false)
    private int displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ALLOCATION_POOL_ID", insertable = false, updatable = false)
    private AllocationPoolEntity allocationPool;

    public AllocationPoolGroupEntity() {}

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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public AllocationPoolEntity getAllocationPool() {
        return allocationPool;
    }

    public void setAllocationPool(AllocationPoolEntity allocationPool) {
        this.allocationPool = allocationPool;
    }
}

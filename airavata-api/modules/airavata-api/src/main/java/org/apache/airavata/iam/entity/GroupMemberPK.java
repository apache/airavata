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
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for GROUP_MEMBER table.
 * Identifies a unique membership: (parent group, child user/group, domain).
 */
public class GroupMemberPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String parentId;
    private String childId;
    private String domainId;

    public GroupMemberPK() {}

    public GroupMemberPK(String parentId, String childId, String domainId) {
        this.parentId = parentId;
        this.childId = childId;
        this.domainId = domainId;
    }

    @Column(name = "parent_id")
    @Id
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Column(name = "child_id")
    @Id
    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    @Column(name = "domain_id")
    @Id
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMemberPK that = (GroupMemberPK) o;
        return Objects.equals(parentId, that.parentId)
                && Objects.equals(childId, that.childId)
                && Objects.equals(domainId, that.domainId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, childId, domainId);
    }

    @Override
    public String toString() {
        return "GroupMemberPK{" + "parentId='"
                + parentId + '\'' + ", childId='"
                + childId + '\'' + ", domainId='"
                + domainId + '\'' + '}';
    }
}

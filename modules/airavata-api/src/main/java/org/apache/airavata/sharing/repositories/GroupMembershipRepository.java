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
package org.apache.airavata.sharing.repositories;

import java.util.List;
import org.apache.airavata.sharing.entities.GroupMembershipEntity;
import org.apache.airavata.sharing.entities.GroupMembershipPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembershipEntity, GroupMembershipPK> {

    @Query("SELECT gm FROM GroupMembershipEntity gm WHERE gm.domainId = :domainId AND gm.childId = :childId")
    List<GroupMembershipEntity> findByDomainIdAndChildId(
            @Param("domainId") String domainId, @Param("childId") String childId);

    @Query("SELECT gm FROM GroupMembershipEntity gm WHERE gm.domainId = :domainId AND gm.parentId = :parentId")
    List<GroupMembershipEntity> findByDomainIdAndParentId(
            @Param("domainId") String domainId, @Param("parentId") String parentId);

    // Note: Complex query methods (getAllChildUsers, getAllChildGroups, getAllMemberGroupsForUser,
    // getAllParentMembershipsForChild) with joins should be implemented in a service class
}

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
import org.apache.airavata.sharing.entities.EntityEntity;
import org.apache.airavata.sharing.entities.EntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityRepository extends JpaRepository<EntityEntity, EntityPK> {

    @Query(
            "SELECT e FROM EntityEntity e WHERE e.domainId = :domainId AND e.parentEntityId = :parentId ORDER BY e.originalEntityCreationTime DESC")
    List<EntityEntity> findByDomainIdAndParentEntityIdOrderByOriginalEntityCreationTimeDesc(
            @Param("domainId") String domainId, @Param("parentId") String parentId);

    // Note: searchEntities method with complex dynamic queries should be implemented
    // as a custom repository implementation using Specifications or Criteria API
    // to avoid SQL injection vulnerabilities
}

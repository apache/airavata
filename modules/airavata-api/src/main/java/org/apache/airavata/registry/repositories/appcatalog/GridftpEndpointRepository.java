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
package org.apache.airavata.registry.repositories.appcatalog;

import java.util.List;
import org.apache.airavata.registry.entities.appcatalog.GridftpEndpointEntity;
import org.apache.airavata.registry.entities.appcatalog.GridftpEndpointPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for GridFTP endpoint entities.
 */
@Repository
public interface GridftpEndpointRepository extends JpaRepository<GridftpEndpointEntity, GridftpEndpointPK> {

    /**
     * Find all endpoints for a specific data movement interface.
     */
    @Query("SELECT e FROM GridftpEndpointEntity e WHERE e.dataMovementInterfaceId = :dataMovementInterfaceId")
    List<GridftpEndpointEntity> findByDataMovementInterfaceId(
            @Param("dataMovementInterfaceId") String dataMovementInterfaceId);

    /**
     * Delete all endpoints for a specific data movement interface.
     */
    void deleteByDataMovementInterfaceId(String dataMovementInterfaceId);
}

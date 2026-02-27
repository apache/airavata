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
package org.apache.airavata.research.application.repository;

import java.util.List;
import org.apache.airavata.research.application.entity.ApplicationInstallationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ApplicationInstallationEntity}.
 *
 * <p>Provides query methods for tracking the deployment of applications onto compute
 * resources. All finder methods use Spring Data JPA method naming conventions for automatic
 * query derivation.
 */
@Repository
public interface ApplicationInstallationRepository extends JpaRepository<ApplicationInstallationEntity, String> {

    /**
     * Find all installation records for a specific application across all resources.
     *
     * @param applicationId the application identifier
     * @return list of installation records for the application, empty list if none found
     */
    List<ApplicationInstallationEntity> findByApplicationId(String applicationId);
}

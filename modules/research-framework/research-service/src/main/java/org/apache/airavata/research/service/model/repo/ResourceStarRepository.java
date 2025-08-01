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
package org.apache.airavata.research.service.model.repo;

import java.util.List;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.model.entity.Resource;
import org.apache.airavata.research.service.model.entity.ResourceStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceStarRepository extends JpaRepository<ResourceStar, String> {

    boolean existsByResourceAndUserId(Resource resource, String ownerId);

    List<ResourceStar> findByResourceAndUserId(Resource resource, String ownerId);

    List<ResourceStar> findByUserIdAndResourceState(String loggedInUser, StateEnum stateEnum);

    long countResourceStarByResource(Resource resource);
}

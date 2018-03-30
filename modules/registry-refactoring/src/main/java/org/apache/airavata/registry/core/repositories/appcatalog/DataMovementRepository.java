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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.DataMovementInterfaceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.DataMovementInterfacePK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.dozer.Mapper;

public class DataMovementRepository extends AppCatAbstractRepository<DataMovementInterface, DataMovementInterfaceEntity, DataMovementInterfacePK> {

    public DataMovementRepository() {
        super(DataMovementInterface.class, DataMovementInterfaceEntity.class);
    }

    public String addDataMovementProtocol(String resourceId, DataMovementInterface dataMovementInterface) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        DataMovementInterfaceEntity dataMovementInterfaceEntity = mapper.map(dataMovementInterface, DataMovementInterfaceEntity.class);
        ComputeResourceEntity computeResourceEntity = mapper.map(new ComputeResourceRepository().get(resourceId), ComputeResourceEntity.class);
        dataMovementInterfaceEntity.setComputeResource(computeResourceEntity);
        dataMovementInterfaceEntity.setComputeResourceId(resourceId);
        execute(entityManager -> entityManager.merge(dataMovementInterfaceEntity));
        return dataMovementInterfaceEntity.getDataMovementInterfaceId();
    }
}

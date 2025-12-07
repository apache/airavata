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
package org.apache.airavata.registry.services;

import com.github.dozermapper.core.Mapper;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.exceptions.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.replicacatalog.DataReplicaLocationRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataReplicaLocationService {
    @Autowired
    private DataReplicaLocationRepository dataReplicaLocationRepository;

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws ReplicaCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        DataReplicaLocationEntity entity = mapper.map(replicaLocationModel, DataReplicaLocationEntity.class);
        DataReplicaLocationEntity saved = dataReplicaLocationRepository.save(entity);
        return saved.getReplicaId();
    }

    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException {
        DataReplicaLocationEntity entity =
                dataReplicaLocationRepository.findById(replicaId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, DataReplicaLocationModel.class);
    }

    public List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws ReplicaCatalogException {
        List<DataReplicaLocationEntity> entities = dataReplicaLocationRepository.findByProductUri(productUri);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream()
                .map(e -> mapper.map(e, DataReplicaLocationModel.class))
                .collect(Collectors.toList());
    }

    public boolean updateReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws ReplicaCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        DataReplicaLocationEntity entity = mapper.map(replicaLocationModel, DataReplicaLocationEntity.class);
        dataReplicaLocationRepository.save(entity);
        return true;
    }

    public void removeReplicaLocation(String replicaId) throws ReplicaCatalogException {
        dataReplicaLocationRepository.deleteById(replicaId);
    }
}

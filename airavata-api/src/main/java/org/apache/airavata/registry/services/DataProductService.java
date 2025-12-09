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
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.exceptions.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.replicacatalog.DataProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataProductService {
    private final DataProductRepository dataProductRepository;
    private final Mapper mapper;

    public DataProductService(DataProductRepository dataProductRepository, Mapper mapper) {
        this.dataProductRepository = dataProductRepository;
        this.mapper = mapper;
    }

    public DataProductModel getDataProduct(String productUri) throws ReplicaCatalogException {
        DataProductEntity entity = dataProductRepository.findById(productUri).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, DataProductModel.class);
    }

    public DataProductModel getParentDataProduct(String productUri) throws ReplicaCatalogException {
        DataProductEntity entity = dataProductRepository.findById(productUri).orElse(null);
        if (entity == null || entity.getParentProductUri() == null) return null;
        DataProductEntity parentEntity =
                dataProductRepository.findById(entity.getParentProductUri()).orElse(null);
        if (parentEntity == null) return null;
        return mapper.map(parentEntity, DataProductModel.class);
    }

    public List<DataProductModel> getChildDataProducts(String productUri) throws ReplicaCatalogException {
        List<DataProductEntity> entities = dataProductRepository.findByParentProductUri(productUri);
        return entities.stream().map(e -> mapper.map(e, DataProductModel.class)).collect(Collectors.toList());
    }

    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws ReplicaCatalogException {
        String searchPattern = "%" + productName + "%";
        List<DataProductEntity> entities =
                dataProductRepository.findByGatewayIdAndOwnerNameAndProductNameLike(gatewayId, userId, searchPattern);
        return entities.stream().map(e -> mapper.map(e, DataProductModel.class)).collect(Collectors.toList());
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws ReplicaCatalogException {
        DataProductEntity entity = mapper.map(dataProductModel, DataProductEntity.class);
        DataProductEntity saved = dataProductRepository.save(entity);
        return saved.getProductUri();
    }

    public boolean isDataProductExists(String productUri) throws ReplicaCatalogException {
        return dataProductRepository.existsById(productUri);
    }

    public boolean updateDataProduct(DataProductModel dataProductModel) throws ReplicaCatalogException {
        DataProductEntity entity = mapper.map(dataProductModel, DataProductEntity.class);
        dataProductRepository.save(entity);
        return true;
    }

    public void removeDataProduct(String productUri) throws ReplicaCatalogException {
        dataProductRepository.deleteById(productUri);
    }
}

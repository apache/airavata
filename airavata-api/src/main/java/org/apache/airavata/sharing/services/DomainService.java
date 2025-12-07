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
package org.apache.airavata.sharing.services;

import com.github.dozermapper.core.Mapper;
import java.util.List;
import org.apache.airavata.sharing.entities.DomainEntity;
import org.apache.airavata.sharing.models.Domain;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.repositories.DomainRepository;
import org.apache.airavata.sharing.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DomainService {
    @Autowired
    private DomainRepository domainRepository;

    public Domain get(String domainId) throws SharingRegistryException {
        DomainEntity entity = domainRepository.findById(domainId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, Domain.class);
    }

    public Domain create(Domain domain) throws SharingRegistryException {
        return update(domain);
    }

    public Domain update(Domain domain) throws SharingRegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        DomainEntity entity = mapper.map(domain, DomainEntity.class);
        DomainEntity saved = domainRepository.save(entity);
        return mapper.map(saved, Domain.class);
    }

    public boolean delete(String domainId) throws SharingRegistryException {
        domainRepository.deleteById(domainId);
        return true;
    }

    public boolean isExists(String domainId) throws SharingRegistryException {
        return domainRepository.existsById(domainId);
    }

    public List<Domain> getAll() throws SharingRegistryException {
        List<DomainEntity> entities = domainRepository.findAll();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, Domain.class)).toList();
    }
}

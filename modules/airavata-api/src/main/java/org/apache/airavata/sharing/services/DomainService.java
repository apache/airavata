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

import java.util.List;
import org.apache.airavata.sharing.mappers.DomainMapper;
import org.apache.airavata.sharing.model.Domain;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.repositories.DomainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DomainService {
    private final DomainRepository domainRepository;
    private final DomainMapper domainMapper;

    public DomainService(DomainRepository domainRepository, DomainMapper domainMapper) {
        this.domainRepository = domainRepository;
        this.domainMapper = domainMapper;
    }

    public Domain get(String domainId) throws SharingRegistryException {
        var entity = domainRepository.findById(domainId).orElse(null);
        if (entity == null) return null;
        return domainMapper.toModel(entity);
    }

    public Domain create(Domain domain) throws SharingRegistryException {
        return update(domain);
    }

    public Domain update(Domain domain) throws SharingRegistryException {
        var entity = domainMapper.toEntity(domain);
        var saved = domainRepository.save(entity);
        return domainMapper.toModel(saved);
    }

    public boolean delete(String domainId) throws SharingRegistryException {
        domainRepository.deleteById(domainId);
        return true;
    }

    public boolean isExists(String domainId) throws SharingRegistryException {
        return domainRepository.existsById(domainId);
    }

    public List<Domain> getAll() throws SharingRegistryException {
        var entities = domainRepository.findAll();
        return domainMapper.toModelList(entities);
    }
}

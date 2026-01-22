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

import org.apache.airavata.sharing.entities.GroupAdminPK;
import org.apache.airavata.sharing.mappers.GroupAdminMapper;
import org.apache.airavata.sharing.model.GroupAdmin;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.repositories.GroupAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupAdminService {
    private final GroupAdminRepository groupAdminRepository;
    private final GroupAdminMapper groupAdminMapper;

    public GroupAdminService(GroupAdminRepository groupAdminRepository, GroupAdminMapper groupAdminMapper) {
        this.groupAdminRepository = groupAdminRepository;
        this.groupAdminMapper = groupAdminMapper;
    }

    public GroupAdmin get(GroupAdminPK pk) throws SharingRegistryException {
        var entity = groupAdminRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return groupAdminMapper.toModel(entity);
    }

    public GroupAdmin create(GroupAdmin groupAdmin) throws SharingRegistryException {
        return update(groupAdmin);
    }

    public GroupAdmin update(GroupAdmin groupAdmin) throws SharingRegistryException {
        var entity = groupAdminMapper.toEntity(groupAdmin);
        var saved = groupAdminRepository.save(entity);
        return groupAdminMapper.toModel(saved);
    }

    public boolean delete(GroupAdminPK pk) throws SharingRegistryException {
        groupAdminRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(GroupAdminPK pk) throws SharingRegistryException {
        return groupAdminRepository.existsById(pk);
    }
}

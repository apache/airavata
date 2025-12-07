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
import org.apache.airavata.sharing.entities.GroupAdminEntity;
import org.apache.airavata.sharing.entities.GroupAdminPK;
import org.apache.airavata.sharing.models.GroupAdmin;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.repositories.GroupAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupAdminService {
    @Autowired
    private GroupAdminRepository groupAdminRepository;

    @Autowired
    private Mapper mapper;

    public GroupAdmin get(GroupAdminPK pk) throws SharingRegistryException {
        GroupAdminEntity entity = groupAdminRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, GroupAdmin.class);
    }

    public GroupAdmin create(GroupAdmin groupAdmin) throws SharingRegistryException {
        return update(groupAdmin);
    }

    public GroupAdmin update(GroupAdmin groupAdmin) throws SharingRegistryException {
        GroupAdminEntity entity = mapper.map(groupAdmin, GroupAdminEntity.class);
        GroupAdminEntity saved = groupAdminRepository.save(entity);
        return mapper.map(saved, GroupAdmin.class);
    }

    public boolean delete(GroupAdminPK pk) throws SharingRegistryException {
        groupAdminRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(GroupAdminPK pk) throws SharingRegistryException {
        return groupAdminRepository.existsById(pk);
    }
}

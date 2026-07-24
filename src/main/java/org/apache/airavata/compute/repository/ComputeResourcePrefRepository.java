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
package org.apache.airavata.compute.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.ComputeResourcePreferenceEntity;
import org.apache.airavata.compute.model.ComputeResourcePreferencePK;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.springframework.stereotype.Component;

@Component
public class ComputeResourcePrefRepository
        extends AbstractRepository<
                ComputeResourcePreference, ComputeResourcePreferenceEntity, ComputeResourcePreferencePK> {

    public ComputeResourcePrefRepository() {
        super(ComputeResourcePreference.class, ComputeResourcePreferenceEntity.class);
    }

    @Override
    protected ComputeResourcePreference toModel(ComputeResourcePreferenceEntity entity) {
        return ComputeMapper.INSTANCE.computeResourcePrefToModel(entity);
    }

    @Override
    protected ComputeResourcePreferenceEntity toEntity(ComputeResourcePreference model) {
        return ComputeMapper.INSTANCE.computeResourcePrefToEntity(model);
    }

    public Map<String, String> getsshAccountProvisionerConfig(String gatewayId, String hostId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(hostId);
        ComputeResourcePreferenceEntity computeResourcePreferenceEntity = execute(entityManager ->
                entityManager.find(ComputeResourcePreferenceEntity.class, computeResourcePreferencePK));
        List<Map<String, Object>> configs = computeResourcePreferenceEntity.getSshAccountProvisionerConfigurations();
        if (configs != null && !configs.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            for (Map<String, Object> config : configs) {
                String name = (String) config.get("configName");
                String value = (String) config.getOrDefault("configValue", "");
                if (name != null) result.put(name, value);
            }
            return result;
        }
        return null;
    }
}

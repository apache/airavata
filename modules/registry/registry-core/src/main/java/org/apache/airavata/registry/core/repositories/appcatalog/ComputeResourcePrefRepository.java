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

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferenceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferencePK;
import org.apache.airavata.registry.core.entities.appcatalog.SSHAccountProvisionerConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ComputeResourcePrefRepository extends AppCatAbstractRepository<ComputeResourcePreference, ComputeResourcePreferenceEntity, ComputeResourcePreferencePK> {

    public ComputeResourcePrefRepository() {
        super(ComputeResourcePreference.class, ComputeResourcePreferenceEntity.class);
    }

    public Map<String,String> getsshAccountProvisionerConfig(String gatewayId, String hostId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(hostId);
        ComputeResourcePreferenceEntity computeResourcePreferenceEntity = execute(entityManager -> entityManager
                .find(ComputeResourcePreferenceEntity.class, computeResourcePreferencePK));
        if (computeResourcePreferenceEntity.getSshAccountProvisionerConfigurations()!= null && !computeResourcePreferenceEntity.getSshAccountProvisionerConfigurations().isEmpty()){
            Map<String,String> sshAccountProvisionerConfigurations = new HashMap<>();
            for (SSHAccountProvisionerConfiguration config : computeResourcePreferenceEntity.getSshAccountProvisionerConfigurations()){
                sshAccountProvisionerConfigurations.put(config.getConfigName(), config.getConfigValue());
            }
            return sshAccountProvisionerConfigurations;
        }
        return null;
    }
}

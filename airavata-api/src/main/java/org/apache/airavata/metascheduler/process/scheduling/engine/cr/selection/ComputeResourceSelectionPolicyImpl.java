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
package org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection;

import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.registry.api.RegistryService;

public abstract class ComputeResourceSelectionPolicyImpl implements ComputeResourceSelectionPolicy {

    protected ThriftClientPool<RegistryService.Client> registryClientPool;

    public ComputeResourceSelectionPolicyImpl() {
        this.registryClientPool = Utils.getRegistryServiceClientPool();
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourcId, String groupResourceProfileId) throws Exception {
        RegistryService.Client client = this.registryClientPool.getResource();
        try {
            if (client.isGroupComputeResourcePreferenceExists(computeResourcId, groupResourceProfileId)) {
                return client.getGroupComputeResourcePreference(computeResourcId, groupResourceProfileId);
            }
            return null;
        } finally {
            this.registryClientPool.returnResource(client);
        }
    }
}

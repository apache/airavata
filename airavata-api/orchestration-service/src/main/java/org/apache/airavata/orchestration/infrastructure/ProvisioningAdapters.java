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
package org.apache.airavata.orchestration.infrastructure;

import java.util.EnumMap;
import java.util.Map;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;

public class ProvisioningAdapters {

    private static final Map<ResourceType, ProvisioningAdapter> ADAPTERS = new EnumMap<>(ResourceType.class);

    static {
        ADAPTERS.put(ResourceType.SLURM, new SlurmProvisioningAdapter());
        ADAPTERS.put(ResourceType.AWS, new AwsProvisioningAdapter());
    }

    public static ProvisioningAdapter getAdapter(ResourceType type) {
        ProvisioningAdapter adapter = ADAPTERS.get(type);
        if (adapter == null) {
            throw new IllegalArgumentException("No ProvisioningAdapter for " + type);
        }
        return adapter;
    }
}

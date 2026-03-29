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

import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.ComputeResourcePolicyEntity;
import org.apache.airavata.execution.util.AbstractRepository;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;

/**
 * Created by skariyat on 2/10/18.
 */
public class ComputeResourcePolicyRepository
        extends AbstractRepository<ComputeResourcePolicy, ComputeResourcePolicyEntity, String> {

    public ComputeResourcePolicyRepository() {
        super(ComputeResourcePolicy.class, ComputeResourcePolicyEntity.class);
    }

    @Override
    protected ComputeResourcePolicy toModel(ComputeResourcePolicyEntity entity) {
        return ComputeMapper.INSTANCE.computeResourcePolicyToModel(entity);
    }

    @Override
    protected ComputeResourcePolicyEntity toEntity(ComputeResourcePolicy model) {
        return ComputeMapper.INSTANCE.computeResourcePolicyToEntity(model);
    }
}

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
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourcePreferencePK;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefPK;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by skariyat on 2/10/18.
 */
public class GrpComputePrefRepository extends AppCatAbstractRepository<GroupComputeResourcePreference, GroupComputeResourcePrefEntity, GroupComputeResourcePrefPK> {

    public GrpComputePrefRepository() {
        super(GroupComputeResourcePreference.class, GroupComputeResourcePrefEntity.class);
    }

    public void validateGroupComputeResourcePreference(GroupComputeResourcePreference groupComputeResourcePreference, String gatewayId) {
        final String computeResourceId = groupComputeResourcePreference.getComputeResourceId();
        final ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
        ComputeResourcePreference computeResourcePreference = computeResourcePrefRepository.get(new ComputeResourcePreferencePK(gatewayId, computeResourceId));
        if (computeResourcePreference != null) {
            final String loginUserName = groupComputeResourcePreference.getLoginUserName();
            boolean loginUserNameDiffers = StringUtils.isNotBlank(loginUserName) && !loginUserName.equals(computeResourcePreference.getLoginUserName());
            boolean allocationProjectNumberNotBlankUsingSameLoginUserName = StringUtils.isNotBlank(groupComputeResourcePreference.getAllocationProjectNumber()) && StringUtils.isNotBlank(computeResourcePreference.getLoginUserName());
            // Either loginUserName must differ from the corresponding ComputeResourcePreference
            // or allocationProjectNumber should not be blank if using login credentials from computeResourcePreference
            if (!loginUserNameDiffers && !allocationProjectNumberNotBlankUsingSameLoginUserName) {
                throw new RuntimeException("loginUserName must differ from ComputeResourcePreference or " +
                        "allocationProjectNumber should not be blank. GroupResourceProfile "
                        + groupComputeResourcePreference.getGroupResourceProfileId()
                        + " for compute resource " + computeResourceId);
            }
        }
    }

    @Override
    public GroupComputeResourcePreference update(GroupComputeResourcePreference computeResourcePreference) {
        GroupResourceProfile groupResourceProfile = (new GroupResourceProfileRepository()).getGroupResourceProfile(computeResourcePreference.getGroupResourceProfileId());
        validateGroupComputeResourcePreference(computeResourcePreference, groupResourceProfile.getGatewayId());
        return super.update(computeResourcePreference);
    }
}

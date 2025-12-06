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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.groupresourceprofile.AwsComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.EnvironmentSpecificPreferences;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.appcatalog.groupresourceprofile.SlurmComputeResourcePreference;
import org.apache.airavata.registry.core.entities.appcatalog.AWSGroupComputeResourcePrefEntity;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.apache.airavata.registry.core.entities.appcatalog.GroupComputeResourcePrefPK;
import org.apache.airavata.registry.core.entities.appcatalog.SlurmGroupComputeResourcePrefEntity;
import org.springframework.stereotype.Repository;

/**
 * Created by skariyat on 2/10/18.
 */
@Repository
public class GrpComputePrefRepository
        extends AppCatAbstractRepository<
                GroupComputeResourcePreference, GroupComputeResourcePrefEntity, GroupComputeResourcePrefPK> {

    public GrpComputePrefRepository() {
        super(GroupComputeResourcePreference.class, GroupComputeResourcePrefEntity.class);
    }

    @Override
    public GroupComputeResourcePreference get(GroupComputeResourcePrefPK groupComputeResourcePrefPK) {
        GroupComputeResourcePreference pref = super.get(groupComputeResourcePrefPK);
        if (pref == null) {
            return null;
        }

        GroupComputeResourcePrefEntity ent =
                execute(em -> em.find(GroupComputeResourcePrefEntity.class, groupComputeResourcePrefPK));
        if (ent == null) {
            return pref;
        }

        if (ent instanceof SlurmGroupComputeResourcePrefEntity sl) {
            pref.setResourceType(ResourceType.SLURM);

            SlurmComputeResourcePreference scrp = new SlurmComputeResourcePreference();
            scrp.setAllocationProjectNumber(sl.getAllocationProjectNumber());
            scrp.setPreferredBatchQueue(sl.getPreferredBatchQueue());
            scrp.setQualityOfService(sl.getQualityOfService());
            scrp.setUsageReportingGatewayId(sl.getUsageReportingGatewayId());
            scrp.setSshAccountProvisioner(sl.getSshAccountProvisioner());
            scrp.setSshAccountProvisionerAdditionalInfo(sl.getSshAccountProvisionerAdditionalInfo());
            // TODO - check whether the groupSSHAccountProvisionerConfigs and reservations are needed

            EnvironmentSpecificPreferences esp = new EnvironmentSpecificPreferences();
            esp.setSlurm(scrp);
            pref.setSpecificPreferences(esp);

        } else if (ent instanceof AWSGroupComputeResourcePrefEntity aws) {
            pref.setResourceType(ResourceType.AWS);

            AwsComputeResourcePreference awsPref = new AwsComputeResourcePreference();
            awsPref.setRegion(aws.getRegion());
            awsPref.setPreferredAmiId(aws.getPreferredAmiId());
            awsPref.setPreferredInstanceType(aws.getPreferredInstanceType());

            EnvironmentSpecificPreferences esp = new EnvironmentSpecificPreferences();
            esp.setAws(awsPref);
            pref.setSpecificPreferences(esp);
            return pref;
        }

        return pref;
    }
}

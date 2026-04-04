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
import org.apache.airavata.compute.model.AWSGroupComputeResourcePrefEntity;
import org.apache.airavata.compute.model.ComputeResourceReservationEntity;
import org.apache.airavata.compute.model.GroupComputeResourcePrefEntity;
import org.apache.airavata.compute.model.GroupComputeResourcePrefPK;
import org.apache.airavata.compute.model.SlurmGroupComputeResourcePrefEntity;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.AwsComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourceReservation;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.EnvironmentSpecificPreferences;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.SlurmComputeResourcePreference;
import org.springframework.stereotype.Component;

/**
 * Created by skariyat on 2/10/18.
 */
@Component
public class GrpComputePrefRepository
        extends AbstractRepository<
                GroupComputeResourcePreference, GroupComputeResourcePrefEntity, GroupComputeResourcePrefPK> {

    public GrpComputePrefRepository() {
        super(GroupComputeResourcePreference.class, GroupComputeResourcePrefEntity.class);
    }

    @Override
    protected GroupComputeResourcePreference toModel(GroupComputeResourcePrefEntity entity) {
        return ComputeMapper.INSTANCE.groupComputePrefToModel(entity);
    }

    @Override
    protected GroupComputeResourcePrefEntity toEntity(GroupComputeResourcePreference model) {
        return ComputeMapper.INSTANCE.groupComputePrefToEntity(model);
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
            SlurmComputeResourcePreference.Builder scrpBuilder = SlurmComputeResourcePreference.newBuilder();
            if (sl.getAllocationProjectNumber() != null)
                scrpBuilder.setAllocationProjectNumber(sl.getAllocationProjectNumber());
            if (sl.getPreferredBatchQueue() != null) scrpBuilder.setPreferredBatchQueue(sl.getPreferredBatchQueue());
            if (sl.getQualityOfService() != null) scrpBuilder.setQualityOfService(sl.getQualityOfService());
            if (sl.getUsageReportingGatewayId() != null)
                scrpBuilder.setUsageReportingGatewayId(sl.getUsageReportingGatewayId());
            if (sl.getSshAccountProvisioner() != null)
                scrpBuilder.setSshAccountProvisioner(sl.getSshAccountProvisioner());
            if (sl.getSshAccountProvisionerAdditionalInfo() != null)
                scrpBuilder.setSshAccountProvisionerAdditionalInfo(sl.getSshAccountProvisionerAdditionalInfo());
            if (sl.getReservations() != null) {
                for (ComputeResourceReservationEntity resEnt : sl.getReservations()) {
                    ComputeResourceReservation.Builder resBuilder = ComputeResourceReservation.newBuilder()
                            .setReservationId(resEnt.getReservationId())
                            .setReservationName(resEnt.getReservationName())
                            .setStartTime(resEnt.getStartTime().getTime())
                            .setEndTime(resEnt.getEndTime().getTime());
                    if (resEnt.getQueueNames() != null) {
                        resBuilder.addAllQueueNames(resEnt.getQueueNames());
                    }
                    scrpBuilder.addReservations(resBuilder.build());
                }
            }

            EnvironmentSpecificPreferences esp = EnvironmentSpecificPreferences.newBuilder()
                    .setSlurm(scrpBuilder.build())
                    .build();
            pref = pref.toBuilder()
                    .setResourceType(ResourceType.SLURM)
                    .setSpecificPreferences(esp)
                    .build();

        } else if (ent instanceof AWSGroupComputeResourcePrefEntity aws) {
            AwsComputeResourcePreference.Builder awsPrefBuilder = AwsComputeResourcePreference.newBuilder();
            if (aws.getRegion() != null) awsPrefBuilder.setRegion(aws.getRegion());
            if (aws.getPreferredAmiId() != null) awsPrefBuilder.setPreferredAmiId(aws.getPreferredAmiId());
            if (aws.getPreferredInstanceType() != null)
                awsPrefBuilder.setPreferredInstanceType(aws.getPreferredInstanceType());

            EnvironmentSpecificPreferences esp = EnvironmentSpecificPreferences.newBuilder()
                    .setAws(awsPrefBuilder.build())
                    .build();
            pref = pref.toBuilder()
                    .setResourceType(ResourceType.AWS)
                    .setSpecificPreferences(esp)
                    .build();
            return pref;
        }

        return pref;
    }
}

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
package org.apache.airavata.sharing.registry.migrator.airavata;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import java.util.*;

public class EveryoneGroupMigrator {

    public static void main(String[] args) throws ClassNotFoundException, TException, ApplicationSettingsException {

        RegistryService.Client registryClient = RegistryServiceClientFactory.createRegistryClient("149.165.169.138",8970);
        SharingRegistryService.Client sharingClient = SharingRegistryServiceClientFactory.createSharingRegistryClient("149.165.169.138", 7878);

        List<Domain> domainList = sharingClient.getDomains(-1, 0);
        for (Domain domain : domainList) {
            String groupId = "everyone@" + domain.domainId;
            if (!sharingClient.isGroupExists(domain.domainId, groupId)) {
                UserGroup userGroup = new UserGroup();
                userGroup.setGroupId(groupId);
                userGroup.setDomainId(domain.domainId);
                userGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
                userGroup.setCreatedTime(System.currentTimeMillis());
                userGroup.setUpdatedTime(System.currentTimeMillis());
                userGroup.setName("everyone");
                userGroup.setDescription("Default Group");
                userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);
                sharingClient.createGroup(userGroup);

                List<User> userList = sharingClient.getUsers(domain.domainId, -1, 0);
                List<String> users = new ArrayList<>();
                for (User user : userList) {
                    users.add(user.getUserId());
                }
                sharingClient.addUsersToGroup(domain.domainId, users, groupId);

                List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList = registryClient.getAllApplicationDeployments(domain.domainId);
                for (ApplicationDeploymentDescription applicationDeploymentDescription : applicationDeploymentDescriptionList) {
                    sharingClient.shareEntityWithGroups(domain.domainId, applicationDeploymentDescription.getAppDeploymentId(), Arrays.asList(groupId),domain.domainId+":READ", true);
                }
            }
        }

    }

}

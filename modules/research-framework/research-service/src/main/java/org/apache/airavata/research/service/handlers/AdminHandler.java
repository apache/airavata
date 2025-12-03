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
package org.apache.airavata.research.service.handlers;

import java.util.Set;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.Resource;
import org.apache.airavata.research.service.model.entity.ResourceVerificationActivity;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.ResourceVerificationActivityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminHandler {

    private ResourceRepository resourceRepository;
    private ResourceVerificationActivityRepository verificationActivityRepository;

    @Value("#{'${airavata.research-portal.admin-emails}'.split(',')}")
    private Set<String> cybershuttleAdminEmails;

    public AdminHandler(
            ResourceRepository resourceRepository,
            ResourceVerificationActivityRepository verificationActivityRepository) {
        this.resourceRepository = resourceRepository;
        this.verificationActivityRepository = verificationActivityRepository;
    }

    public Resource verifyResource(Resource resource) {
        if (resource.getStatus().equals(StatusEnum.VERIFIED)) {
            return resource;
        }
        String userId = UserContext.userId();
        ensureAdminPermissions(userId);

        resource.setStatus(StatusEnum.VERIFIED);
        resourceRepository.save(resource);

        ResourceVerificationActivity activity = new ResourceVerificationActivity();
        activity.setResource(resource);
        activity.setUserId(userId);
        activity.setStatus(StatusEnum.VERIFIED);
        verificationActivityRepository.save(activity);

        return resource;
    }

    public Resource rejectResource(Resource resource, String rejectionMessage) {
        String userId = UserContext.userId();
        ensureAdminPermissions(userId);
        String cleanMessage = rejectionMessage.trim().replaceAll("\"", "");

        resource.setStatus(StatusEnum.REJECTED);
        resourceRepository.save(resource);

        ResourceVerificationActivity activity = new ResourceVerificationActivity();
        activity.setResource(resource);
        activity.setUserId(userId);
        activity.setMessage(cleanMessage);
        activity.setStatus(StatusEnum.REJECTED);
        verificationActivityRepository.save(activity);

        return resource;
    }

    private void ensureAdminPermissions(String userId) {
        if (!cybershuttleAdminEmails.contains(userId)) {
            throw new RuntimeException(String.format("User %s does not have admin access in Cybershuttle", userId));
        }
    }
}

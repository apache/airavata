/*
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
 *
*/
package org.apache.airavata.registry.core.repositories;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.GatewayEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.NotificationEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.ProjectEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.GatewayRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.NotificationRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.ProjectRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class RepositoryTest {
    private final static Logger logger = LoggerFactory.getLogger(RepositoryTest.class);

    @Test
    public void test(){
        Gateway gateway = new Gateway();
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
        gateway.setGatewayId("test.com" + System.currentTimeMillis());
        gateway.setDomain("test.com");

        GatewayRepository gatewayRepository = new GatewayRepository(Gateway.class, GatewayEntity.class);
        gateway = gatewayRepository.create(gateway);
        Assert.assertTrue(!gateway.getGatewayId().isEmpty());

        Notification notification = new Notification();
        notification.setGatewayId(gateway.getGatewayId());
        notification.setNotificationId(UUID.randomUUID().toString());

        NotificationRepository notificationRepository = new NotificationRepository(Notification.class, NotificationEntity.class);
        notificationRepository.create(notification);

        notificationRepository.get(notification.getNotificationId());

        UserProfile userProfile = new UserProfile();
        userProfile.setAiravataInternalUserId(UUID.randomUUID().toString());
        userProfile.setGatewayId(gateway.getGatewayId());
        UserProfileRepository userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
        userProfileRepository.create(userProfile);


        Project project = new Project();
        project.setProjectID(UUID.randomUUID().toString());
        project.setOwner(userProfile.getAiravataInternalUserId());
        project.setGatewayId(gateway.getGatewayId());
        project.setName("Project Name");

        ProjectRepository projectRepository = new ProjectRepository(Project.class, ProjectEntity.class);
        projectRepository.create(project);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId(UUID.randomUUID().toString());
        experiment.setUserName(userProfile.getAiravataInternalUserId());
        experiment.setProjectId(project.getProjectID());
        experiment.setGatewayId(gateway.getGatewayId());
        experiment.setExperimentName("Dummy Experiment");

        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
        userConfigurationData.setExperimentDataDir("some/path");
        experiment.setUserConfigurationData(userConfigurationData);

        ExperimentRepository experimentRepository = new ExperimentRepository(ExperimentModel.class, ExperimentEntity.class);
        experimentRepository.create(experiment);
    }
}
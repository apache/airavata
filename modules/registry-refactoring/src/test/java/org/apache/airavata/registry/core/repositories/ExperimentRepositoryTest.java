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
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.GatewayEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.ProjectEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.GatewayRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.ProjectRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentRepositoryTest {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private UserProfileRepository userProfileRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private String gatewayId;
    private String userId;
    private String projectId;
    private String experimentId;

    private final String GATEWAY_DOMAIN = "test1.com";
    private final String EXPERIMENT_NAME = "sample experiment";
    private final String EXPERIMENT_DESCRIPTION = "sample description";


    @Before
    public void setupRepository() {

        gatewayRepository = new GatewayRepository(Gateway.class, GatewayEntity.class);
        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
        projectRepository = new ProjectRepository(Project.class, ProjectEntity.class);
        experimentRepository = new ExperimentRepository(ExperimentModel.class, ExperimentEntity.class);

        gatewayId = "test.com" + System.currentTimeMillis();
        userId = "testuser" + System.currentTimeMillis();
        projectId = "project" + System.currentTimeMillis();
        experimentId = "exp" + System.currentTimeMillis();
    }

    @Test
    public void experimentRepositoryTest() {

		/*
         * Creating Gateway required for UserProfile & Project creation
		 */
        Gateway gateway = new Gateway();
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
        gateway.setGatewayId(gatewayId);
        gateway.setDomain(GATEWAY_DOMAIN);
        gateway = gatewayRepository.create(gateway);
        Assert.assertTrue(!gateway.getGatewayId().isEmpty());

		/*
         * UserProfile Instance creation required for Project Creation
		 */
        UserProfile userProfile = new UserProfile();
        userProfile.setAiravataInternalUserId(userId);
        userProfile.setGatewayId(gateway.getGatewayId());
        userProfile = userProfileRepository.create(userProfile);
        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());

        /*
         * Project Instance creation
         */
        Project project = new Project();
        project.setGatewayId(gatewayId);
        project.setOwner(userId);
        project.setProjectID(projectId);
        project.setGatewayIdIsSet(true);
        project = projectRepository.create(project);
        Assert.assertTrue(!project.getProjectID().isEmpty());

        /*
         * Experiment Instance Creation
         */

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId(experimentId);
        experiment.setExperimentName(EXPERIMENT_NAME);
        experiment.setGatewayId(gatewayId);
        experiment.setUserName(userId);
        experiment.setProjectId(projectId);

        /*
         * Experiment Repository Insert Operation Test
		 */
        experiment = experimentRepository.create(experiment);
        Assert.assertTrue(!experiment.getExperimentId().isEmpty());


        /*
         * Experiment Repository Update Operation Test
		 */
        experiment.setDescription(EXPERIMENT_DESCRIPTION);
        experimentRepository.update(experiment);
        experiment = experimentRepository.get(experimentId);
        Assert.assertEquals(experiment.getDescription(), EXPERIMENT_DESCRIPTION);

		/*
         * Workspace Project Repository Select Operation Test
		 */
        experiment = experimentRepository.get(experimentId);
        Assert.assertNotNull(experiment);

		/*
         * Experiment Repository Delete Operation
		 */

        boolean deleteResult = experimentRepository.delete(experimentId);
        Assert.assertTrue(deleteResult);

        deleteResult = projectRepository.delete(projectId);
        Assert.assertTrue(deleteResult);

        deleteResult = userProfileRepository.delete(userId);
        Assert.assertTrue(deleteResult);

        deleteResult = gatewayRepository.delete(gatewayId);
        Assert.assertTrue(deleteResult);


    }

}
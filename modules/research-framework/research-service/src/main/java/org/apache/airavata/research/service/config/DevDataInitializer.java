/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.config;

import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.User;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;

    @Value("${cybershuttle.hub.dev-user}")
    private String devUserEmail;

    public DevDataInitializer(UserRepository userRepository, ProjectRepository projectRepository, ResourceRepository resourceRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User user = new User(devUserEmail, "airavata", "admin", devUserEmail);
            user.setAvatar("");
            userRepository.save(user);

            RepositoryResource repositoryResource = new RepositoryResource();
            repositoryResource.setName("BMTK Repository");
            repositoryResource.setDescription("Repository for the BMTK workshop project");
            repositoryResource.setHeaderImage("header_image.png");
            repositoryResource.setRepositoryUrl("https://github.com/AllenInstitute/bmtk-workshop.git");
            repositoryResource.setStatus(StatusEnum.VERIFIED);
            repositoryResource.setPrivacy(PrivacyEnum.PUBLIC);
            repositoryResource = resourceRepository.save(repositoryResource);

            DatasetResource datasetResource = new DatasetResource();
            datasetResource.setName("BMTK Dataset");
            datasetResource.setDescription("Dataset for the BMTK workshop project");
            datasetResource.setHeaderImage("header_image.png");
            datasetResource.setDatasetUrl("bmtk");
            datasetResource.setStatus(StatusEnum.VERIFIED);
            datasetResource.setPrivacy(PrivacyEnum.PUBLIC);
            Set<User> set = new HashSet<>();
            set.add(user);
            datasetResource.setAuthors(set);
            datasetResource = resourceRepository.save(datasetResource);

            Project project = new Project();
            project.setRepositoryResource(repositoryResource);
            project.getDatasetResources().add(datasetResource);
            project.setName("BMTK Workshop Project");

            projectRepository.save(project);

            System.out.println("Initialized Project with id: " + project.getId());
        }
    }
}

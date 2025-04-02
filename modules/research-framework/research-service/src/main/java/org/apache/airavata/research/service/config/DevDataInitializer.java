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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.airavata.research.service.config;

import java.util.HashSet;

import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;

    @Value("${airavata.research-hub.dev-user}")
    private String devUserEmail;

    public DevDataInitializer(ProjectRepository projectRepository, ResourceRepository resourceRepository) {
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
    }

    private void createProject(String name, String repoUrl, String datasetName, String datasetUrl, String user) {
        RepositoryResource repo = new RepositoryResource();
        repo.setName(name);
        repo.setDescription("Repository for " + name);
        repo.setHeaderImage("header_image.png");
        repo.setRepositoryUrl(repoUrl);
        repo.setStatus(StatusEnum.VERIFIED);
        repo.setPrivacy(PrivacyEnum.PUBLIC);
        repo = resourceRepository.save(repo);

        DatasetResource dataset = new DatasetResource();
        dataset.setName(datasetName);
        dataset.setDescription("Dataset for " + name);
        dataset.setHeaderImage("header_image.png");
        dataset.setDatasetUrl(datasetUrl);
        dataset.setStatus(StatusEnum.VERIFIED);
        dataset.setPrivacy(PrivacyEnum.PUBLIC);
        dataset.setAuthors(new HashSet<>() {
            {
                add(user);
            }
        });
        dataset = resourceRepository.save(dataset);

        Project project = new Project();
        project.setRepositoryResource(repo);
        project.getDatasetResources().add(dataset);
        project.setName(name);
        project.setOwnerId(user);
        projectRepository.save(project);

        System.out.println("Initialized Project with id: " + project.getId());
    }

    @Override
    public void run(String... args) {
        if (projectRepository.existsByOwnerId(devUserEmail)) {
            System.out.println("Dev data already initialized. Skipping initialization.");
            return;
        }

        createProject(
                "Allen / BMTK Workshop",
                "https://github.com/yasithdev/bmtk-workshop.git",
                "Allen / BMTK Workshop Data",
                "allen-bmtk-workshop",
                devUserEmail
        );

        createProject(
                "Allen / V1",
                "https://github.com/yasithdev/allen-v1.git",
                "Allen / V1 Data",
                "allen-v1",
                devUserEmail
        );

        createProject(
                "BRAINML / OneHot HMMGLM",
                "https://github.com/yasithdev/onehot-hmmglm.git",
                "BRAINML / OneHot HMMGLM Data",
                "brainml-onehot-hmmglm",
                devUserEmail
        );

        createProject(
                "HChoiLab / Functional Network",
                "https://github.com/yasithdev/functional-network.git",
                "HChoiLab / Functional Network Data",
                "hchoilab-functional-network",
                devUserEmail
        );
    }
}

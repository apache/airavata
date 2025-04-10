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
import java.util.Set;

import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.Tag;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;
    private final TagRepository tagRepository;

    @Value("${airavata.research-hub.dev-user}")
    private String devUserEmail;

    public DevDataInitializer(ProjectRepository projectRepository, ResourceRepository resourceRepository, TagRepository tagRepository) {
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
        this.tagRepository = tagRepository;
    }

    private void createProject(String name, String repoUrl, String datasetName, String datasetUrl, String[] tags, String user) {
        Set<Tag> tagSet = new HashSet<>();
        for (String tag : tags) {
            Tag t = tagRepository.findByValue(tag);
            if (t != null) {
                tagSet.add(t);
            } else {
                Tag newTag = new Tag();
                newTag.setValue(tag);
                tagSet.add(newTag);
                tagRepository.save(newTag);
            }
        }

        Set<String> authors = new HashSet<>() {
            {
                add(user);
            }
        };

        RepositoryResource repo = new RepositoryResource();
        repo.setName(name);
        repo.setDescription("Repository for " + name);
        repo.setHeaderImage("header_image.png");
        repo.setRepositoryUrl(repoUrl);
        repo.setStatus(StatusEnum.VERIFIED);
        repo.setPrivacy(PrivacyEnum.PUBLIC);
        repo.setTags(tagSet);
        repo.setAuthors(authors);
        repo = resourceRepository.save(repo);

        DatasetResource dataset = new DatasetResource();
        dataset.setName(datasetName);
        dataset.setDescription("Dataset for " + name);
        dataset.setHeaderImage("header_image.png");
        dataset.setDatasetUrl(datasetUrl);
        dataset.setStatus(StatusEnum.VERIFIED);
        dataset.setPrivacy(PrivacyEnum.PUBLIC);
        dataset.setTags(tagSet);
        dataset.setAuthors(authors);
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
                "Bio-realistic model of primary visual cortex (V1) (Allen Institute)",
                "https://github.com/cyber-shuttle/allen-v1.git",
                "Bio-realistic model of primary visual cortex (V1) (Allen Institute)",
                "allenai-v1",
                new String[]{"allenai", "v1-model", "workshop"},
                "Anton Arkhipov & Laura Green"
        );

        createProject(
                "Apache Cerebrum: Flexible tool for constructing computational neuroscience models from large public databases and brain atlases",
                "https://github.com/cyber-shuttle/airavata-cerebrum",
                "Apache Cerebrum: Flexible tool for constructing computational neuroscience models from large public databases and brain atlases",
                "apache-airavata-cerebrum",
                new String[]{"apache", "cerebrum", "workshop"},
                "Sriram Chockalingam"
        );

        createProject(
                "Large-scale brain model during awake and sleep states",
                "https://github.com/cyber-shuttle/whole-brain-public",
                "Large-scale brain model during awake and sleep states",
                "bazhlab-whole-brain",
                new String[]{"bazhlab", "whole-brain", "workshop"},
                "Maxim Bazhenov & Gabriela Navas Zuloaga"
        );

        createProject(
                "One-hot Generalized Linear Model for Switching Brain State Discovery",
                "https://github.com/cyber-shuttle/onehot-hmmglm",
                "One-hot Generalized Linear Model for Switching Brain State Discovery",
                "brainml-onehot-hmmglm",
                new String[]{"brainml", "onehot-hmmglm", "workshop"},
                "Anqi Wu & Chengrui Li"
        );

        createProject(
                "Biologically Constrained RNNs via Dale's Backpropagation and Topologically-Informed Pruning",
                "https://github.com/cyber-shuttle/biologicalRNNs",
                "Biologically Constrained RNNs via Dale's Backpropagation and Topologically-Informed Pruning",
                "hchoilab-biologicalRNNs",
                new String[]{"hchoilab", "biologicalRNNs", "workshop"},
                "Hanna Choi & Aishwarya Balwani"
        );

        createProject(
                "Computing with Neural Oscillators",
                "https://github.com/cyber-shuttle/NeuroDATA_2025",
                "Computing with Neural Oscillators",
                "immam-gt-neurodata25",
                new String[]{"immam-gt", "neural-oscillators", "workshop"},
                "Nabil Imam & Nand Chandravadia"
        );

        createProject(
                "Deep Learning in Neuroscience with torch_brain and temporaldata",
                "https://github.com/cyber-shuttle/neurodata25_torchbrain_notebooks",
                "Deep Learning in Neuroscience with torch_brain and temporaldata",
                "nerdslab-neurodata25",
                new String[]{"nerdslab", "torch_brain", "workshop"},
                "Vinam Arora & Mahato Shivashriganesh"
        );

        createProject(
                "NetFormer: Transformer model for neural connectivity",
                "https://github.com/cyber-shuttle/NetFormer",
                "NetFormer : Transformer model for neural connectivity",
                "neuroaihub-netformer",
                new String[]{"neuroaihub", "netformer", "workshop"},
                "Lu Mi"
        );
    }
}

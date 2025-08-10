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
package org.apache.airavata.research.service.config;

import static org.apache.airavata.research.service.enums.AuthorRoleEnum.PRIMARY;
import static org.apache.airavata.research.service.enums.StateEnum.ACTIVE;

import java.util.HashSet;
import java.util.Set;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.ResourceAuthor;
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

    public DevDataInitializer(
            ProjectRepository projectRepository, ResourceRepository resourceRepository, TagRepository tagRepository) {
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
        this.tagRepository = tagRepository;
    }

    private void createProject(
            String name, String description, String repoUrl, String datasetUrl, String[] tags, Set<String> authors) {
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

        RepositoryResource repo = new RepositoryResource();
        repo.setName(name);
        repo.setDescription(description);
        repo.setHeaderImage("header_image.png");
        repo.setRepositoryUrl(repoUrl);
        repo.setStatus(StatusEnum.VERIFIED);
        repo.setPrivacy(PrivacyEnum.PUBLIC);
        repo.setTags(tagSet);
        repo.setState(ACTIVE);
        Set<ResourceAuthor> repoResourceAuthors = new HashSet<>();
        for (String author : authors) {
            ResourceAuthor a = new ResourceAuthor();
            a.setResource(repo);
            a.setRole(PRIMARY);
            a.setAuthorId(author);
            repoResourceAuthors.add(a);
        }
        repo.setAuthors(repoResourceAuthors);
        repo = resourceRepository.save(repo);

        DatasetResource dataset = new DatasetResource();
        dataset.setName(datasetUrl);
        dataset.setDescription(description);
        dataset.setHeaderImage("header_image.png");
        dataset.setDatasetUrl(datasetUrl);
        dataset.setStatus(StatusEnum.VERIFIED);
        dataset.setPrivacy(PrivacyEnum.PUBLIC);
        dataset.setTags(tagSet);
        dataset.setState(ACTIVE);
        Set<ResourceAuthor> datasetResourceAuthors = new HashSet<>();
        for (String author : authors) {
            ResourceAuthor a = new ResourceAuthor();
            a.setResource(repo);
            a.setRole(PRIMARY);
            a.setAuthorId(author);
            datasetResourceAuthors.add(a);
        }
        dataset.setAuthors(datasetResourceAuthors);
        dataset = resourceRepository.save(dataset);

        Project project = new Project();
        project.setRepositoryResource(repo);
        project.getDatasetResources().add(dataset);
        project.setName(name);
        project.setState(ACTIVE);
        project.setOwnerId(String.join(", ", authors.stream().toString()));
        projectRepository.save(project);

        System.out.println("Initialized Project with id: " + project.getId());
    }

    @Override
    public void run(String... args) {
        System.out.println("HRSDSF");
        if (projectRepository.count() > 0) {
            System.out.println("Dev data already initialized. Skipping initialization.");
            return;
        }

        System.out.println("Initializing dev data...");

        createProject(
                "Bio-realistic multiscale simulations of cortical circuits",
                "Running the AllenAI V1 model, with thalamacortical (LGN) and background (BKG) inputs",
                "https://github.com/cyber-shuttle/allenai-v1",
                "allenai-v1",
                new String[] {"neurodata25", "allenai", "visual_cortex"},
                Set.of("Anton Arkhipov", "Laura Green"));

        createProject(
                "Apache Cerebrum",
                "Constructing computational neuroscience models from large public databases and brain atlases",
                "https://github.com/cyber-shuttle/airavata-cerebrum",
                "apache-airavata-cerebrum",
                new String[] {"neurodata25", "apache", "cerebrum"},
                Set.of("Sriram Chockalingam"));

        createProject(
                "Spatio-temporal dynamics of sleep in large-scale brain models",
                "Running a large-scale brain model during awake and sleep states",
                "https://github.com/cyber-shuttle/whole-brain-public",
                "bazhlab-whole-brain",
                new String[] {"neurodata25", "bazhlab", "whole-brain"},
                Set.of("Maxim Bazhenov", "Gabriela Navas Zuloaga"));

        createProject(
                "Biologically Constrained RNNs",
                "Running a biologically constrained RNN via Dale's backpropagation and topologically-informed pruning",
                "https://github.com/cyber-shuttle/biologicalRNNs",
                "hchoilab-biologicalRNNs",
                new String[] {"neurodata25", "hchoilab", "biological-rnn"},
                Set.of("Hannah Choi", "Aishwarya Balwani"));

        createProject(
                "One-hot Generalized Linear Model for Switching Brain State Discovery",
                "Reproducing the One-hot HMM-GLM paper (ICLR 2024)",
                "https://github.com/cyber-shuttle/onehot-hmmglm",
                "brainml-onehot-hmmglm",
                new String[] {"neurodata25", "brainml", "hmm-glm"},
                Set.of("Anqi Wu", "Chengrui Li"));

        createProject(
                "Scaling up neural data analysis with torch_brain and temporaldata",
                "Understand and highlight the features of torch_brain and temporaldata",
                "https://github.com/cyber-shuttle/neurodata25_torchbrain_notebooks",
                "nerdslab-neurodata25",
                new String[] {"neurodata25", "nerdslab", "torch_brain", "temporaldata"},
                Set.of("Eva Dyer, Vinam Arora", "Mahato Shivashriganesh"));

        createProject(
                "Bridge the Gap between the Structure and Function in the Brain",
                "Run the NetFormer model for neural connectivity",
                "https://github.com/cyber-shuttle/neuroaihub-netformer",
                "neuroaihub-netformer",
                new String[] {"neurodata25", "neuroaihub", "netformer"},
                Set.of("Lu Mi"));

        createProject(
                "Computing with Neural Oscillators",
                "A speech demo that uses Neural Oscillators",
                "https://github.com/cyber-shuttle/imamlab-neural-oscillators",
                "imamlab-neurodata25",
                new String[] {"neurodata25", "imamlab", "neural-oscillators"},
                Set.of("Nabil Imam, Nand Chandravadia"));

        createProject(
                "Getting started with Cybershuttle",
                "Run a simulation and understand the minimum macros required to run Cybershuttle",
                "https://github.com/cyber-shuttle/cybershuttle-reference",
                "cybershuttle-reference",
                new String[] {"cybershuttle", "apache-airavata", "reference"},
                Set.of("Suresh Marru"));

        createProject(
                "Malicious URL Detector",
                "Detect malicious URLs using machine learning models",
                "https://github.com/airavata-courses/malicious-url-detector",
                "airavata-courses-malicious-url-detector",
                new String[] {"airavata-courses", "spring-2025"},
                Set.of("Krish Katariya", "Jesse Gong", "Shreyas Arisa", "Devin Fromond"));

        createProject(
                "Deepseek Remote Execution",
                "Executing deepseek model on remote HPC",
                "https://github.com/ZhenmeiOng/proj2-llama",
                "airavata-courses-deepseek-chat",
                new String[] {"airavata-courses", "spring-2025", "llm"},
                Set.of("Yashkaran Chauhan", "Zhenmei Ong", "Varenya Amagowni"));

        createProject(
                "Fast Chat",
                "Fast and easy communication with fast chat",
                "https://github.com/riccog/cybershuttle",
                "airavata-courses-fast-chat",
                new String[] {"airavata-courses", "spring-2025"},
                Set.of("Ricco Goss", "Mason Graham", "Talam", "Ruchira"));
    }
}

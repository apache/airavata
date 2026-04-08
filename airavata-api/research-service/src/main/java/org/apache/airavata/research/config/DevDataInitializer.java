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
package org.apache.airavata.research.config;

import java.util.HashSet;
import java.util.Set;
import org.apache.airavata.research.model.DatasetResourceEntity;
import org.apache.airavata.research.model.RepositoryResourceEntity;
import org.apache.airavata.research.model.ResearchProjectEntity;
import org.apache.airavata.research.repository.ResearchProjectRepository;
import org.apache.airavata.sharing.model.PrivacyEnum;
import org.apache.airavata.sharing.model.StatusEnum;
import org.apache.airavata.sharing.model.TagEntity;
import org.apache.airavata.sharing.repository.ResourceRepository;
import org.apache.airavata.sharing.repository.TagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final ResearchProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;
    private final TagRepository tagRepository;
    private final ResearchProperties researchProperties;

    public DevDataInitializer(
            ResearchProjectRepository projectRepository,
            ResourceRepository resourceRepository,
            TagRepository tagRepository,
            ResearchProperties researchProperties) {
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
        this.tagRepository = tagRepository;
        this.researchProperties = researchProperties;
    }

    private void createProject(
            String name, String description, String repoUrl, String datasetUrl, String[] tags, String user) {
        Set<TagEntity> tagSet = new HashSet<>();
        for (String tag : tags) {
            TagEntity t = tagRepository.findByValue(tag);
            if (t != null) {
                tagSet.add(t);
            } else {
                TagEntity newTag = new TagEntity();
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

        RepositoryResourceEntity repo = new RepositoryResourceEntity();
        repo.setName(name);
        repo.setDescription(description);
        repo.setHeaderImage("header_image.png");
        repo.setRepositoryUrl(repoUrl);
        repo.setStatus(StatusEnum.VERIFIED);
        repo.setPrivacy(PrivacyEnum.PUBLIC);
        repo.setTags(tagSet);
        repo.setAuthors(authors);
        repo = resourceRepository.save(repo);

        DatasetResourceEntity dataset = new DatasetResourceEntity();
        dataset.setName(datasetUrl);
        dataset.setDescription(description);
        dataset.setHeaderImage("header_image.png");
        dataset.setDatasetUrl(datasetUrl);
        dataset.setStatus(StatusEnum.VERIFIED);
        dataset.setPrivacy(PrivacyEnum.PUBLIC);
        dataset.setTags(tagSet);
        dataset.setAuthors(authors);
        dataset = resourceRepository.save(dataset);

        ResearchProjectEntity project = new ResearchProjectEntity();
        project.setRepositoryResource(repo);
        project.getDatasetResources().add(dataset);
        project.setName(name);
        project.setOwnerId(user);
        projectRepository.save(project);

        System.out.println("Initialized ResearchProjectEntity with id: " + project.getId());
    }

    @Override
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            System.out.println("Dev data already initialized. Skipping initialization.");
            return;
        }

        createProject(
                "Bio-realistic multiscale simulations of cortical circuits",
                "Running the AllenAI V1 model, with thalamacortical (LGN) and background (BKG) inputs",
                "https://github.com/cyber-shuttle/allenai-v1",
                "allenai-v1",
                new String[] {"neurodata25", "allenai", "visual_cortex"},
                "Anton Arkhipov, Laura Green");

        createProject(
                "Apache Cerebrum",
                "Constructing computational neuroscience models from large public databases and brain atlases",
                "https://github.com/cyber-shuttle/airavata-cerebrum",
                "apache-airavata-cerebrum",
                new String[] {"neurodata25", "apache", "cerebrum"},
                "Sriram Chockalingam");

        createProject(
                "Spatio-temporal dynamics of sleep in large-scale brain models",
                "Running a large-scale brain model during awake and sleep states",
                "https://github.com/cyber-shuttle/whole-brain-public",
                "bazhlab-whole-brain",
                new String[] {"neurodata25", "bazhlab", "whole-brain"},
                "Maxim Bazhenov, Gabriela Navas Zuloaga");

        createProject(
                "Biologically Constrained RNNs",
                "Running a biologically constrained RNN via Dale's backpropagation and topologically-informed pruning",
                "https://github.com/cyber-shuttle/biologicalRNNs",
                "hchoilab-biologicalRNNs",
                new String[] {"neurodata25", "hchoilab", "biological-rnn"},
                "Hannah Choi, Aishwarya Balwani");

        createProject(
                "One-hot Generalized Linear Model for Switching Brain State Discovery",
                "Reproducing the One-hot HMM-GLM paper (ICLR 2024)",
                "https://github.com/cyber-shuttle/onehot-hmmglm",
                "brainml-onehot-hmmglm",
                new String[] {"neurodata25", "brainml", "hmm-glm"},
                "Anqi Wu, Chengrui Li");

        createProject(
                "Scaling up neural data analysis with torch_brain and temporaldata",
                "Understand and highlight the features of torch_brain and temporaldata",
                "https://github.com/cyber-shuttle/neurodata25_torchbrain_notebooks",
                "nerdslab-neurodata25",
                new String[] {"neurodata25", "nerdslab", "torch_brain", "temporaldata"},
                "Eva Dyer, Vinam Arora, Mahato Shivashriganesh");

        createProject(
                "Bridge the Gap between the Structure and Function in the Brain",
                "Run the NetFormer model for neural connectivity",
                "https://github.com/cyber-shuttle/neuroaihub-netformer",
                "neuroaihub-netformer",
                new String[] {"neurodata25", "neuroaihub", "netformer"},
                "Lu Mi");

        createProject(
                "Computing with Neural Oscillators",
                "A speech demo that uses Neural Oscillators",
                "https://github.com/cyber-shuttle/imamlab-neural-oscillators",
                "imamlab-neurodata25",
                new String[] {"neurodata25", "imamlab", "neural-oscillators"},
                "Nabil Imam, Nand Chandravadia");

        createProject(
                "Getting started with Cybershuttle",
                "Run a simulation and understand the minimum macros required to run Cybershuttle",
                "https://github.com/cyber-shuttle/cybershuttle-reference",
                "cybershuttle-reference",
                new String[] {"cybershuttle", "apache-airavata", "reference"},
                "Suresh Marru");

        createProject(
                "Malicious URL Detector",
                "Detect malicious URLs using machine learning models",
                "https://github.com/airavata-courses/malicious-url-detector",
                "airavata-courses-malicious-url-detector",
                new String[] {"airavata-courses", "spring-2025"},
                "Krish Katariya, Jesse Gong, Shreyas Arisa, Devin Fromond");

        createProject(
                "Deepseek Remote Execution",
                "Executing deepseek model on remote HPC",
                "https://github.com/ZhenmeiOng/proj2-llama",
                "airavata-courses-deepseek-chat",
                new String[] {"airavata-courses", "spring-2025", "llm"},
                "Yashkaran Chauhan, Zhenmei Ong, Varenya Amagowni");

        createProject(
                "Fast Chat",
                "Fast and easy communication with fast chat",
                "https://github.com/riccog/cybershuttle",
                "airavata-courses-fast-chat",
                new String[] {"airavata-courses", "spring-2025"},
                "Ricco Goss, Mason Graham, Talam, Ruchira");
    }
}

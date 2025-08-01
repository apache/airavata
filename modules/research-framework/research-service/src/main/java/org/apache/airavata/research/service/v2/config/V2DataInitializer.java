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
package org.apache.airavata.research.service.v2.config;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.Tag;
import org.apache.airavata.research.service.model.repo.TagRepository;
import org.apache.airavata.research.service.v2.entity.Code;
import org.apache.airavata.research.service.v2.repository.CodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * V2 Data Initializer for Code entities
 * Storage resources now use airavata-api registry services (following migration.md)
 */
@Component
public class V2DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(V2DataInitializer.class);

    private final CodeRepository codeRepository;
    private final TagRepository tagRepository;

    public V2DataInitializer(CodeRepository codeRepository,
                           TagRepository tagRepository) {
        this.codeRepository = codeRepository;
        this.tagRepository = tagRepository;
    }

    @PostConstruct
    public void initializeData() {
        LOGGER.info("Initializing V2 mock data for code resources...");
        
        try {
            initializeCodes();
            
            LOGGER.info("V2 mock data initialization completed.");
        } catch (Exception e) {
            LOGGER.error("Error during V2 data initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize V2 mock data", e);
        }
    }

    private void initializeCodes() {
        if (codeRepository.count() == 0) {
            LOGGER.info("Creating mock code resources...");
            
            // Research-focused sample data based on v1 patterns and real deployments
            CodeData[] codeDataArray = {
                // Neuroscience Research Models (inspired by v1 neurodata25 projects)
                new CodeData(
                    "Bio-realistic Cortical Circuit Simulation Model",
                    "Running the AllenAI V1 model for bio-realistic multiscale simulations of cortical circuits with thalamacortical and background inputs",
                    "MODEL",
                    "Python",
                    "NEURON",
                    Set.of("anton.arkhipov@alleninstitute.org", "laura.green@alleninstitute.org"),
                    Set.of("neurodata25", "allenai", "visual-cortex", "neuroscience", "simulation"),
                    "allenai_v1_model.pkl",
                    "1.0",
                    null,
                    null,
                    "Bio-realistic cortical circuit model with thalamacortical inputs. Part of NeuroData25 initiative."
                ),
                
                new CodeData(
                    "Apache Cerebrum Computational Model",
                    "Constructing computational neuroscience models from large public databases and brain atlases using Apache Airavata middleware",
                    "MODEL",
                    "Python",
                    "Apache Airavata",
                    Set.of("sriram.chockalingam@apache.org"),
                    Set.of("neurodata25", "apache", "cerebrum", "brain-atlases", "computational-neuroscience"),
                    "cerebrum_brain_model_v2.h5",
                    "2.0",
                    null,
                    null,
                    "Large-scale brain modeling framework using public neuroscience databases."
                ),
                
                new CodeData(
                    "Biologically Constrained RNN Model",
                    "Biologically constrained recurrent neural network via Dale's backpropagation and topologically-informed pruning for neural computation",
                    "MODEL",
                    "Python",
                    "PyTorch",
                    Set.of("hannah.choi@gatech.edu", "aishwarya.balwani@gatech.edu"),
                    Set.of("neurodata25", "hchoilab", "biological-rnn", "dale-principle", "neural-networks"),
                    "biological_rnn_trained.pth",
                    "1.2",
                    null,
                    null,
                    "Biologically plausible RNN with Dale's law constraints and topological pruning."
                ),
                
                // Computational Chemistry Models (inspired by SQL dump applications) 
                new CodeData(
                    "PSI4 Quantum Chemistry Model",
                    "OPENMP Psi4 application for ab initio quantum chemistry programs designed for efficient, high-accuracy simulations of molecular properties",
                    "MODEL",
                    "Python",
                    "PSI4",
                    Set.of("quantum.team@psi4.org", "ccguser@chemistry.org"),
                    Set.of("quantum-chemistry", "ab-initio", "molecular-simulation", "psi4", "computational-chemistry"),
                    "psi4_optimized_model.wfn",
                    "1.8",
                    null,
                    null,
                    "High-accuracy quantum chemistry calculations with OPENMP parallelization."
                ),
                
                new CodeData(
                    "AlphaFold2 Protein Structure Model",
                    "Protein structure prediction using locally deployed AlphaFold2 singularity container for accurate protein folding prediction",
                    "MODEL",
                    "Python",
                    "JAX",
                    Set.of("deepmind.team@google.com", "scigap@alphafold.org"),
                    Set.of("protein-folding", "alphafold", "structural-biology", "deep-learning", "bioinformatics"),
                    "alphafold2_weights.pkl",
                    "2.3",
                    null,
                    null,
                    "State-of-the-art protein structure prediction using AlphaFold2 architecture."
                ),
                
                // Research Notebooks (based on real scientific workflows)
                new CodeData(
                    "Whole-Brain Sleep Dynamics Analysis",
                    "Jupyter notebook for analyzing spatio-temporal dynamics of sleep in large-scale brain models during awake and sleep states",
                    "NOTEBOOK",
                    "Python",
                    "Jupyter",
                    Set.of("maxim.bazhenov@ucsd.edu", "gabriela.navas@ucsd.edu"),
                    Set.of("neurodata25", "bazhlab", "whole-brain", "sleep-dynamics", "neuroscience"),
                    "sleep_dynamics_analysis.ipynb",
                    "3.1",
                    null,
                    null,
                    "Comprehensive analysis of sleep-related brain activity patterns using large-scale modeling."
                ),
                
                new CodeData(
                    "One-hot HMM-GLM Brain State Discovery",
                    "Implementation of One-hot Generalized Linear Model for switching brain state discovery, reproducing ICLR 2024 paper findings",
                    "NOTEBOOK",
                    "Python",
                    "JAX",
                    Set.of("anqi.wu@nyu.edu", "chengrui.li@nyu.edu"),
                    Set.of("neurodata25", "brainml", "hmm-glm", "brain-states", "machine-learning"),
                    "onehot_hmmglm_analysis.ipynb",
                    "1.0",
                    null,
                    null,
                    "Advanced statistical modeling for brain state identification using GLM framework."
                ),
                
                new CodeData(
                    "NAMD Molecular Dynamics Workflow",
                    "Comprehensive molecular dynamics simulation workflow using NAMD with GPU acceleration for protein-ligand interactions",
                    "NOTEBOOK",
                    "Tcl",
                    "NAMD",
                    Set.of("md.researcher@illinois.edu", "namd.support@ks.uiuc.edu"),
                    Set.of("molecular-dynamics", "namd", "protein-simulation", "gpu-computing", "hpc"),
                    "namd_md_workflow.ipynb",
                    "2.14",
                    null,
                    null,
                    "Production molecular dynamics workflows with NAMD 2.14 and GPU support."
                ),
                
                // Research Code Repositories (following v1 GitHub pattern)
                new CodeData(
                    "Neural Oscillators Computing Framework",
                    "A comprehensive framework for computing with neural oscillators, including speech processing demos and neuromorphic computing applications",
                    "REPOSITORY",
                    "Python",
                    "NumPy",
                    Set.of("nabil.imam@intel.com", "nand.chandravadia@intel.com"),
                    Set.of("neurodata25", "imamlab", "neural-oscillators", "neuromorphic", "speech-processing"),
                    null,
                    null,
                    "https://github.com/cyber-shuttle/imamlab-neural-oscillators",
                    "main",
                    "Neural oscillator-based computing for neuromorphic applications and speech processing."
                ),
                
                new CodeData(
                    "Torch Brain and TemporalData Toolkit",
                    "Scaling up neural data analysis with torch_brain and temporaldata libraries for large-scale neuroscience data processing",
                    "REPOSITORY",
                    "Python",
                    "PyTorch",
                    Set.of("eva.dyer@gatech.edu", "vinam.arora@gatech.edu", "mahato.shivashriganesh@gatech.edu"),
                    Set.of("neurodata25", "nerdslab", "torch-brain", "temporaldata", "neuroscience"),
                    null,
                    null,
                    "https://github.com/cyber-shuttle/neurodata25_torchbrain_notebooks",
                    "main",
                    "Advanced neural data analysis tools with PyTorch integration for large-scale processing."
                ),
                
                new CodeData(
                    "NetFormer Neural Connectivity Model",
                    "Running the NetFormer model to bridge the gap between structure and function in the brain using transformer architectures",
                    "REPOSITORY",
                    "Python",
                    "Transformers",
                    Set.of("lu.mi@neuroaihub.org"),
                    Set.of("neurodata25", "neuroaihub", "netformer", "neural-connectivity", "transformers"),
                    null,
                    null,
                    "https://github.com/cyber-shuttle/neuroaihub-netformer",
                    "main",
                    "Transformer-based analysis of neural connectivity patterns in brain networks."
                )
            };
            
            // Create codes from sample data
            for (CodeData codeData : codeDataArray) {
                Code code = createCodeFromData(codeData);
                codeRepository.save(code);
            }
            
            LOGGER.info("Created {} code resources", codeDataArray.length);
        }
    }

    private Code createCodeFromData(CodeData data) {
        Code code = new Code();
        code.setName(data.name);
        code.setDescription(data.description);
        code.setCodeType(data.codeType);
        code.setProgrammingLanguage(data.programmingLanguage);
        code.setFramework(data.framework);
        code.setVersion(data.version);
        code.setFileName(data.fileName);
        code.setRepositoryUrl(data.repositoryUrl);
        code.setBranch(data.branch);
        code.setAdditionalInfo(data.additionalInfo);
        
        // Set default v1 Resource fields (inherited)
        code.setPrivacy(PrivacyEnum.PUBLIC);
        code.setState(StateEnum.ACTIVE);
        code.setStatus(StatusEnum.VERIFIED);
        code.setAuthors(new HashSet<>(data.authors));
        code.setTags(getOrCreateTags(data.tags));
        code.setHeaderImage(""); // Default empty header image
        
        return code;
    }

    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag existingTag = tagRepository.findByValue(tagName);
            if (existingTag != null) {
                tags.add(existingTag);
            } else {
                Tag newTag = new Tag();
                newTag.setValue(tagName);
                Tag savedTag = tagRepository.save(newTag);
                tags.add(savedTag);
            }
        }
        return tags;
    }


    // Helper class for organizing sample data
    private static class CodeData {
        final String name;
        final String description;
        final String codeType; // MODEL, NOTEBOOK, REPOSITORY
        final String programmingLanguage;
        final String framework;
        final Set<String> authors;
        final Set<String> tags;
        final String fileName; // For models and notebooks
        final String version;
        final String repositoryUrl; // For repositories
        final String branch; // For repositories
        final String additionalInfo;

        public CodeData(String name, String description, String codeType, String programmingLanguage, 
                       String framework, Set<String> authors, Set<String> tags, String fileName, 
                       String version, String repositoryUrl, String branch, String additionalInfo) {
            this.name = name;
            this.description = description;
            this.codeType = codeType;
            this.programmingLanguage = programmingLanguage;
            this.framework = framework;
            this.authors = authors;
            this.tags = tags;
            this.fileName = fileName;
            this.version = version;
            this.repositoryUrl = repositoryUrl;
            this.branch = branch;
            this.additionalInfo = additionalInfo;
        }
    }
}
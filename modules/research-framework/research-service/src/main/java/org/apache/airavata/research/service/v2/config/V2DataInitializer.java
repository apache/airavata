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
import org.apache.airavata.research.service.v2.entity.Code;
import org.apache.airavata.research.service.v2.entity.ComputeResource;
import org.apache.airavata.research.service.v2.entity.StorageResource;
import org.apache.airavata.research.service.v2.entity.TagV2;
import org.apache.airavata.research.service.v2.enums.PrivacyEnumV2;
import org.apache.airavata.research.service.v2.enums.StateEnumV2;
import org.apache.airavata.research.service.v2.enums.StatusEnumV2;
import org.apache.airavata.research.service.v2.repository.CodeRepository;
import org.apache.airavata.research.service.v2.repository.ComputeResourceRepository;
import org.apache.airavata.research.service.v2.repository.StorageResourceRepository;
import org.apache.airavata.research.service.v2.repository.TagV2Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class V2DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(V2DataInitializer.class);

    private final ComputeResourceRepository computeResourceRepository;
    private final StorageResourceRepository storageResourceRepository;
    private final CodeRepository codeRepository;
    private final TagV2Repository tagV2Repository;

    public V2DataInitializer(ComputeResourceRepository computeResourceRepository,
                           StorageResourceRepository storageResourceRepository,
                           CodeRepository codeRepository,
                           TagV2Repository tagV2Repository) {
        this.computeResourceRepository = computeResourceRepository;
        this.storageResourceRepository = storageResourceRepository;
        this.codeRepository = codeRepository;
        this.tagV2Repository = tagV2Repository;
    }

    @PostConstruct
    public void initializeData() {
        LOGGER.info("Initializing V2 mock data for compute, storage, and code resources...");
        
        try {
            initializeComputeResources();
            initializeStorageResources();
            initializeCodes();
            
            LOGGER.info("V2 mock data initialization completed.");
        } catch (Exception e) {
            LOGGER.error("Error during V2 data initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize V2 mock data", e);
        }
    }

    private void initializeComputeResources() {
        if (computeResourceRepository.count() == 0) {
            LOGGER.info("Creating mock compute resources...");
            
            List<ComputeResource> computeResources = List.of(
                new ComputeResource(
                    "Bridges-2 Supercomputer",
                    "Advanced high-performance computing cluster at Pittsburgh Supercomputing Center with GPU acceleration for AI workloads.",
                    "bridges2.psc.edu",
                    "HPC",
                    1280,
                    2560,
                    "CentOS 7",
                    "SLURM",
                    "Features GPU nodes for machine learning, regular memory and extreme memory configurations. Maximum job time: 48 hours.",
                    "Pittsburgh Supercomputing Center"
                ),
                
                new ComputeResource(
                    "Expanse Supercomputer",
                    "SDSC's newest supercomputer designed for scientific computing with specialized GPU nodes for machine learning and AI research.",
                    "expanse.sdsc.edu",
                    "HPC",
                    1408,
                    2816,
                    "CentOS 8",
                    "SLURM",
                    "CPU and GPU partitions available. Optimized for parallel computing and machine learning workloads.",
                    "San Diego Supercomputer Center"
                ),
                
                new ComputeResource(
                    "Anvil Cluster",
                    "Purdue University's advanced computing cluster with high-memory nodes and specialized hardware for data analytics.",
                    "anvil.rcac.purdue.edu",
                    "HPC",
                    1000,
                    4000,
                    "Red Hat Enterprise Linux 8",
                    "SLURM",
                    "High-memory nodes (1.5TB RAM), GPU nodes with V100 and A100 cards for deep learning applications.",
                    "Purdue University RCAC"
                ),
                
                new ComputeResource(
                    "Frontera Supercomputer",
                    "Leadership-class supercomputer at TACC for large-scale computational research and simulation.",
                    "frontera.tacc.utexas.edu",
                    "HPC",
                    8008,
                    16000,
                    "CentOS 7",
                    "SLURM",
                    "Leadership computing facility with specialized queues for different workload types. Maximum allocation: 3M core-hours.",
                    "Texas Advanced Computing Center"
                ),
                
                new ComputeResource(
                    "AWS EC2 Compute Cloud",
                    "Scalable cloud computing platform with on-demand instance provisioning and auto-scaling capabilities.",
                    "amazonaws.com",
                    "Cloud",
                    9999,
                    99999,
                    "Amazon Linux 2",
                    "Cloud Native",
                    "Pay-as-you-go pricing model with various instance types (CPU, memory, GPU optimized). Global availability zones.",
                    "Amazon Web Services"
                ),
                
                new ComputeResource(
                    "Google Cloud Compute Engine",
                    "High-performance virtual machines with custom machine types and specialized accelerators for AI/ML workloads.",
                    "compute.googleapis.com",
                    "Cloud",
                    8888,
                    88888,
                    "Ubuntu 20.04 LTS",
                    "Cloud Native",
                    "Preemptible instances available for cost savings. TPUs available for machine learning acceleration.",
                    "Google Cloud Platform"
                ),
                
                new ComputeResource(
                    "XSEDE Comet",
                    "GPU-accelerated supercomputer optimized for data-intensive computing and machine learning applications.",
                    "comet.sdsc.edu",
                    "HPC",
                    1980,
                    2640,
                    "CentOS 7",
                    "SLURM",
                    "72 GPU nodes with K80 cards, high-speed interconnect, and parallel file systems for data-intensive research.",
                    "San Diego Supercomputer Center"
                ),
                
                new ComputeResource(
                    "Jetstream2 Cloud",
                    "National cyberinfrastructure providing on-demand virtual machines for academic research computing.",
                    "jetstream-cloud.org",
                    "Cloud",
                    2000,
                    8000,
                    "Various Linux Distributions",
                    "OpenStack",
                    "Self-service cloud environment with support for containers, Kubernetes, and Jupyter notebooks.",
                    "Indiana University & TACC"
                ),
                
                new ComputeResource(
                    "NERSC Perlmutter",
                    "Exascale-class supercomputer with GPU acceleration designed for scientific computing and AI convergence.",
                    "perlmutter.nersc.gov",
                    "HPC",
                    6159,
                    4915,
                    "SUSE Linux Enterprise",
                    "SLURM",
                    "A100 GPU nodes optimized for mixed-precision computing. Advanced interconnect and parallel file systems.",
                    "National Energy Research Scientific Computing Center"
                )
            );
            
            computeResourceRepository.saveAll(computeResources);
            LOGGER.info("Created {} compute resources", computeResources.size());
        }
    }

    private void initializeStorageResources() {
        if (storageResourceRepository.count() == 0) {
            LOGGER.info("Creating mock storage resources...");
            
            List<StorageResource> storageResources = List.of(
                new StorageResource(
                    "TACC Ranch Archive",
                    "Petascale archival storage system for long-term data preservation and backup with tape-based architecture.",
                    "ranch.tacc.utexas.edu",
                    "Archive Storage",
                    20000L,
                    "SFTP",
                    "ranch.tacc.utexas.edu:22",
                    true,
                    false,
                    "Hierarchical storage management with automatic data migration. Designed for long-term archival.",
                    "Texas Advanced Computing Center"
                ),
                
                new StorageResource(
                    "XSEDE Globus Data Transfer",
                    "High-performance data transfer and sharing service connecting research institutions worldwide.",
                    "globus.org",
                    "Data Transfer",
                    50000L,
                    "GridFTP",
                    "https://transfer.api.globusonline.org",
                    true,
                    true,
                    "Parallel data transfer with resumption capabilities. Supports endpoint-to-endpoint transfers.",
                    "University of Chicago & XSEDE"
                ),
                
                new StorageResource(
                    "AWS S3 Object Storage",
                    "Highly scalable object storage service with 99.999999999% durability and global accessibility.",
                    "s3.amazonaws.com",
                    "Object Storage",
                    999999L,
                    "S3 API",
                    "https://s3.amazonaws.com",
                    true,
                    true,
                    "Multiple storage classes (Standard, IA, Glacier) with lifecycle policies for automatic cost optimization.",
                    "Amazon Web Services"
                ),
                
                new StorageResource(
                    "Google Cloud Storage",
                    "Unified object storage platform with multi-regional availability and integrated machine learning capabilities.",
                    "storage.googleapis.com",
                    "Object Storage",
                    888888L,
                    "S3 Compatible API",
                    "https://storage.googleapis.com",
                    true,
                    true,
                    "Integrated with BigQuery and AI Platform. Supports both hot and cold storage tiers.",
                    "Google Cloud Platform"
                ),
                
                new StorageResource(
                    "NERSC HPSS Archive",
                    "High Performance Storage System providing long-term archival storage for scientific data.",
                    "archive.nersc.gov",
                    "Archive Storage",
                    30000L,
                    "HPSS",
                    "hpss://archive.nersc.gov",
                    true,
                    false,
                    "Hierarchical storage management with robotic tape library. Optimized for large scientific datasets.",
                    "National Energy Research Scientific Computing Center"
                ),
                
                new StorageResource(
                    "Open Science Data Federation",
                    "Distributed data federation providing access to scientific datasets across multiple institutions.",
                    "osdf.osg-htc.org",
                    "Distributed Storage",
                    15000L,
                    "HTTP/HTTPS",
                    "https://osdf.osg-htc.org",
                    true,
                    false,
                    "Caching infrastructure for efficient data distribution. Supports both public and authenticated access.",
                    "Open Science Grid"
                ),
                
                new StorageResource(
                    "SDSC Data Oasis",
                    "High-performance parallel file system designed for data-intensive computing and analytics workflows.",
                    "oasis.sdsc.edu",
                    "Parallel File System",
                    12000L,
                    "NFS/Lustre",
                    "/oasis/projects",
                    false,
                    false,
                    "Lustre-based parallel file system with high IOPS capability. Optimized for concurrent access patterns.",
                    "San Diego Supercomputer Center"
                ),
                
                new StorageResource(
                    "CyVerse Data Store",
                    "Comprehensive data management platform for life sciences research with integrated analysis tools.",
                    "datastore.cyverse.org",
                    "Research Data Platform",
                    25000L,
                    "iRODS",
                    "https://data.cyverse.org",
                    true,
                    true,
                    "Metadata management, data sharing, and integrated analysis workflows. Specialized for life sciences.",
                    "University of Arizona CyVerse"
                ),
                
                new StorageResource(
                    "HDF5 Cloud Storage",
                    "Cloud-optimized storage service designed specifically for HDF5 datasets and scientific data formats.",
                    "hdf5.cloud",
                    "Scientific Data Storage",
                    5000L,
                    "REST API",
                    "https://api.hdf5.cloud",
                    true,
                    true,
                    "Native support for HDF5 datasets with cloud-optimized access patterns and metadata indexing.",
                    "HDF Group"
                )
            );
            
            storageResourceRepository.saveAll(storageResources);
            LOGGER.info("Created {} storage resources", storageResources.size());
        }
    }

    private void initializeCodes() {
        if (codeRepository.count() == 0) {
            LOGGER.info("Creating mock code resources...");
            
            // Sample data configurations
            CodeData[] codeDataArray = {
                // Model-type codes
                new CodeData(
                    "COVID-19 Chest X-ray Classification Model",
                    "Deep learning model for automatic detection of COVID-19 pneumonia from chest X-ray images using ResNet-50 architecture with transfer learning.",
                    "MODEL",
                    "Python",
                    "TensorFlow",
                    Set.of("dr.sarah.medical@stanford.edu", "alex.vision@mit.edu"),
                    Set.of("medical", "computer-vision", "covid-19", "deep-learning", "classification"),
                    "covid_xray_classifier_v2.1",
                    "2.1",
                    null,
                    null,
                    "Pre-trained model weights available. Compatible with standard ML pipelines."
                ),
                
                new CodeData(
                    "Financial Fraud Detection Model",
                    "Machine learning ensemble model combining XGBoost and Random Forest for real-time credit card fraud detection with 99.2% accuracy.",
                    "MODEL",
                    "Python",
                    "Scikit-learn",
                    Set.of("mike.finance@jpmorgan.com", "lisa.ml@visa.com"),
                    Set.of("finance", "fraud-detection", "machine-learning", "ensemble", "xgboost"),
                    "fraud_detector_ensemble_v1.3",
                    "1.3",
                    null,
                    null,
                    "Pre-trained model weights available. Compatible with standard ML pipelines."
                ),
                
                new CodeData(
                    "Protein Folding Prediction Model",
                    "AlphaFold2-inspired neural network for predicting 3D protein structures from amino acid sequences using attention mechanisms.",
                    "MODEL",
                    "Python", 
                    "PyTorch",
                    Set.of("prof.chen@deepmind.com", "bio.researcher@harvard.edu"),
                    Set.of("bioinformatics", "protein-folding", "deep-learning", "attention", "alphafold"),
                    "protein_fold_predictor_v3.0",
                    "3.0",
                    null,
                    null,
                    "Pre-trained model weights available. Compatible with standard ML pipelines."
                ),
                
                // Notebook-type codes
                new CodeData(
                    "Cybersecurity Threat Analysis Notebook",
                    "Comprehensive Jupyter notebook for analyzing network traffic patterns and identifying potential cybersecurity threats using statistical analysis.",
                    "NOTEBOOK",
                    "Python",
                    null,
                    Set.of("security.analyst@cisco.com", "threat.hunter@crowdstrike.com"),
                    Set.of("cybersecurity", "threat-analysis", "network-security", "statistical-analysis", "jupyter"),
                    null,
                    null,
                    "/notebooks/cybersecurity/threat_analysis.ipynb",
                    null,
                    "Interactive Jupyter notebook with step-by-step analysis and visualizations."
                ),
                
                new CodeData(
                    "Climate Data Visualization Notebook", 
                    "Interactive data visualization notebook for climate change analysis using NOAA datasets with advanced plotting and statistical modeling.",
                    "NOTEBOOK",
                    "Python",
                    null,
                    Set.of("climate.scientist@noaa.gov", "data.viz@nasa.gov"),
                    Set.of("climate-science", "data-visualization", "environmental", "statistical-modeling", "matplotlib"),
                    null,
                    null,
                    "/notebooks/climate/climate_visualization.ipynb",
                    null,
                    "Interactive Jupyter notebook with step-by-step analysis and visualizations."
                ),
                
                new CodeData(
                    "NLP Sentiment Analysis Notebook",
                    "End-to-end natural language processing pipeline for sentiment analysis of social media data using transformer models and BERT.",
                    "NOTEBOOK",
                    "Python",
                    null,
                    Set.of("nlp.researcher@google.com", "sentiment.expert@twitter.com"),
                    Set.of("nlp", "sentiment-analysis", "transformers", "bert", "social-media"),
                    null,
                    null,
                    "/notebooks/nlp/sentiment_analysis.ipynb",
                    null,
                    "Interactive Jupyter notebook with step-by-step analysis and visualizations."
                ),
                
                // Repository-type codes
                new CodeData(
                    "Distributed Machine Learning Framework",
                    "Open-source framework for distributed machine learning across multiple compute nodes with fault tolerance and auto-scaling capabilities.",
                    "REPOSITORY",
                    "Python",
                    "PyTorch",
                    Set.of("distributed.ml@uber.com", "framework.dev@netflix.com"),
                    Set.of("distributed-computing", "machine-learning", "framework", "pytorch", "scalability"),
                    null,
                    null,
                    null,
                    "https://github.com/ml-distributed/framework",
                    "Full source code repository with documentation, tests, and CI/CD pipeline."
                ),
                
                new CodeData(
                    "Quantum Computing Algorithms Library",
                    "Comprehensive library of quantum computing algorithms implemented in Qiskit with educational examples and benchmarking tools.",
                    "REPOSITORY",
                    "Python", 
                    "Qiskit",
                    Set.of("quantum.researcher@ibm.com", "algorithms.expert@google.com"),
                    Set.of("quantum-computing", "algorithms", "qiskit", "benchmarking", "education"),
                    null,
                    null,
                    null,
                    "https://github.com/quantum-algorithms/qiskit-library",
                    "Full source code repository with documentation, tests, and CI/CD pipeline."
                ),
                
                new CodeData(
                    "Time Series Forecasting Toolkit",
                    "Advanced time series analysis and forecasting toolkit with support for ARIMA, LSTM, and Prophet models for financial and IoT data.",
                    "REPOSITORY",
                    "Python",
                    "TensorFlow", 
                    Set.of("time.series@bloomberg.com", "forecasting.expert@amazon.com"),
                    Set.of("time-series", "forecasting", "arima", "lstm", "prophet", "financial"),
                    null,
                    null,
                    null,
                    "https://github.com/timeseries-toolkit/forecasting",
                    "Full source code repository with documentation, tests, and CI/CD pipeline."
                )
            };
            
            for (CodeData codeData : codeDataArray) {
                try {
                    Code code = createCodeWithTags(codeData);
                    codeRepository.save(code);
                } catch (Exception e) {
                    LOGGER.error("Error creating code '{}': {}", codeData.name, e.getMessage(), e);
                }
            }
            
            LOGGER.info("Created {} code resources", codeDataArray.length);
        }
    }

    /**
     * Helper method to create Code entity with proper TagV2 associations
     */
    private Code createCodeWithTags(CodeData codeData) {
        // Create or get existing TagV2 entities
        Set<TagV2> tagEntities = getOrCreateTags(codeData.tagStrings);
        
        // Create Code entity using the constructor
        Code code = new Code(codeData.name, codeData.description, codeData.codeType, 
                           codeData.programmingLanguage, codeData.framework, 
                           codeData.authors, tagEntities);
        
        // Set enum-based fields with proper defaults
        code.setStatus(StatusEnumV2.VERIFIED);
        code.setState(StateEnumV2.ACTIVE);
        code.setPrivacy(PrivacyEnumV2.PUBLIC);
        
        // Set random star count for demonstration
        int starCount = (int) (Math.random() * 1000) + 10;
        code.setStarCount(starCount);
        
        // Set code-specific fields
        if (codeData.applicationInterfaceId != null) {
            code.setApplicationInterfaceId(codeData.applicationInterfaceId);
        }
        if (codeData.version != null) {
            code.setVersion(codeData.version);
        }
        if (codeData.notebookPath != null) {
            code.setNotebookPath(codeData.notebookPath);
        }
        if (codeData.repositoryUrl != null) {
            code.setRepositoryUrl(codeData.repositoryUrl);
        }
        if (codeData.additionalInfo != null) {
            code.setAdditionalInfo(codeData.additionalInfo);
        }
        
        // Set header image - use a default for now
        code.setHeaderImage("https://via.placeholder.com/400x200?text=" + codeData.codeType);
        
        // Add dependencies based on programming language and framework
        addDependencies(code);
        
        return code;
    }

    /**
     * Helper method to get or create TagV2 entities from tag strings
     */
    private Set<TagV2> getOrCreateTags(Set<String> tagStrings) {
        Set<TagV2> tagEntities = new HashSet<>();
        
        for (String tagString : tagStrings) {
            Optional<TagV2> existingTag = tagV2Repository.findByTagValue(tagString);
            if (existingTag.isPresent()) {
                tagEntities.add(existingTag.get());
            } else {
                // Create new tag
                TagV2 newTag = new TagV2();
                newTag.setTagValue(tagString);
                TagV2 savedTag = tagV2Repository.save(newTag);
                tagEntities.add(savedTag);
                LOGGER.debug("Created new tag: {}", tagString);
            }
        }
        
        return tagEntities;
    }

    /**
     * Helper method to add dependencies based on programming language and framework
     */
    private void addDependencies(Code code) {
        if ("Python".equals(code.getProgrammingLanguage())) {
            code.getDependencies().addAll(Set.of("numpy", "pandas", "matplotlib"));
            
            String framework = code.getFramework();
            if ("TensorFlow".equals(framework)) {
                code.getDependencies().addAll(Set.of("tensorflow>=2.8.0", "keras"));
            } else if ("PyTorch".equals(framework)) {
                code.getDependencies().addAll(Set.of("torch>=1.12.0", "torchvision"));
            } else if ("Scikit-learn".equals(framework)) {
                code.getDependencies().addAll(Set.of("scikit-learn>=1.1.0", "joblib"));
            } else if ("Qiskit".equals(framework)) {
                code.getDependencies().addAll(Set.of("qiskit>=0.39.0", "qiskit-aer"));
            }
        }
    }

    /**
     * Data structure to hold code initialization data
     */
    private static class CodeData {
        final String name;
        final String description;
        final String codeType;
        final String programmingLanguage;
        final String framework;
        final Set<String> authors;
        final Set<String> tagStrings;
        final String applicationInterfaceId;
        final String version;
        final String notebookPath;
        final String repositoryUrl;
        final String additionalInfo;

        CodeData(String name, String description, String codeType, String programmingLanguage,
                String framework, Set<String> authors, Set<String> tagStrings,
                String applicationInterfaceId, String version, String notebookPath,
                String repositoryUrl, String additionalInfo) {
            this.name = name;
            this.description = description;
            this.codeType = codeType;
            this.programmingLanguage = programmingLanguage;
            this.framework = framework;
            this.authors = authors != null ? authors : new HashSet<>();
            this.tagStrings = tagStrings != null ? tagStrings : new HashSet<>();
            this.applicationInterfaceId = applicationInterfaceId;
            this.version = version;
            this.notebookPath = notebookPath;
            this.repositoryUrl = repositoryUrl;
            this.additionalInfo = additionalInfo;
        }
    }
}
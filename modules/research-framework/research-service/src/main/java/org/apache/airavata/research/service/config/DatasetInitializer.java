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

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Tag;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Dataset Initializer for creating sample dataset resources
 * Runs automatically without requiring dev-local profile
 */
@Component
public class DatasetInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetInitializer.class);

    private final ResourceRepository resourceRepository;
    private final TagRepository tagRepository;

    public DatasetInitializer(ResourceRepository resourceRepository, TagRepository tagRepository) {
        this.resourceRepository = resourceRepository;
        this.tagRepository = tagRepository;
    }

    @PostConstruct
    public void initializeDatasets() {
        LOGGER.info("Initializing dataset resources...");
        
        try {
            // Only initialize if no datasets exist
            long datasetCount = resourceRepository.findAll().stream()
                .filter(resource -> resource instanceof DatasetResource)
                .count();
                
            if (datasetCount == 0) {
                LOGGER.info("Creating sample dataset resources...");
                createSampleDatasets();
                LOGGER.info("Dataset initialization completed.");
            } else {
                LOGGER.info("Datasets already exist. Skipping initialization.");
            }
        } catch (Exception e) {
            LOGGER.error("Error during dataset initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize dataset resources", e);
        }
    }

    private void createSampleDatasets() {
        // Create diverse sample datasets
        DatasetData[] datasetArray = {
            new DatasetData(
                "Lung CT Scans Database",
                "A comprehensive collection of 3D lung CT images for medical imaging research and machine learning model training.",
                "lung-ct-scans-db",
                Set.of("medical@imaging.lab", "ssaggi3@gatech.edu"),
                Set.of("medical", "ct-scans", "lungs", "imaging")
            ),
            
            new DatasetData(
                "Financial Fraud Dataset",
                "Large-scale dataset of credit card transactions with fraud labels for developing fraud detection systems.",
                "financial-fraud-dataset",
                Set.of("fraud@detection.org", "security@fintech.com"),
                Set.of("finance", "fraud", "transactions", "cybersecurity")
            ),
            
            new DatasetData(
                "Stock Market Data",
                "Historical stock prices and trading volumes for major market indices over the past 20 years.",
                "stock-market-historical-data",
                Set.of("market@data.finance", "trading@analytics.com"),
                Set.of("finance", "stocks", "time-series", "trading")
            ),
            
            new DatasetData(
                "Drug Compound Library",
                "Chemical compound structures and properties for pharmaceutical research and drug discovery.",
                "drug-compound-library",
                Set.of("pharma@research.edu", "compounds@drugdev.org"),
                Set.of("healthcare", "compounds", "pharmaceuticals", "chemistry")
            ),
            
            new DatasetData(
                "Social Media Sentiment",
                "Annotated social media posts with sentiment labels for natural language processing and sentiment analysis.",
                "social-media-sentiment-dataset",
                Set.of("nlp@sentiment.lab", "text@analysis.ai"),
                Set.of("nlp", "sentiment", "social-media", "text")
            ),
            
            new DatasetData(
                "Protein Sequences",
                "Large collection of protein sequences with structural annotations for bioinformatics research.",
                "protein-sequences-annotated",
                Set.of("bio@sequences.org", "proteins@research.edu"),
                Set.of("life-sciences", "proteins", "sequences", "bioinformatics")
            ),
            
            new DatasetData(
                "ImageNet Subset",
                "Curated subset of ImageNet for computer vision benchmarking and deep learning model evaluation.",
                "imagenet-curated-subset",
                Set.of("vision@datasets.org", "images@cv.lab"),
                Set.of("computer-vision", "images", "classification", "benchmark")
            ),
            
            new DatasetData(
                "Malware Samples",
                "Classified malware samples for cybersecurity research and threat detection system training.",
                "malware-samples-classified",
                Set.of("security@malware.lab", "threats@cyber.defense"),
                Set.of("cybersecurity", "malware", "security", "threats")
            ),
            
            new DatasetData(
                "Climate Data Collection",
                "Long-term climate measurements including temperature, precipitation, and atmospheric data.",
                "climate-data-collection",
                Set.of("climate@research.org", "weather@prediction.lab"),
                Set.of("climate", "environmental", "weather", "data-analysis")
            )
        };

        // Create datasets from sample data
        for (DatasetData datasetData : datasetArray) {
            DatasetResource dataset = createDatasetFromData(datasetData);
            resourceRepository.save(dataset);
        }
        
        LOGGER.info("Created {} dataset resources", datasetArray.length);
    }

    private DatasetResource createDatasetFromData(DatasetData data) {
        DatasetResource dataset = new DatasetResource();
        dataset.setName(data.name);
        dataset.setDescription(data.description);
        dataset.setDatasetUrl(data.datasetUrl);
        
        // Set default Resource fields (inherited)
        dataset.setPrivacy(PrivacyEnum.PUBLIC);
        dataset.setState(StateEnum.ACTIVE);
        dataset.setStatus(StatusEnum.VERIFIED);
        dataset.setAuthors(new HashSet<>(data.authors));
        dataset.setTags(getOrCreateTags(data.tags));
        dataset.setHeaderImage(""); // Default empty header image
        
        return dataset;
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
    private static class DatasetData {
        final String name;
        final String description;
        final String datasetUrl;
        final Set<String> authors;
        final Set<String> tags;

        public DatasetData(String name, String description, String datasetUrl, 
                          Set<String> authors, Set<String> tags) {
            this.name = name;
            this.description = description;
            this.datasetUrl = datasetUrl;
            this.authors = authors;
            this.tags = tags;
        }
    }
}
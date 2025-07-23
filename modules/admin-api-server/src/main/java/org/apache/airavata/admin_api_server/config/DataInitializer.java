package org.apache.airavata.admin_api_server.config;

import org.apache.airavata.admin_api_server.entity.Dataset;
import org.apache.airavata.admin_api_server.entity.Model;
import org.apache.airavata.admin_api_server.entity.Notebook;
import org.apache.airavata.admin_api_server.entity.Repository;
import org.apache.airavata.admin_api_server.entity.StorageResource;
import org.apache.airavata.admin_api_server.entity.ComputeResource;
import org.apache.airavata.admin_api_server.repository.DatasetRepository;
import org.apache.airavata.admin_api_server.repository.ModelRepository;
import org.apache.airavata.admin_api_server.repository.NotebookRepository;
import org.apache.airavata.admin_api_server.repository.RepositoryRepository;
import org.apache.airavata.admin_api_server.repository.StorageResourceRepository;
import org.apache.airavata.admin_api_server.repository.ComputeResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private StorageResourceRepository storageResourceRepository;

    @Autowired
    private ComputeResourceRepository computeResourceRepository;

    @Override
    public void run(String... args) throws Exception {
        if (modelRepository.count() == 0) {
            initializeModels();
        }
        if (datasetRepository.count() == 0) {
            initializeDatasets();
        }
        if (notebookRepository.count() == 0) {
            initializeNotebooks();
        }
        if (repositoryRepository.count() == 0) {
            initializeRepositories();
        }
        if (storageResourceRepository.count() == 0) {
            initializeStorageResources();
        }
        if (computeResourceRepository.count() == 0) {
            initializeComputeResources();
        }
    }

    private void initializeModels() {
        // Create diverse sample models
        Model model1 = new Model(
            "Lung Nodule Classifier",
            "A deep learning model trained to detect and classify lung nodules in CT scan images.",
            Arrays.asList("medical", "classification", "lungs", "deep-learning"),
            Arrays.asList("ssaggi3@gatech.edu", "swen6@gatech.edu", "pkadekodi3@gatech.edu"),
            "medical"
        );
        model1.setStarCount(42);

        Model model2 = new Model(
            "Fraud Detection System",
            "Machine learning model for detecting fraudulent transactions in real-time.",
            Arrays.asList("cybersecurity", "fraud", "ml", "classification"),
            Arrays.asList("alex.smith@university.edu", "maria.garcia@research.org"),
            "cyber_security"
        );
        model2.setStarCount(28);

        Model model3 = new Model(
            "Stock Price Predictor",
            "LSTM neural network for predicting stock market trends and prices.",
            Arrays.asList("finance", "lstm", "time-series", "prediction"),
            Arrays.asList("john.doe@fintech.com", "sarah.wilson@trading.ai"),
            "finance"
        );
        model3.setStarCount(35);

        Model model4 = new Model(
            "Drug Discovery Platform",
            "AI model for accelerating pharmaceutical drug discovery processes.",
            Arrays.asList("healthcare", "drug-discovery", "ai", "pharmaceutical"),
            Arrays.asList("dr.patel@pharma.edu", "research@biotech.org"),
            "healthcare"
        );
        model4.setStarCount(67);

        Model model5 = new Model(
            "Sentiment Analysis Tool",
            "Natural language processing model for analyzing text sentiment and emotions.",
            Arrays.asList("nlp", "sentiment", "text-analysis", "emotion"),
            Arrays.asList("anna.chen@nlp.institute", "mark.brown@textai.com"),
            "nlp"
        );
        model5.setStarCount(19);

        Model model6 = new Model(
            "Protein Structure Predictor",
            "Advanced model for predicting 3D protein structures from amino acid sequences.",
            Arrays.asList("life-sciences", "protein", "structure", "bioinformatics"),
            Arrays.asList("prof.kim@biology.edu", "lab@proteins.research"),
            "life_sciences"
        );
        model6.setStarCount(53);

        Model model7 = new Model(
            "Image Classification CNN",
            "Convolutional neural network for general-purpose image classification tasks.",
            Arrays.asList("computer-vision", "cnn", "image-classification", "deep-learning"),
            Arrays.asList("vision@ai.lab", "david.jones@cv.research"),
            "computer_vision"
        );
        model7.setStarCount(31);

        Model model8 = new Model(
            "Malware Detection Engine",
            "Cybersecurity model for detecting and classifying malicious software.",
            Arrays.asList("cybersecurity", "malware", "detection", "security"),
            Arrays.asList("security@cyber.defense", "analyst@threat.intel"),
            "cyber_security"
        );
        model8.setStarCount(44);

        Model model9 = new Model(
            "Credit Risk Assessment",
            "Financial model for evaluating loan default risk and credit worthiness.",
            Arrays.asList("finance", "risk", "credit", "banking"),
            Arrays.asList("risk@banking.corp", "finance@credit.ai"),
            "finance"
        );
        model9.setStarCount(22);

        modelRepository.save(model1);
        modelRepository.save(model2);
        modelRepository.save(model3);
        modelRepository.save(model4);
        modelRepository.save(model5);
        modelRepository.save(model6);
        modelRepository.save(model7);
        modelRepository.save(model8);
        modelRepository.save(model9);
    }

    private void initializeDatasets() {
        // Create diverse sample datasets
        Dataset dataset1 = new Dataset(
            "Lung CT Scans Database",
            "A comprehensive collection of 3D lung CT images for medical imaging research.",
            Arrays.asList("medical", "ct-scans", "lungs", "imaging"),
            Arrays.asList("ssaggi3@gatech.edu", "medical@imaging.lab"),
            "medical"
        );
        dataset1.setStarCount(84);

        Dataset dataset2 = new Dataset(
            "Financial Fraud Dataset",
            "Large-scale dataset of credit card transactions with fraud labels.",
            Arrays.asList("finance", "fraud", "transactions", "cybersecurity"),
            Arrays.asList("fraud@detection.org", "security@fintech.com"),
            "cyber_security"
        );
        dataset2.setStarCount(56);

        Dataset dataset3 = new Dataset(
            "Stock Market Data",
            "Historical stock prices and trading volumes for major market indices.",
            Arrays.asList("finance", "stocks", "time-series", "trading"),
            Arrays.asList("market@data.finance", "trading@analytics.com"),
            "finance"
        );
        dataset3.setStarCount(73);

        Dataset dataset4 = new Dataset(
            "Drug Compound Library",
            "Chemical compound structures and properties for pharmaceutical research.",
            Arrays.asList("healthcare", "compounds", "pharmaceuticals", "chemistry"),
            Arrays.asList("pharma@research.edu", "compounds@drugdev.org"),
            "healthcare"
        );
        dataset4.setStarCount(45);

        Dataset dataset5 = new Dataset(
            "Social Media Sentiment",
            "Annotated social media posts with sentiment labels for NLP training.",
            Arrays.asList("nlp", "sentiment", "social-media", "text"),
            Arrays.asList("nlp@sentiment.lab", "text@analysis.ai"),
            "nlp"
        );
        dataset5.setStarCount(62);

        Dataset dataset6 = new Dataset(
            "Protein Sequences",
            "Large collection of protein sequences with structural annotations.",
            Arrays.asList("life-sciences", "proteins", "sequences", "bioinformatics"),
            Arrays.asList("bio@sequences.org", "proteins@research.edu"),
            "life_sciences"
        );
        dataset6.setStarCount(38);

        Dataset dataset7 = new Dataset(
            "ImageNet Subset",
            "Curated subset of ImageNet for computer vision benchmarking.",
            Arrays.asList("computer-vision", "images", "classification", "benchmark"),
            Arrays.asList("vision@datasets.org", "images@cv.lab"),
            "computer_vision"
        );
        dataset7.setStarCount(91);

        Dataset dataset8 = new Dataset(
            "Malware Samples",
            "Classified malware samples for cybersecurity research and training.",
            Arrays.asList("cybersecurity", "malware", "security", "threats"),
            Arrays.asList("security@malware.lab", "threats@cyber.defense"),
            "cyber_security"
        );
        dataset8.setStarCount(27);

        Dataset dataset9 = new Dataset(
            "Credit Risk Data",
            "Loan application data with default outcomes for risk modeling.",
            Arrays.asList("finance", "credit", "risk", "banking"),
            Arrays.asList("risk@banking.data", "credit@financial.ai"),
            "finance"
        );
        dataset9.setStarCount(49);

        datasetRepository.save(dataset1);
        datasetRepository.save(dataset2);
        datasetRepository.save(dataset3);
        datasetRepository.save(dataset4);
        datasetRepository.save(dataset5);
        datasetRepository.save(dataset6);
        datasetRepository.save(dataset7);
        datasetRepository.save(dataset8);
        datasetRepository.save(dataset9);
    }

    private void initializeNotebooks() {
        // Create diverse sample notebooks
        Notebook notebook1 = new Notebook(
            "COVID-19 Case Forecasting",
            "A time-series analysis notebook using real-world data to model and predict COVID-19 case trends.",
            Arrays.asList("epidemiology", "forecasting", "time-series", "healthcare"),
            Arrays.asList("ssaggi3@gatech.edu", "swen6@gatech.edu", "pkadekodi3@gatech.edu"),
            "healthcare"
        );
        notebook1.setStarCount(42);

        Notebook notebook2 = new Notebook(
            "Financial Risk Analysis",
            "Jupyter notebook for analyzing portfolio risk and market volatility using Python.",
            Arrays.asList("finance", "risk", "portfolio", "python"),
            Arrays.asList("finance@analyst.com", "risk@trading.org"),
            "finance"
        );
        notebook2.setStarCount(38);

        Notebook notebook3 = new Notebook(
            "Protein Folding Simulation",
            "Computational notebook for simulating protein folding dynamics using molecular dynamics.",
            Arrays.asList("bioinformatics", "protein", "simulation", "molecular-dynamics"),
            Arrays.asList("bio@research.edu", "proteins@simulation.lab"),
            "life_sciences"
        );
        notebook3.setStarCount(56);

        Notebook notebook4 = new Notebook(
            "Cybersecurity Threat Detection",
            "Network analysis notebook for detecting and classifying cybersecurity threats.",
            Arrays.asList("cybersecurity", "threat-detection", "network", "analysis"),
            Arrays.asList("security@cyber.lab", "threats@detection.org"),
            "cyber_security"
        );
        notebook4.setStarCount(29);

        Notebook notebook5 = new Notebook(
            "Computer Vision Pipeline",
            "Image processing and computer vision workflow for object detection and classification.",
            Arrays.asList("computer-vision", "image-processing", "object-detection", "python"),
            Arrays.asList("vision@cv.lab", "images@processing.ai"),
            "computer_vision"
        );
        notebook5.setStarCount(51);

        Notebook notebook6 = new Notebook(
            "Natural Language Processing",
            "Text analysis and NLP techniques for sentiment analysis and entity recognition.",
            Arrays.asList("nlp", "text-analysis", "sentiment", "entity-recognition"),
            Arrays.asList("nlp@text.lab", "language@processing.ai"),
            "nlp"
        );
        notebook6.setStarCount(34);

        Notebook notebook7 = new Notebook(
            "Drug Discovery Analysis",
            "Pharmaceutical research notebook for drug compound analysis and screening.",
            Arrays.asList("pharmaceutical", "drug-discovery", "compounds", "screening"),
            Arrays.asList("pharma@discovery.org", "drugs@research.lab"),
            "healthcare"
        );
        notebook7.setStarCount(47);

        Notebook notebook8 = new Notebook(
            "Machine Learning Tutorial",
            "Educational notebook covering fundamental machine learning algorithms and implementations.",
            Arrays.asList("machine-learning", "tutorial", "algorithms", "education"),
            Arrays.asList("ml@education.org", "tutorial@learning.ai"),
            "education"
        );
        notebook8.setStarCount(63);

        Notebook notebook9 = new Notebook(
            "Climate Data Analysis",
            "Environmental data analysis for climate change research and weather prediction.",
            Arrays.asList("climate", "environmental", "weather", "data-analysis"),
            Arrays.asList("climate@research.org", "weather@prediction.lab"),
            "environmental"
        );
        notebook9.setStarCount(25);

        notebookRepository.save(notebook1);
        notebookRepository.save(notebook2);
        notebookRepository.save(notebook3);
        notebookRepository.save(notebook4);
        notebookRepository.save(notebook5);
        notebookRepository.save(notebook6);
        notebookRepository.save(notebook7);
        notebookRepository.save(notebook8);
        notebookRepository.save(notebook9);
    }

    private void initializeRepositories() {
        // Create diverse sample repositories
        Repository repo1 = new Repository(
            "Lung CT Preprocessing Pipeline",
            "Scripts and tools for preprocessing Lung CT scans, including normalization and segmentation.",
            Arrays.asList("medical", "preprocessing", "lungs", "python"),
            Arrays.asList("ssaggi3@gatech.edu", "swen6@gatech.edu", "pkadekodi3@gatech.edu"),
            "medical"
        );
        repo1.setStarCount(73);

        Repository repo2 = new Repository(
            "Fraud Detection Framework",
            "Open-source Python framework for building real-time fraud detection systems.",
            Arrays.asList("cybersecurity", "fraud", "python", "framework"),
            Arrays.asList("security@fraud.detection", "dev@fintech.security"),
            "cyber_security"
        );
        repo2.setStarCount(68);

        Repository repo3 = new Repository(
            "Stock Trading Bot",
            "Automated trading bot with machine learning for stock market analysis and execution.",
            Arrays.asList("finance", "trading", "automation", "machine-learning"),
            Arrays.asList("trader@quant.finance", "bot@trading.ai"),
            "finance"
        );
        repo3.setStarCount(91);

        Repository repo4 = new Repository(
            "Protein Analysis Toolkit",
            "Bioinformatics toolkit for protein sequence analysis and structure prediction.",
            Arrays.asList("bioinformatics", "protein", "analysis", "toolkit"),
            Arrays.asList("bio@toolkit.org", "protein@analysis.lab"),
            "life_sciences"
        );
        repo4.setStarCount(45);

        Repository repo5 = new Repository(
            "Computer Vision Library",
            "Comprehensive computer vision library with deep learning models for image processing.",
            Arrays.asList("computer-vision", "deep-learning", "library", "image-processing"),
            Arrays.asList("vision@cv.library", "dev@image.ai"),
            "computer_vision"
        );
        repo5.setStarCount(82);

        Repository repo6 = new Repository(
            "NLP Text Processing",
            "Natural language processing utilities for text cleaning, tokenization, and analysis.",
            Arrays.asList("nlp", "text-processing", "utilities", "tokenization"),
            Arrays.asList("nlp@text.utils", "dev@language.processing"),
            "nlp"
        );
        repo6.setStarCount(37);

        Repository repo7 = new Repository(
            "Healthcare Data Pipeline",
            "ETL pipeline for processing and standardizing healthcare data from multiple sources.",
            Arrays.asList("healthcare", "etl", "data-pipeline", "standardization"),
            Arrays.asList("health@data.pipeline", "medical@processing.org"),
            "healthcare"
        );
        repo7.setStarCount(54);

        Repository repo8 = new Repository(
            "Cybersecurity Tools",
            "Collection of penetration testing and security analysis tools for ethical hacking.",
            Arrays.asList("cybersecurity", "penetration-testing", "security", "tools"),
            Arrays.asList("security@tools.org", "pentest@security.lab"),
            "cyber_security"
        );
        repo8.setStarCount(29);

        Repository repo9 = new Repository(
            "Climate Modeling Scripts",
            "Python scripts for climate data modeling and environmental impact analysis.",
            Arrays.asList("climate", "modeling", "environmental", "python"),
            Arrays.asList("climate@modeling.org", "env@research.lab"),
            "environmental"
        );
        repo9.setStarCount(41);

        repositoryRepository.save(repo1);
        repositoryRepository.save(repo2);
        repositoryRepository.save(repo3);
        repositoryRepository.save(repo4);
        repositoryRepository.save(repo5);
        repositoryRepository.save(repo6);
        repositoryRepository.save(repo7);
        repositoryRepository.save(repo8);
        repositoryRepository.save(repo9);
    }

    private void initializeStorageResources() {
        // Create sample storage resources based on the screenshot
        StorageResource storage1 = new StorageResource("DeltaAI", "500 GB", "SCP", "Active", "High-performance storage cluster for AI workloads");
        StorageResource storage2 = new StorageResource("DeltaAI", "500 GB", "S3", "Active", "Object storage for large datasets");
        StorageResource storage3 = new StorageResource("DeltaAI", "500 GB", "S3", "Full", "Archive storage for completed experiments");
        StorageResource storage4 = new StorageResource("DeltaAI", "500 GB", "SCP", "Archived", "Legacy storage system");
        StorageResource storage5 = new StorageResource("DeltaAI", "500 GB", "SCP", "Full", "Production storage cluster");

        storageResourceRepository.save(storage1);
        storageResourceRepository.save(storage2);
        storageResourceRepository.save(storage3);
        storageResourceRepository.save(storage4);
        storageResourceRepository.save(storage5);
    }

    private void initializeComputeResources() {
        // Create sample compute resources with Step 2 data
        ComputeResource compute1 = new ComputeResource("DeltaAI GPU", "gpu-cluster.deltaai.org", "GPU", "Active", 
            "High-performance GPU cluster for machine learning", "SLURM", "SCP", 
            Arrays.asList("GPU queue", "Compute queue", "Debug queue"));
        
        ComputeResource compute2 = new ComputeResource("DeltaAI CPU", "cpu-cluster.deltaai.org", "CPU", "Active", 
            "General purpose compute cluster", "PBS", "SFTP", 
            Arrays.asList("Compute queue", "Debug queue"));
        
        ComputeResource compute3 = new ComputeResource("DeltaAI HPC", "hpc-cluster.deltaai.org", "HPC", "Full", 
            "High-performance computing cluster", "SLURM", "SCP", 
            Arrays.asList("GPU queue", "Compute queue", "GPU shared queue"));
        
        ComputeResource compute4 = new ComputeResource("DeltaAI Edge", "edge-nodes.deltaai.org", "Edge", "Active", 
            "Edge computing nodes for distributed processing", "SGE", "RSYNC", 
            Arrays.asList("Debug queue", "GPU shared queue"));
        
        ComputeResource compute5 = new ComputeResource("DeltaAI Cloud", "cloud.deltaai.org", "Cloud", "Archived", 
            "Cloud-based compute resources", "SLURM", "SCP", 
            Arrays.asList("GPU queue", "Compute queue", "Debug queue", "GPU shared queue"));

        computeResourceRepository.save(compute1);
        computeResourceRepository.save(compute2);
        computeResourceRepository.save(compute3);
        computeResourceRepository.save(compute4);
        computeResourceRepository.save(compute5);
    }
}
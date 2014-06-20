package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "DEPLOYMENT")
public class Deployment {
    @Id
    @Column(name = "DEPLOYMENT_ID")
    private String applicationID;

    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceID;

    @Column(name = "DEPLOYMENT_HOST_NAME")
    private String deploymentHostName;

    @Column(name = "INPUT_DIR")
    private String inputDir;

    @Column(name = "OUTPUT_DIR")
    private String outputDir;

    @Column(name = "SCRATCH_DIR")
    private String scratchDir;

    @Column(name = "EXECUTION_PATH")
    private String executionPath;

    @Column(name = "QUEUE_NAME")
    private String queueName;

    @Column(name = "CPU_COUNT")
    private int cpuCount;

    @Column(name = "NODE_COUNT")
    private int nodeCount;

    @Column(name = "WALLTIME_LIMIT")
    private int walltime;

    @Column(name = "NO_OF_THREADS")
    private int numberOfThreads;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "COMPUTE_RESOURCE_ID")
    private ComputeResource computeResource;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public void setWalltime(int walltime) {
        this.walltime = walltime;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public ComputeResource getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResource computeResource) {
        this.computeResource = computeResource;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getComputeResourceID() {
        return computeResourceID;
    }

    public void setComputeResourceID(String computeResourceID) {
        this.computeResourceID = computeResourceID;
    }

    public String getDeploymentHostName() {
        return deploymentHostName;
    }

    public void setDeploymentHostName(String deploymentHostName) {
        this.deploymentHostName = deploymentHostName;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getScratchDir() {
        return scratchDir;
    }

    public void setScratchDir(String scratchDir) {
        this.scratchDir = scratchDir;
    }

    public String getExecutionPath() {
        return executionPath;
    }

    public void setExecutionPath(String executionPath) {
        this.executionPath = executionPath;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getWalltime() {
        return walltime;
    }
}

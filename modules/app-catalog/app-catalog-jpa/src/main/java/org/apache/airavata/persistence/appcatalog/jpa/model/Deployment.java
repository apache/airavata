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

    @Column(name = "CPU_COUNT")
    private String cpuCount;

    @Column(name = "NODE_COUNT")
    private String nodeCount;

    @Column(name = "WALLTIME")
    private String walltime;


    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "COMPUTE_RESOURCE_ID")
    private ComputeResource computeResource;


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

    public String getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(String cpuCount) {
        this.cpuCount = cpuCount;
    }

    public String getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(String nodeCount) {
        this.nodeCount = nodeCount;
    }

    public String getWalltime() {
        return walltime;
    }

    public void setWalltime(String walltime) {
        this.walltime = walltime;
    }
}

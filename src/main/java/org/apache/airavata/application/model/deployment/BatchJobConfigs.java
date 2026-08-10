package org.apache.airavata.application.model.deployment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Batch-scheduler resource request for a {@link BatchApplicationDeploymentEntity}. Owned
 * by its deployment — created, replaced and deleted with it — rather than a shareable
 * aggregate like {@code ApplicationTemplateEntity} or {@code SlurmClusterEntity}.
 */
@Entity
public class BatchJobConfigs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String batchJobConfigId;

    // Slurm resource allocation parameters
    @Column(nullable = true)
    private Integer cpus;
    @Column(nullable = true)
    private String mem;
    @Column(nullable = true)
    private String memPerCpu;
    @Column(nullable = true)
    private Integer ntasksPerNode;
    @Column(nullable = true)
    private Integer cpusPerTask;
    @Column(nullable = true)
    private Integer nodes;
    @Column(nullable = true)
    private Integer ntasks;

    // GPU related fields
    @Column(nullable = true)
    private String gres;
    @Column(nullable = true)
    private Integer gpus;
    @Column(nullable = true)
    private String memPerGpu;
    @Column(nullable = true)
    private String cpusPerGpu;
    @Column(nullable = true)
    private Integer gpusPerNode;

    @Column(nullable = false)
    private Long wallTimeMinutes;

    @Column(nullable = true)
    private String constraints; // e.g. "gpu", "cpu", "highmem", etc.

    @Column(nullable = false)
    private String allocation;

    public String getBatchJobConfigId() {
        return batchJobConfigId;
    }

    public void setBatchJobConfigId(String batchJobConfigId) {
        this.batchJobConfigId = batchJobConfigId;
    }

    public Integer getCpus() {
        return cpus;
    }

    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public String getMemPerCpu() {
        return memPerCpu;
    }

    public void setMemPerCpu(String memPerCpu) {
        this.memPerCpu = memPerCpu;
    }

    public Integer getNtasksPerNode() {
        return ntasksPerNode;
    }

    public void setNtasksPerNode(Integer ntasksPerNode) {
        this.ntasksPerNode = ntasksPerNode;
    }

    public Integer getCpusPerTask() {
        return cpusPerTask;
    }

    public void setCpusPerTask(Integer cpusPerTask) {
        this.cpusPerTask = cpusPerTask;
    }

    public Integer getNodes() {
        return nodes;
    }

    public void setNodes(Integer nodes) {
        this.nodes = nodes;
    }

    public Integer getNtasks() {
        return ntasks;
    }

    public void setNtasks(Integer ntasks) {
        this.ntasks = ntasks;
    }

    public String getGres() {
        return gres;
    }

    public void setGres(String gres) {
        this.gres = gres;
    }

    public Integer getGpus() {
        return gpus;
    }

    public void setGpus(Integer gpus) {
        this.gpus = gpus;
    }

    public String getMemPerGpu() {
        return memPerGpu;
    }

    public void setMemPerGpu(String memPerGpu) {
        this.memPerGpu = memPerGpu;
    }

    public String getCpusPerGpu() {
        return cpusPerGpu;
    }

    public void setCpusPerGpu(String cpusPerGpu) {
        this.cpusPerGpu = cpusPerGpu;
    }

    public Integer getGpusPerNode() {
        return gpusPerNode;
    }

    public void setGpusPerNode(Integer gpusPerNode) {
        this.gpusPerNode = gpusPerNode;
    }

    public Long getWallTimeMinutes() {
        return wallTimeMinutes;
    }

    public void setWallTimeMinutes(Long wallTimeMinutes) {
        this.wallTimeMinutes = wallTimeMinutes;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getAllocation() {
        return allocation;
    }

    public void setAllocation(String allocation) {
        this.allocation = allocation;
    }
}

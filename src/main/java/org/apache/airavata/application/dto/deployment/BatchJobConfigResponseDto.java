package org.apache.airavata.application.dto.deployment;

/** Read view of a deployment's batch-scheduler resource request. */
public class BatchJobConfigResponseDto {

    private String batchJobConfigId;
    private Long wallTimeMinutes;
    private String allocation;

    // Slurm resource allocation parameters
    private Integer cpus;
    private String mem;
    private String memPerCpu;
    private Integer ntasksPerNode;
    private Integer cpusPerTask;
    private Integer nodes;
    private Integer ntasks;

    // GPU related fields
    private String gres;
    private Integer gpus;
    private String memPerGpu;
    private String cpusPerGpu;
    private Integer gpusPerNode;

    private String constraints;

    public String getBatchJobConfigId() {
        return batchJobConfigId;
    }

    public void setBatchJobConfigId(String batchJobConfigId) {
        this.batchJobConfigId = batchJobConfigId;
    }

    public Long getWallTimeMinutes() {
        return wallTimeMinutes;
    }

    public void setWallTimeMinutes(Long wallTimeMinutes) {
        this.wallTimeMinutes = wallTimeMinutes;
    }

    public String getAllocation() {
        return allocation;
    }

    public void setAllocation(String allocation) {
        this.allocation = allocation;
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

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }
}

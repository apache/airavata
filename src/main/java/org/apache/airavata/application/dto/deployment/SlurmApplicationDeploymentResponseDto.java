package org.apache.airavata.application.dto.deployment;

/** Read view of a Slurm deployment; the owning template is referenced by id only. */
public class SlurmApplicationDeploymentResponseDto {

    private String deploymentId;
    private String templateId;
    private String slurmClusterId;
    private String slurmRunSection;
    private Long wallTimeMinutes;
    private String allocation;
    private String user;

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
    private String workDir;
    private String partition;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getSlurmClusterId() {
        return slurmClusterId;
    }

    public void setSlurmClusterId(String slurmClusterId) {
        this.slurmClusterId = slurmClusterId;
    }

    public String getSlurmRunSection() {
        return slurmRunSection;
    }

    public void setSlurmRunSection(String slurmRunSection) {
        this.slurmRunSection = slurmRunSection;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }
}

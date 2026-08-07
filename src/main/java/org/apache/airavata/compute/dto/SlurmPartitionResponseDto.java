package org.apache.airavata.compute.dto;

/** Read view of a partition; the owning cluster is referenced by id only. */
public class SlurmPartitionResponseDto {

    private String partitionId;
    private String clusterId;
    private String name;
    private String description;
    private Integer maxRunTime;
    private Integer maxNodes;
    private Integer maxProcessors;
    private Integer maxJobsInQueue;
    private Long maxMemory;
    private Integer cpuPerNode;
    private Integer defaultNodeCount;
    private Integer defaultCpuCount;
    private Long defaultWalltime;
    private String gres;
    private String nodes;
    private Boolean isDefaultQueue;
    private Boolean isCheckpointable;

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxRunTime() {
        return maxRunTime;
    }

    public void setMaxRunTime(Integer maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    public Integer getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(Integer maxNodes) {
        this.maxNodes = maxNodes;
    }

    public Integer getMaxProcessors() {
        return maxProcessors;
    }

    public void setMaxProcessors(Integer maxProcessors) {
        this.maxProcessors = maxProcessors;
    }

    public Integer getMaxJobsInQueue() {
        return maxJobsInQueue;
    }

    public void setMaxJobsInQueue(Integer maxJobsInQueue) {
        this.maxJobsInQueue = maxJobsInQueue;
    }

    public Long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Integer getCpuPerNode() {
        return cpuPerNode;
    }

    public void setCpuPerNode(Integer cpuPerNode) {
        this.cpuPerNode = cpuPerNode;
    }

    public Integer getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(Integer defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public Integer getDefaultCpuCount() {
        return defaultCpuCount;
    }

    public void setDefaultCpuCount(Integer defaultCpuCount) {
        this.defaultCpuCount = defaultCpuCount;
    }

    public Long getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(Long defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public String getGres() {
        return gres;
    }

    public void setGres(String gres) {
        this.gres = gres;
    }

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public Boolean getIsDefaultQueue() {
        return isDefaultQueue;
    }

    public void setIsDefaultQueue(Boolean isDefaultQueue) {
        this.isDefaultQueue = isDefaultQueue;
    }

    public Boolean getIsCheckpointable() {
        return isCheckpointable;
    }

    public void setIsCheckpointable(Boolean isCheckpointable) {
        this.isCheckpointable = isCheckpointable;
    }
}

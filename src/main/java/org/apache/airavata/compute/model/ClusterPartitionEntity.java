package org.apache.airavata.compute.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ClusterPartitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String partitionId;

    @ManyToOne
    @JoinColumn(name = "cluster_id", foreignKey = @ForeignKey(name = "fk_partition_cluster"))
    private ClusterEntity slurmCluster;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private Integer maxRunTime;

    @Column(nullable = true)
    private Integer maxNodes;

    @Column(nullable = true)
    private Integer maxProcessors;

    @Column(nullable = true)
    private Integer maxJobsInQueue;

    @Column(nullable = true)
    private Long maxMemory;

    @Column(nullable = true)
    private Integer cpuPerNode;

    @Column(nullable = true)
    private Integer defaultNodeCount;

    @Column(nullable = true)
    private Integer defaultCpuCount;

    @Column(nullable = true)
    private Long defaultWalltime;

    @Column(nullable = true)
    private String gres; // Comma-separated list of generic resources (GRES) associated with the
                         // partition

    @Column(nullable = true)
    private String nodes; // Comma-separated list of nodes associated with the partition

    @Column(nullable = true)
    private Boolean isDefaultQueue;

    @Column(nullable = true)
    private Boolean isCheckpointable;

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public ClusterEntity getSlurmCluster() {
        return slurmCluster;
    }

    public void setSlurmCluster(ClusterEntity slurmCluster) {
        this.slurmCluster = slurmCluster;
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

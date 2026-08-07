package org.apache.airavata.compute.dto;

import java.util.List;

/** Read view of a Slurm cluster, including its partitions. */
public class SlurmClusterResponseDto {

    private String clusterId;
    private String clusterName;
    private String clusterDescription;
    private String hostName;
    private String slurmHome;
    private List<SlurmPartitionResponseDto> partitions;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterDescription() {
        return clusterDescription;
    }

    public void setClusterDescription(String clusterDescription) {
        this.clusterDescription = clusterDescription;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getSlurmHome() {
        return slurmHome;
    }

    public void setSlurmHome(String slurmHome) {
        this.slurmHome = slurmHome;
    }

    public List<SlurmPartitionResponseDto> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<SlurmPartitionResponseDto> partitions) {
        this.partitions = partitions;
    }
}

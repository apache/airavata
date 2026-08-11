package org.apache.airavata.compute.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for a Slurm cluster. Partitions are not accepted here — they are
 * managed through {@code /api/v1/clusters/{clusterId}/partitions} so that updating
 * a cluster cannot silently drop them.
 */
public class ClusterRequestDto {

    @NotBlank(message = "Cluster name cannot be blank")
    private String clusterName;

    private String clusterDescription;

    @NotBlank(message = "Host name cannot be blank")
    private String hostName;

    @NotBlank(message = "Slurm home cannot be blank")
    private String slurmHome;

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
}

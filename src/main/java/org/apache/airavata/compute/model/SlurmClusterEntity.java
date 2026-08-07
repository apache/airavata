package org.apache.airavata.compute.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SlurmClusterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String clusterId;

    @Column(nullable = false)
    private String clusterName;
    @Column(nullable = true)
    private String clusterDescription;
    @Column(nullable = false)
    private String hostName;
    @Column(nullable = false)
    private String slurmHome;

    // Partitions are owned by the cluster: they are created, replaced and deleted
    // with it, and have no meaning outside of it.
    @OneToMany(mappedBy = "slurmCluster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlurmPartitionEntity> partitions = new ArrayList<>();

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

    public List<SlurmPartitionEntity> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<SlurmPartitionEntity> partitions) {
        this.partitions = partitions;
    }
}

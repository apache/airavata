package org.apache.airavata.research.service.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "COMPUTE_RESOURCE")
public class ComputeResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceId;

    @Column(name = "HOST_NAME", nullable = false)
    private String hostName;

    @Column(name = "RESOURCE_DESCRIPTION", length = 2048)
    private String resourceDescription;

    @Column(name = "CREATION_TIME", nullable = false)
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME", nullable = false)
    private Timestamp updateTime;

    @Column(name = "MAX_MEMORY_NODE")
    private Integer maxMemoryNode;

    @Column(name = "CPUS_PER_NODE")
    private Integer cpusPerNode;

    @Column(name = "DEFAULT_NODE_COUNT")
    private Integer defaultNodeCount;

    @Column(name = "DEFAULT_CPU_COUNT")
    private Integer defaultCpuCount;

    @Column(name = "DEFAULT_WALLTIME")
    private Integer defaultWalltime;

    @Column(name = "ENABLED")
    private Short enabled;

    @Column(name = "GATEWAY_USAGE_REPORTING")
    private Boolean gatewayUsageReporting;

    @Column(name = "GATEWAY_USAGE_MODULE_LOAD_CMD", length = 500)
    private String gatewayUsageModuleLoadCmd;

    @Column(name = "GATEWAY_USAGE_EXECUTABLE")
    private String gatewayUsageExecutable;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getMaxMemoryNode() {
        return maxMemoryNode;
    }

    public void setMaxMemoryNode(Integer maxMemoryNode) {
        this.maxMemoryNode = maxMemoryNode;
    }

    public Integer getCpusPerNode() {
        return cpusPerNode;
    }

    public void setCpusPerNode(Integer cpusPerNode) {
        this.cpusPerNode = cpusPerNode;
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

    public Integer getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(Integer defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public Short getEnabled() {
        return enabled;
    }

    public void setEnabled(Short enabled) {
        this.enabled = enabled;
    }

    public Boolean getGatewayUsageReporting() {
        return gatewayUsageReporting;
    }

    public void setGatewayUsageReporting(Boolean gatewayUsageReporting) {
        this.gatewayUsageReporting = gatewayUsageReporting;
    }

    public String getGatewayUsageModuleLoadCmd() {
        return gatewayUsageModuleLoadCmd;
    }

    public void setGatewayUsageModuleLoadCmd(String gatewayUsageModuleLoadCmd) {
        this.gatewayUsageModuleLoadCmd = gatewayUsageModuleLoadCmd;
    }

    public String getGatewayUsageExecutable() {
        return gatewayUsageExecutable;
    }

    public void setGatewayUsageExecutable(String gatewayUsageExecutable) {
        this.gatewayUsageExecutable = gatewayUsageExecutable;
    }
} 
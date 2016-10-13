package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the compute_resource database table.
 */
@Entity
@Table(name = "compute_resource")
public class ComputeResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    private short enabled;

    @Column(name = "GATEWAY_USAGE_EXECUTABLE")
    private String gatewayUsageExecutable;

    @Column(name = "GATEWAY_USAGE_MODULE_LOAD_CMD")
    private String gatewayUsageModuleLoadCmd;

    @Column(name = "GATEWAY_USAGE_REPORTING")
    private short gatewayUsageReporting;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name = "MAX_MEMORY_NODE")
    private int maxMemoryNode;

    @Column(name = "RESOURCE_DESCRIPTION")
    private String resourceDescription;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public ComputeResourceEntity() {
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public short getEnabled() {
        return enabled;
    }

    public void setEnabled(short enabled) {
        this.enabled = enabled;
    }

    public String getGatewayUsageExecutable() {
        return gatewayUsageExecutable;
    }

    public void setGatewayUsageExecutable(String gatewayUsageExecutable) {
        this.gatewayUsageExecutable = gatewayUsageExecutable;
    }

    public String getGatewayUsageModuleLoadCmd() {
        return gatewayUsageModuleLoadCmd;
    }

    public void setGatewayUsageModuleLoadCmd(String gatewayUsageModuleLoadCmd) {
        this.gatewayUsageModuleLoadCmd = gatewayUsageModuleLoadCmd;
    }

    public short getGatewayUsageReporting() {
        return gatewayUsageReporting;
    }

    public void setGatewayUsageReporting(short gatewayUsageReporting) {
        this.gatewayUsageReporting = gatewayUsageReporting;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getMaxMemoryNode() {
        return maxMemoryNode;
    }

    public void setMaxMemoryNode(int maxMemoryNode) {
        this.maxMemoryNode = maxMemoryNode;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
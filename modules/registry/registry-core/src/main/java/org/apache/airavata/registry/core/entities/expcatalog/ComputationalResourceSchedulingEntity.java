package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Persistent class for computational_resource_scheduling data table.
 */
@Entity
@Table(name = "COMPUTE_RESOURCE_SCHEDULING")
@IdClass(ComputationalResourceSchedulingPK.class)
public class ComputationalResourceSchedulingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Id
    @Column(name = "RESOURCE_HOST_ID")
    private String resourceHostId;

    @Id
    @Column(name = "QUEUE_NAME")
    private String queueName;

    @Column(name = "TOTAL_CPU_COUNT")
    private int totalCPUCount;

    @Column(name = "NODE_COUNT")
    private int nodeCount;

    @Column(name = "NUMBER_OF_THREADS")
    private int numberOfThreads;

    @Column(name = "WALL_TIME_LIMIT")
    private int wallTimeLimit;

    @Column(name = "PARALLEL_GROUP_COUNT")
    private int mGroupCount;

    @Column(name = "TOTAL_PHYSICAL_MEMORY")
    private int totalPhysicalMemory;

    @Column(name = "STATIC_WORKING_DIR")
    private String staticWorkingDir;

    @Column(name = "OVERRIDE_LOGIN_USER_NAME")
    private String overrideLoginUserName;

    @Column(name = "OVERRIDE_SCRATCH_LOCATION")
    private String overrideScratchLocation;

    @Column(name = "OVERRIDE_ALLOCATION_PROJECT_NUMBER")
    private String overrideAllocationProjectNumber;


    @ManyToOne(targetEntity = UserConfigurationDataEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    private UserConfigurationDataEntity userConfigurationData;


    public ComputationalResourceSchedulingEntity() {
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getTotalCPUCount() {
        return totalCPUCount;
    }

    public void setTotalCPUCount(int totalCPUCount) {
        this.totalCPUCount = totalCPUCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public int getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(int totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public String getStaticWorkingDir() {
        return staticWorkingDir;
    }

    public void setStaticWorkingDir(String staticWorkingDir) {
        this.staticWorkingDir = staticWorkingDir;
    }

    public String getOverrideLoginUserName() {
        return overrideLoginUserName;
    }

    public void setOverrideLoginUserName(String overrideLoginUserName) {
        this.overrideLoginUserName = overrideLoginUserName;
    }

    public String getOverrideScratchLocation() {
        return overrideScratchLocation;
    }

    public void setOverrideScratchLocation(String overrideScratchLocation) {
        this.overrideScratchLocation = overrideScratchLocation;
    }

    public String getOverrideAllocationProjectNumber() {
        return overrideAllocationProjectNumber;
    }

    public void setOverrideAllocationProjectNumber(String overrideAllocationProjectNumber) {
        this.overrideAllocationProjectNumber = overrideAllocationProjectNumber;
    }

    public int getmGroupCount() {
        return mGroupCount;
    }

    public void setmGroupCount(int mGroupCount) {
        this.mGroupCount = mGroupCount;
    }
}

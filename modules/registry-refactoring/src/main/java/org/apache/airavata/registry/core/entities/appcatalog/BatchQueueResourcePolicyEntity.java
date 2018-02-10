package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by skariyat on 2/8/18.
 */
@Entity
@Table(name = "BATCH_QUEUE_RESOURCE_POLICY")
@IdClass(BatchQueueResourcePolicyPK.class)
public class BatchQueueResourcePolicyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_POLICY_ID")
    private String resourcePolicyId;

    @Id
    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Id
    @Column(name = "QUEUE_NAME")
    private String queuename;

    @Column(name = "MAX_ALLOWED_NODES")
    private String maxAllowedNodes;

    @Column(name = "MAX_ALLOWED_CORES")
    private String maxAllowedCores;

    @Column(name = "MAX_ALLOWED_WALLTIME")
    private String maxAllowedWalltime;

    @ManyToOne(targetEntity = GroupResourceProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID")
    private GroupResourceProfileEntity groupResourceProfile;

    public BatchQueueResourcePolicyEntity() {
    }

    public String getResourcePolicyId() {
        return resourcePolicyId;
    }

    public void setResourcePolicyId(String resourcePolicyId) {
        this.resourcePolicyId = resourcePolicyId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getQueuename() {
        return queuename;
    }

    public void setQueuename(String queuename) {
        this.queuename = queuename;
    }

    public String getMaxAllowedNodes() {
        return maxAllowedNodes;
    }

    public void setMaxAllowedNodes(String maxAllowedNodes) {
        this.maxAllowedNodes = maxAllowedNodes;
    }

    public String getMaxAllowedCores() {
        return maxAllowedCores;
    }

    public void setMaxAllowedCores(String maxAllowedCores) {
        this.maxAllowedCores = maxAllowedCores;
    }

    public String getMaxAllowedWalltime() {
        return maxAllowedWalltime;
    }

    public void setMaxAllowedWalltime(String maxAllowedWalltime) {
        this.maxAllowedWalltime = maxAllowedWalltime;
    }

    public GroupResourceProfileEntity getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfileEntity groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }
}

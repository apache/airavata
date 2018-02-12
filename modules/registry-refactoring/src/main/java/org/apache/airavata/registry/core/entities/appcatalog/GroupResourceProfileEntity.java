package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by skariyat on 2/7/18.
 */
@Entity
@Table(name = "GROUP_RESOURCE_PROFILE")
@IdClass(GroupResourceProfilePK.class)
public class GroupResourceProfileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "GROUP_RESOURCE_PROFILE_NAME")
    private String groupResourceProfileName;

    @Column(name = "CREATION_TIME")
    private Long creationTime;

    @Column(name = "UPDATE_TIME")
    private Long updatedTime;

    @OneToMany(targetEntity = GroupComputeResourcePrefEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER)
    private List<GroupComputeResourcePrefEntity> computePreferences;

    @OneToMany(targetEntity = ComputeResourcePolicyEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER)
    private List<ComputeResourcePolicyEntity> computeResourcePolicies;

    @OneToMany(targetEntity = BatchQueueResourcePolicyEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER)
    private List<BatchQueueResourcePolicyEntity> batchQueueResourcePolicies;

    public GroupResourceProfileEntity() {
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getGroupResourceProfileName() {
        return groupResourceProfileName;
    }

    public void setGroupResourceProfileName(String groupResourceProfileName) {
        this.groupResourceProfileName = groupResourceProfileName;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public List<GroupComputeResourcePrefEntity> getComputePreferences() {
        return computePreferences;
    }

    public void setComputePreferences(List<GroupComputeResourcePrefEntity> computePreferences) {
        this.computePreferences = computePreferences;
    }

    public List<ComputeResourcePolicyEntity> getComputeResourcePolicies() {
        return computeResourcePolicies;
    }

    public void setComputeResourcePolicies(List<ComputeResourcePolicyEntity> computeResourcePolicies) {
        this.computeResourcePolicies = computeResourcePolicies;
    }

    public List<BatchQueueResourcePolicyEntity> getBatchQueueResourcePolicies() {
        return batchQueueResourcePolicies;
    }

    public void setBatchQueueResourcePolicies(List<BatchQueueResourcePolicyEntity> batchQueueResourcePolicies) {
        this.batchQueueResourcePolicies = batchQueueResourcePolicies;
    }
}

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
    private String gatewayID;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "GROUP_RESOURCE_PROFILE_NAME")
    private String groupResourceProfileName;

    @Column(name = "CREATION_TIME")
    private String creationTime;

    @Column(name = "UPDATE_TIME")
    private String updatedTime;

    @OneToMany(targetEntity = GroupComputeResourcePrefEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER)
    private List<GroupComputeResourcePrefEntity> ComputeResourcePrefences;

    @OneToMany(targetEntity = ComputeResourcePolicyEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER)
    private List<ComputeResourcePolicyEntity> computeResourcePolicies;

    @OneToMany(targetEntity = BatchQueueResourcePolicyEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER)
    private List<BatchQueueResourcePolicyEntity> batchQueueResourcePolicies;

    public GroupResourceProfileEntity() {
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
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

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public List<GroupComputeResourcePrefEntity> getComputeResourcePrefences() {
        return ComputeResourcePrefences;
    }

    public void setComputeResourcePrefences(List<GroupComputeResourcePrefEntity> computeResourcePrefences) {
        ComputeResourcePrefences = computeResourcePrefences;
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

package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by skariyat on 2/8/18.
 */
@Entity
@Table(name = "COMPUTE_RESOURCE_POLICY")
@IdClass(ComputeResourcePolicyPK.class)
public class ComputeResourcePolicyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_POLICY_ID")
    private String resourcePolicyId;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Id
    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="COMPUTE_RESOURCE_POLICY_QUEUES", joinColumns = {
            @JoinColumn(name = "RESOURCE_POLICY_ID"),
            @JoinColumn(name = "COMPUTE_RESOURCE_ID"),
            @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID"),
            @JoinColumn(name = "GATEWAY_ID")})
    @Column(name = "ALLOWED_BATCH_QUEUES")
    private List<String> allowedBatchQueues;

    @ManyToOne(targetEntity = GroupResourceProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID")
    private GroupResourceProfileEntity groupResourceProfile;

    public ComputeResourcePolicyEntity() {
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

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public List<String> getAllowedBatchQueues() {
        return allowedBatchQueues;
    }

    public void setAllowedBatchQueues(List<String> allowedBatchQueues) {
        this.allowedBatchQueues = allowedBatchQueues;
    }

    public GroupResourceProfileEntity getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfileEntity groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }
}

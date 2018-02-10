package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by skariyat on 2/8/18.
 */
public class ComputeResourcePolicyPK implements Serializable {

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

    public ComputeResourcePolicyPK() {
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
}

package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by skariyat on 2/8/18.
 */
public class BatchQueueResourcePolicyPK implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_POLICY_ID")
    private String resourcePolicyId;

    @Id
    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Id
    @Column(name = "QUEUE_NAME")
    private String queuename;

    public BatchQueueResourcePolicyPK() {
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchQueueResourcePolicyPK that = (BatchQueueResourcePolicyPK) o;

        if (resourcePolicyId != null ? !resourcePolicyId.equals(that.resourcePolicyId) : that.resourcePolicyId != null)
            return false;
        if (computeResourceId != null ? !computeResourceId.equals(that.computeResourceId) : that.computeResourceId != null)
            return false;
        if (groupResourceProfileId != null ? !groupResourceProfileId.equals(that.groupResourceProfileId) : that.groupResourceProfileId != null)
            return false;
        return queuename != null ? queuename.equals(that.queuename) : that.queuename == null;
    }

    @Override
    public int hashCode() {
        int result = resourcePolicyId != null ? resourcePolicyId.hashCode() : 0;
        result = 31 * result + (computeResourceId != null ? computeResourceId.hashCode() : 0);
        result = 31 * result + (groupResourceProfileId != null ? groupResourceProfileId.hashCode() : 0);
        result = 31 * result + (queuename != null ? queuename.hashCode() : 0);
        return result;
    }
}

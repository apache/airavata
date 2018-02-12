package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by skariyat on 2/9/18.
 */
public class GroupComputeResourcePrefPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "RESOURCE_ID")
    @Id
    private String computeResourceId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    @Id
    private String groupResourceProfileId;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupComputeResourcePrefPK that = (GroupComputeResourcePrefPK) o;

        if (computeResourceId != null ? !computeResourceId.equals(that.computeResourceId) : that.computeResourceId != null)
            return false;
        return groupResourceProfileId != null ? groupResourceProfileId.equals(that.groupResourceProfileId) : that.groupResourceProfileId == null;
    }

    @Override
    public int hashCode() {
        int result = computeResourceId != null ? computeResourceId.hashCode() : 0;
        result = 31 * result + (groupResourceProfileId != null ? groupResourceProfileId.hashCode() : 0);
        return result;
    }
}

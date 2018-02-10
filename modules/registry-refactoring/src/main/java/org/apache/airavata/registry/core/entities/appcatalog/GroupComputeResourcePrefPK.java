package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by skariyat on 2/9/18.
 */
public class GroupComputeResourcePrefPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "GATEWAY_ID")
    @Id
    private String gatewayID;

    @Column(name = "RESOURCE_ID")
    @Id
    private String computeResourceId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    @Id
    private String groupResourceProfileId;

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
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
}

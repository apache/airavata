package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by skariyat on 2/7/18.
 */
public class GroupResourceProfilePK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayID;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    public GroupResourceProfilePK() {

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
}

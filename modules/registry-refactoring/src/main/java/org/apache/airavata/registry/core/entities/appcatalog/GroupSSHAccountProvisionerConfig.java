package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by skariyat on 2/9/18.
 */
@Entity
@Table(name = "GROUP_SSH_ACCOUNT_PROVISIONER_CONFIG")
@IdClass(GroupSSHAccountProvisionerConfigPK.class)
public class GroupSSHAccountProvisionerConfig implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Id
    @Column(name = "RESOURCE_ID")
    private String resourceId;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Id
    @Column(name = "CONFIG_NAME")
    private String configName;

    @Column(name = "CONFIG_VALUE")
    private String configValue;

    @ManyToOne(targetEntity = GroupComputeResourcePrefEntity.class, cascade= CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID", nullable = false),
            @JoinColumn(name = "RESOURCE_ID", referencedColumnName = "RESOURCE_ID", nullable = false),
            @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", referencedColumnName = "GROUP_RESOURCE_PROFILE_ID", nullable = false)
    })
    private GroupComputeResourcePrefEntity groupComputeResourcePref;

    public GroupSSHAccountProvisionerConfig() {
    }

    public GroupSSHAccountProvisionerConfig( String configName, String configValue, GroupComputeResourcePrefEntity groupComputeResourcePref) {
        this.gatewayId = groupComputeResourcePref.getGatewayID();
        this.resourceId = groupComputeResourcePref.getComputeResourceId();
        this.groupResourceProfileId = groupComputeResourcePref.getGroupResourceProfileId();
        this.configName = configName;
        this.configValue = configValue;
        this.groupComputeResourcePref= groupComputeResourcePref;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public GroupComputeResourcePrefEntity getGroupComputeResourcePref() {
        return groupComputeResourcePref;
    }

    public void setGroupComputeResourcePref(GroupComputeResourcePrefEntity groupComputeResourcePref) {
        this.groupComputeResourcePref = groupComputeResourcePref;
    }
}

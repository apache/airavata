package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by skariyat on 2/9/18.
 */
@Entity
@Table(name = "GRP_SSH_ACC_PROV_CONFIG")
@IdClass(GroupSSHAccountProvisionerConfigPK.class)
public class GroupSSHAccountProvisionerConfig implements Serializable{

    private static final long serialVersionUID = 1L;

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
            @JoinColumn(name = "RESOURCE_ID", referencedColumnName = "RESOURCE_ID", nullable = false),
            @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", referencedColumnName = "GROUP_RESOURCE_PROFILE_ID", nullable = false)
    })
    private GroupComputeResourcePrefEntity groupComputeResourcePref;

    public GroupSSHAccountProvisionerConfig() {
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

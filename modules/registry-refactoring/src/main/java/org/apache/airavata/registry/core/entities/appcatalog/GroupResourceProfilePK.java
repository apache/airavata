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
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    public GroupResourceProfilePK() {

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

        GroupResourceProfilePK that = (GroupResourceProfilePK) o;

        return groupResourceProfileId != null ? groupResourceProfileId.equals(that.groupResourceProfileId) : that.groupResourceProfileId == null;
    }

    @Override
    public int hashCode() {
        return groupResourceProfileId != null ? groupResourceProfileId.hashCode() : 0;
    }
}

package org.apache.airavata.sharing.registry.db.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class OwnerPK implements Serializable{

    private final static Logger logger = LoggerFactory.getLogger(OwnerPK.class);
    private String groupId;
    private String domainId;

    @Id
    @Column(name = "GROUP_ID")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Id
    @Column(name = "DOMAIN_ID")
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OwnerPK ownerPK = (OwnerPK) o;

        if (!getGroupId().equals(ownerPK.getGroupId())) return false;
        return getDomainId().equals(ownerPK.getDomainId());
    }

    @Override
    public int hashCode() {
        int result = getGroupId().hashCode();
        result = 31 * result + getDomainId().hashCode();
        return result;
    }
}

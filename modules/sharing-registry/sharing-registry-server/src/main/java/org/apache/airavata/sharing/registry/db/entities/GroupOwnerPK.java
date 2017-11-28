package org.apache.airavata.sharing.registry.db.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class GroupOwnerPK implements Serializable{

    private final static Logger logger = LoggerFactory.getLogger(GroupOwnerPK.class);
    private String ownerId;
    private String domainId;

    @Id
    @Column(name = "DOMAIN_ID")
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Id
    @Column(name = "OWNER_ID")
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupOwnerPK groupOwnerPK = (GroupOwnerPK) o;

        if (!getOwnerId().equals(groupOwnerPK.getOwnerId())) return false;
        return getDomainId().equals(groupOwnerPK.getDomainId());
    }

    @Override
    public int hashCode() {
        int result = getOwnerId().hashCode();
        result = 31 * result + getDomainId().hashCode();
        return result;
    }
}

package org.apache.airavata.sharing.registry.db.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "GROUP_ADMIN", schema = "")
@IdClass(GroupAdminPK.class)
public class GroupAdminEntity {
    private final static Logger logger = LoggerFactory.getLogger(GroupAdminEntity.class);
    private String groupId;
    private String domainId;
    private String adminId;

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

    @Id
    @Column(name = "ADMIN_ID")
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupAdminEntity that = (GroupAdminEntity) o;

        if (!getGroupId().equals(that.getGroupId())) return false;
        if (!getDomainId().equals(that.getDomainId())) return false;
        return getAdminId().equals(that.getAdminId());
    }

    @Override
    public int hashCode() {
        int result = getGroupId().hashCode();
        result = 31 * result + getDomainId().hashCode();
        result = 31 * result + getAdminId().hashCode();
        return result;
    }
}

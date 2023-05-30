package org.apache.airavata.apis.db.entity.backend.iface;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SCPInterfaceEntity {
    @Id
    @Column(name = "SCP_IFACE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String scpInterfaceId;

    @Column
    String interfaceId;

    @Column
    String hostName;

    @Column
    Integer port;

    @Column
    String sshCredentialId;

    public String getScpInterfaceId() {
        return scpInterfaceId;
    }

    public void setScpInterfaceId(String scpInterfaceId) {
        this.scpInterfaceId = scpInterfaceId;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSshCredentialId() {
        return sshCredentialId;
    }

    public void setSshCredentialId(String sshCredentialId) {
        this.sshCredentialId = sshCredentialId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scpInterfaceId == null) ? 0 : scpInterfaceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SCPInterfaceEntity other = (SCPInterfaceEntity) obj;
        if (scpInterfaceId == null) {
            if (other.scpInterfaceId != null)
                return false;
        } else if (!scpInterfaceId.equals(other.scpInterfaceId))
            return false;
        return true;
    }

}

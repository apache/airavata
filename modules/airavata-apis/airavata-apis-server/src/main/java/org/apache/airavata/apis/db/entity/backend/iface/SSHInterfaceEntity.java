package org.apache.airavata.apis.db.entity.backend.iface;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SSHInterfaceEntity {

    @Id
    @Column(name = "SSH_IFACE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String sshInterfaceId;

    @Column
    String hostName;
    @Column
    Integer port;
    @Column
    String sshCredentialId;

    public String getSshInterfaceId() {
        return sshInterfaceId;
    }

    public void setSshInterfaceId(String sshInterfaceId) {
        this.sshInterfaceId = sshInterfaceId;
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
        result = prime * result + ((sshInterfaceId == null) ? 0 : sshInterfaceId.hashCode());
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
        SSHInterfaceEntity other = (SSHInterfaceEntity) obj;
        if (sshInterfaceId == null) {
            if (other.sshInterfaceId != null)
                return false;
        } else if (!sshInterfaceId.equals(other.sshInterfaceId))
            return false;
        return true;
    }


}

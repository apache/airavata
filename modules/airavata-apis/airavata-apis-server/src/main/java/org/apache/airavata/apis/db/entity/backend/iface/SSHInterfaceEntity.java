package org.apache.airavata.apis.db.entity.backend.iface;

import org.apache.airavata.apis.db.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class SSHInterfaceEntity extends BaseEntity {

    @Column
    String hostName;
    @Column
    Integer port;
    @Column
    String sshCredentialId;

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

}

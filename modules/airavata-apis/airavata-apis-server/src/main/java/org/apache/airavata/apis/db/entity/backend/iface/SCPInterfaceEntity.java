package org.apache.airavata.apis.db.entity.backend.iface;

import org.apache.airavata.apis.db.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class SCPInterfaceEntity extends BaseEntity {

    @Column
    String interfaceId;

    @Column
    String hostName;

    @Column
    Integer port;

    @Column
    String sshCredentialId;

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

}

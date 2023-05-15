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
    String port;

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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSshCredentialId() {
        return sshCredentialId;
    }

    public void setSshCredentialId(String sshCredentialId) {
        this.sshCredentialId = sshCredentialId;
    }
}

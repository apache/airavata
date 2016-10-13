package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the scp_data_movement database table.
 */
@Entity
@Table(name = "scp_data_movement")
public class ScpDataMovement implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DATA_MOVEMENT_INTERFACE_ID")
    private String dataMovementInterfaceId;

    @Column(name = "ALTERNATIVE_SCP_HOSTNAME")
    private String alternativeScpHostname;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "QUEUE_DESCRIPTION")
    private String queueDescription;

    @Column(name = "SECURITY_PROTOCOL")
    private String securityProtocol;

    @Column(name = "SSH_PORT")
    private int sshPort;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public ScpDataMovement() {
    }

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public String getAlternativeScpHostname() {
        return alternativeScpHostname;
    }

    public void setAlternativeScpHostname(String alternativeScpHostname) {
        this.alternativeScpHostname = alternativeScpHostname;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public void setQueueDescription(String queueDescription) {
        this.queueDescription = queueDescription;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
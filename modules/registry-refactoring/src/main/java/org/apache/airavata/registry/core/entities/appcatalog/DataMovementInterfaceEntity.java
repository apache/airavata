package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;

/**
 * The persistent class for the data_movement_interface database table.
 */
@Entity
@Table(name = "data_movement_interface")
public class DataMovementInterfaceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DataMovementInterfacePK id;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "DATA_MOVEMENT_PROTOCOL")
    private String dataMovementProtocol;

    @Column(name = "PRIORITY_ORDER")
    private int priorityOrder;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public DataMovementInterfaceEntity() {
    }

    public DataMovementInterfacePK getId() {
        return id;
    }

    public void setId(DataMovementInterfacePK id) {
        this.id = id;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getDataMovementProtocol() {
        return dataMovementProtocol;
    }

    public void setDataMovementProtocol(String dataMovementProtocol) {
        this.dataMovementProtocol = dataMovementProtocol;
    }

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
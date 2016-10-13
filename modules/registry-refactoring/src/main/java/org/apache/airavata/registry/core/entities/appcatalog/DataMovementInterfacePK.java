package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the data_movement_interface database table.
 */
@Embeddable
public class DataMovementInterfacePK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    @Column(name = "COMPUTE_RESOURCE_ID", insertable = false, updatable = false)
    private String computeResourceId;

    @Column(name = "DATA_MOVEMENT_INTERFACE_ID")
    private String dataMovementInterfaceId;

    public DataMovementInterfacePK() {
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DataMovementInterfacePK)) {
            return false;
        }
        DataMovementInterfacePK castOther = (DataMovementInterfacePK) other;
        return
                this.computeResourceId.equals(castOther.computeResourceId)
                        && this.dataMovementInterfaceId.equals(castOther.dataMovementInterfaceId);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.computeResourceId.hashCode();
        hash = hash * prime + this.dataMovementInterfaceId.hashCode();

        return hash;
    }
}
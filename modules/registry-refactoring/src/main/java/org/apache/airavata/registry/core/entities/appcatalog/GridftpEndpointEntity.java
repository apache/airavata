package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the gridftp_endpoint database table.
 */
@Entity
@Table(name = "gridftp_endpoint")
public class GridftpEndpointEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private GridftpEndpointPK id;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;


    public GridftpEndpointEntity() {
    }

    public GridftpEndpointPK getId() {
        return id;
    }

    public void setId(GridftpEndpointPK id) {
        this.id = id;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
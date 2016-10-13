package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the job_manager_command database table.
 */
@Embeddable
public class JobManagerCommandPK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    @Column(name = "RESOURCE_JOB_MANAGER_ID", insertable = false, updatable = false)
    private String resourceJobManagerId;

    @Column(name = "COMMAND_TYPE")
    private String commandType;

    public JobManagerCommandPK() {
    }

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JobManagerCommandPK)) {
            return false;
        }
        JobManagerCommandPK castOther = (JobManagerCommandPK) other;
        return
                this.resourceJobManagerId.equals(castOther.resourceJobManagerId)
                        && this.commandType.equals(castOther.commandType);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.resourceJobManagerId.hashCode();
        hash = hash * prime + this.commandType.hashCode();

        return hash;
    }
}
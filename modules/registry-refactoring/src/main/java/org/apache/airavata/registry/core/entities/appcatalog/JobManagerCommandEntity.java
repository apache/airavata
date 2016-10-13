package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the job_manager_command database table.
 */
@Entity
@Table(name = "job_manager_command")
@NamedQuery(name = "JobManagerCommand.findAll", query = "SELECT j FROM JobManagerCommand j")
public class JobManagerCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private JobManagerCommandPK id;

    @Column(name = "COMMAND")
    private String command;

    public JobManagerCommand() {
    }

    public JobManagerCommandPK getId() {
        return id;
    }

    public void setId(JobManagerCommandPK id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
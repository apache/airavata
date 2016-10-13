package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the parallelism_command database table.
 */
@Entity
@Table(name = "parallelism_command")
public class ParallelismCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ParallelismCommandPK id;

    @Column(name = "COMMAND")
    private String command;

    public ParallelismCommand() {
    }

    public ParallelismCommandPK getId() {
        return id;
    }

    public void setId(ParallelismCommandPK id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
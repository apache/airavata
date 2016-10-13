package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the prejob_command database table.
 */
@Entity
@Table(name = "prejob_command")
@NamedQuery(name = "PrejobCommand.findAll", query = "SELECT p FROM PrejobCommand p")
public class PrejobCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PrejobCommandPK id;


    public PrejobCommand() {
    }

    public PrejobCommandPK getId() {
        return id;
    }

    public void setId(PrejobCommandPK id) {
        this.id = id;
    }
}
package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the gsissh_prejobcommand database table.
 */
@Entity
@Table(name = "gsissh_prejobcommand")
public class GsisshPrejobcommand implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private GsisshPrejobcommandPK id;


    public GsisshPrejobcommand() {
    }

    public GsisshPrejobcommandPK getId() {
        return id;
    }

    public void setId(GsisshPrejobcommandPK id) {
        this.id = id;
    }
}
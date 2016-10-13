package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the gsissh_export database table.
 */
@Entity
@Table(name = "gsissh_export")
public class GsisshExport implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private GsisshExportPK id;

    public GsisshExport() {
    }

    public GsisshExportPK getId() {
        return id;
    }

    public void setId(GsisshExportPK id) {
        this.id = id;
    }
}
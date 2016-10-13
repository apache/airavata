package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the postjob_command database table.
 */
@Entity
@Table(name = "postjob_command")
public class PostjobCommandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PostjobCommandPK id;


    public PostjobCommandEntity() {
    }

    public PostjobCommandPK getId() {
        return id;
    }

    public void setId(PostjobCommandPK id) {
        this.id = id;
    }
}
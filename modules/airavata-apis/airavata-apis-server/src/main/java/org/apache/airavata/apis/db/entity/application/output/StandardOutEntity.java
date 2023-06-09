package org.apache.airavata.apis.db.entity.application.output;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class StandardOutEntity extends ApplicationOutputValueEntity {

    @Column
    private String destinationPath;

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }
}

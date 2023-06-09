package org.apache.airavata.apis.db.entity.application.output;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class FileOutputEntity extends ApplicationOutputValueEntity {

    @Column
    private String friendlyName;

    @Column
    private String destinationPath;

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }
}

package org.apache.airavata.apis.db.entity.application.input;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class FileInputEntity {

    @Id
    @Column(name = "INPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String inputId;

    @Column
    private String friendlyName;

    @Column
    private String destinationPath;

    public String getInputId() {
        return inputId;
    }

    @OneToOne(mappedBy = "fileInput")
    private ApplicationInputEntity applicationInput;

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

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

    public ApplicationInputEntity getApplicationInput() {
        return applicationInput;
    }

    public void setApplicationInput(ApplicationInputEntity applicationInput) {
        this.applicationInput = applicationInput;
    }
}

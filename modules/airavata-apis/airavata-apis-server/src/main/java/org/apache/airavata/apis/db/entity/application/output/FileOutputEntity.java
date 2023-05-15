package org.apache.airavata.apis.db.entity.application.output;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class FileOutputEntity {

    @Id
    @Column(name = "OUTPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String outputId;

    @Column
    private String friendlyName;

    @Column
    private String destinationPath;

    @OneToOne(mappedBy = "fileOutput")
    private ApplicationOutputEntity applicationOutput;

    public ApplicationOutputEntity getApplicationOutput() {
        return applicationOutput;
    }

    public void setApplicationOutput(ApplicationOutputEntity applicationOutput) {
        this.applicationOutput = applicationOutput;
    }

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
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
}

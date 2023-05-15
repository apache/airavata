package org.apache.airavata.apis.db.entity.application.output;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class StandardOutEntity {

    @Id
    @Column(name = "OUTPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String outputId;

    @Column
    private String destinationPath;

    @OneToOne(mappedBy = "standardOut")
    private ApplicationOutputEntity applicationOutput;

    public ApplicationOutputEntity getApplicationOutput() {
        return applicationOutput;
    }

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }
}

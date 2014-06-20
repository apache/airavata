package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GATEWAY_PROFILE")
public class InputOutputs {

    @Id
    @Column(name = "INPUT_OUTPUT_ID")
    private String inputOutputID;

    @Column(name = "INPUT_OUTPUT_NAME")
    private String inputOutputName;

    @Column(name = "INPUT_OUTPUT_TYPE")
    private String inputOutputType;

    @Column(name = "MAXSIZE")
    private int maxSize;

    @Column(name = "MINSIZE")
    private int minSize;

    public String getInputOutputID() {
        return inputOutputID;
    }

    public void setInputOutputID(String inputOutputID) {
        this.inputOutputID = inputOutputID;
    }

    public String getInputOutputName() {
        return inputOutputName;
    }

    public void setInputOutputName(String inputOutputName) {
        this.inputOutputName = inputOutputName;
    }

    public String getInputOutputType() {
        return inputOutputType;
    }

    public void setInputOutputType(String inputOutputType) {
        this.inputOutputType = inputOutputType;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }
}

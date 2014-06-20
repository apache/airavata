package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "APPLICATION_INPUT_OUTPUTS")
@IdClass(ApplicationInputOutputsPK.class)
public class ApplicationInputOutputs {

    @Id
    @Column(name = "APPLICATION_ID")
    private String applicationID;

    @Id
    @Column(name = "INPUT_OUTPUT_ID")
    private String inputOutputID;

    @Column(name = "IS_INPUT")
    private boolean input;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "INPUT_OUTPUT_ID")
    private InputOutputs inputOutputs;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "APPLICATION_ID")
    private Application application;

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getInputOutputID() {
        return inputOutputID;
    }

    public void setInputOutputID(String inputOutputID) {
        this.inputOutputID = inputOutputID;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public InputOutputs getInputOutputs() {
        return inputOutputs;
    }

    public void setInputOutputs(InputOutputs inputOutputs) {
        this.inputOutputs = inputOutputs;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}

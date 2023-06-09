package org.apache.airavata.apis.db.entity.application.input;

import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class ApplicationInputEntity {

    @Id
    @Column(name = "APPLICATION_INPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String applicationInputId;

    @Column
    private int index;

    @Column
    private boolean required;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "input_id")
    private ApplicationInputValueEntity applicationInputValue;

    @ManyToOne
    @JoinColumn(name="application_id", nullable=false)
    private ApplicationEntity application;

    public ApplicationEntity getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntity application) {
        this.application = application;
    }

    public String getApplicationInputId() {
        return applicationInputId;
    }

    public void setApplicationInputId(String applicationInputId) {
        this.applicationInputId = applicationInputId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    /*
     * Helper getters/setters for mapping from/to protobuf messages
     */

    public CommandLineInputEntity getCommandLineInput() {
        return applicationInputValue instanceof CommandLineInputEntity ? (CommandLineInputEntity) applicationInputValue
                : null;
    }

    public void setCommandLineInput(CommandLineInputEntity commandLineInput) {
        this.applicationInputValue = commandLineInput;
    }

    public EnvironmentInputEntity getEnvironmentInput() {
        return applicationInputValue instanceof EnvironmentInputEntity ? (EnvironmentInputEntity) applicationInputValue
                : null;
    }

    public void setEnvironmentInput(EnvironmentInputEntity environmentInput) {
        this.applicationInputValue = environmentInput;
    }

    public FileInputEntity getFileInput() {
        return applicationInputValue instanceof FileInputEntity ? (FileInputEntity) applicationInputValue : null;
    }

    public void setFileInput(FileInputEntity fileInput) {
        this.applicationInputValue = fileInput;
    }
}

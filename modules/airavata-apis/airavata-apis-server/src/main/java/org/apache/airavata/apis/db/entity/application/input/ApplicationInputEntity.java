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
    @JoinColumn(name = "command_line_input_id", referencedColumnName = "input_id")
    private CommandLineInputEntity commandLineInput;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "environment_input_id", referencedColumnName = "input_id")
    private EnvironmentInputEntity environmentInput;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_input_id", referencedColumnName = "input_id")
    private FileInputEntity fileInput;

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

    public CommandLineInputEntity getCommandLineInput() {
        return commandLineInput;
    }

    public void setCommandLineInput(CommandLineInputEntity commandLineInput) {
        this.commandLineInput = commandLineInput;
    }

    public EnvironmentInputEntity getEnvironmentInput() {
        return environmentInput;
    }

    public void setEnvironmentInput(EnvironmentInputEntity environmentInput) {
        this.environmentInput = environmentInput;
    }

    public FileInputEntity getFileInput() {
        return fileInput;
    }

    public void setFileInput(FileInputEntity fileInput) {
        this.fileInput = fileInput;
    }
}

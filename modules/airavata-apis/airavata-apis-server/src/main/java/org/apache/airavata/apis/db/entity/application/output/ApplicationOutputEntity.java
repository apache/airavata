package org.apache.airavata.apis.db.entity.application.output;

import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class ApplicationOutputEntity {

    @Id
    @Column(name = "APPLICATION_OUTPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String applicationOutputId;

    @Column
    private int index;

    @Column
    private boolean required;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_output_id", referencedColumnName = "output_id")
    private FileOutputEntity fileOutput;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "std_error_output_id", referencedColumnName = "output_id")
    private StandardErrorEntity standardError;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "std_out_output_id", referencedColumnName = "output_id")
    private StandardOutEntity standardOut;

    @ManyToOne
    @JoinColumn(name="application_id", nullable=false)
    private ApplicationEntity application;

    public ApplicationEntity getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntity application) {
        this.application = application;
    }

    public String getApplicationOutputId() {
        return applicationOutputId;
    }

    public void setApplicationOutputId(String applicationOutputId) {
        this.applicationOutputId = applicationOutputId;
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

    public FileOutputEntity getFileOutput() {
        return fileOutput;
    }

    public void setFileOutput(FileOutputEntity fileOutput) {
        this.fileOutput = fileOutput;
    }

    public StandardErrorEntity getStandardError() {
        return standardError;
    }

    public void setStandardError(StandardErrorEntity standardError) {
        this.standardError = standardError;
    }

    public StandardOutEntity getStandardOut() {
        return standardOut;
    }

    public void setStandardOut(StandardOutEntity standardOut) {
        this.standardOut = standardOut;
    }
}

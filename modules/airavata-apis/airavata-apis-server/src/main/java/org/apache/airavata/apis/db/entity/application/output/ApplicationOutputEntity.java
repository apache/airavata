package org.apache.airavata.apis.db.entity.application.output;

import org.apache.airavata.apis.db.entity.BaseEntity;
import org.apache.airavata.apis.db.entity.application.ApplicationEntity;

import javax.persistence.*;

@Entity
public class ApplicationOutputEntity extends BaseEntity {

    @Column
    private int index;

    @Column
    private boolean required;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "output_id")
    private ApplicationOutputValueEntity applicationOutputValue;

    @ManyToOne
    @JoinColumn(name="application_id", nullable=false)
    private ApplicationEntity application;

    public ApplicationEntity getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntity application) {
        this.application = application;
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

    public ApplicationOutputValueEntity getApplicationOutputValue() {
        return applicationOutputValue;
    }

    public void setApplicationOutputValue(ApplicationOutputValueEntity applicationOutputValue) {
        this.applicationOutputValue = applicationOutputValue;
    }

    public FileOutputEntity getFileOutput() {
        return applicationOutputValue instanceof FileOutputEntity ? (FileOutputEntity) applicationOutputValue : null;
    }

    public void setFileOutput(FileOutputEntity fileOutput) {
        this.applicationOutputValue = fileOutput;
    }

    public StandardErrorEntity getStdErr() {
        return applicationOutputValue instanceof StandardErrorEntity ? (StandardErrorEntity) applicationOutputValue
                : null;
    }

    public void setStdErr(StandardErrorEntity standardError) {
        this.applicationOutputValue = standardError;
    }

    public StandardOutEntity getStdOut() {
        return applicationOutputValue instanceof StandardOutEntity ? (StandardOutEntity) applicationOutputValue : null;
    }

    public void setStdOut(StandardOutEntity standardOut) {
        this.applicationOutputValue = standardOut;
    }
}

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

    public StandardErrorEntity getStandardError() {
        return applicationOutputValue instanceof StandardErrorEntity ? (StandardErrorEntity) applicationOutputValue
                : null;
    }

    public void setStandardError(StandardErrorEntity standardError) {
        this.applicationOutputValue = standardError;
    }

    public StandardOutEntity getStandardOut() {
        return applicationOutputValue instanceof StandardOutEntity ? (StandardOutEntity) applicationOutputValue : null;
    }

    public void setStandardOut(StandardOutEntity standardOut) {
        this.applicationOutputValue = standardOut;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationOutputId == null) ? 0 : applicationOutputId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApplicationOutputEntity other = (ApplicationOutputEntity) obj;
        if (applicationOutputId == null) {
            if (other.applicationOutputId != null)
                return false;
        } else if (!applicationOutputId.equals(other.applicationOutputId))
            return false;
        return true;
    }

}

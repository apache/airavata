package org.apache.airavata.apis.db.entity.application.input;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ApplicationInputValueEntity {

    @Id
    @Column(name = "INPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String inputId;

    @OneToOne(mappedBy = "applicationInputValue")
    private ApplicationInputEntity applicationInput;

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public ApplicationInputEntity getApplicationInput() {
        return applicationInput;
    }

    public void setApplicationInput(ApplicationInputEntity applicationInput) {
        this.applicationInput = applicationInput;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inputId == null) ? 0 : inputId.hashCode());
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
        ApplicationInputValueEntity other = (ApplicationInputValueEntity) obj;
        if (inputId == null) {
            if (other.inputId != null)
                return false;
        } else if (!inputId.equals(other.inputId))
            return false;
        return true;
    }

}

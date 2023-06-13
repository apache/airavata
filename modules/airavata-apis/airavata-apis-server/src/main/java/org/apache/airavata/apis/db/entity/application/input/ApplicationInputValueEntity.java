package org.apache.airavata.apis.db.entity.application.input;

import org.apache.airavata.apis.db.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ApplicationInputValueEntity extends BaseEntity {

    @OneToOne(mappedBy = "applicationInputValue")
    private ApplicationInputEntity applicationInput;

    public ApplicationInputEntity getApplicationInput() {
        return applicationInput;
    }

    public void setApplicationInput(ApplicationInputEntity applicationInput) {
        this.applicationInput = applicationInput;
    }

}

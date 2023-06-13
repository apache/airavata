package org.apache.airavata.apis.db.entity.application.output;

import org.apache.airavata.apis.db.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ApplicationOutputValueEntity extends BaseEntity {

    @OneToOne(mappedBy = "applicationOutputValue")
    private ApplicationOutputEntity applicationOutput;

    public ApplicationOutputEntity getApplicationOutput() {
        return applicationOutput;
    }

    public void setApplicationOutput(ApplicationOutputEntity applicationOutput) {
        this.applicationOutput = applicationOutput;
    }
}

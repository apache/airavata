package org.apache.airavata.apis.db.entity.application;

import org.apache.airavata.apis.db.entity.BaseEntity;
import org.apache.airavata.apis.db.entity.application.input.ApplicationInputEntity;
import org.apache.airavata.apis.db.entity.application.output.ApplicationOutputEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import java.util.Set;

@Entity
public class ApplicationEntity extends BaseEntity {

    @Column(name = "APPLICATION_NAME")
    private String name;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private Set<ApplicationInputEntity> inputs;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private Set<ApplicationOutputEntity> outputs;

    public Set<ApplicationInputEntity> getInputs() {
        return inputs;
    }

    public void setInputs(Set<ApplicationInputEntity> inputs) {
        this.inputs = inputs;
        for (ApplicationInputEntity input : inputs) {
            input.setApplication(this);
        }
    }

    public Set<ApplicationOutputEntity> getOutputs() {
        return outputs;
    }

    public void setOutputs(Set<ApplicationOutputEntity> outputs) {
        this.outputs = outputs;
        for (ApplicationOutputEntity output : outputs) {
            output.setApplication(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package org.apache.airavata.apis.db.entity.application;

import org.apache.airavata.apis.db.entity.application.input.ApplicationInputEntity;
import org.apache.airavata.apis.db.entity.application.output.ApplicationOutputEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ApplicationEntity {

    @Id
    @Column(name = "APPLICATION_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String applicationId;

    @Column(name = "APPLICATION_NAME")
    private String name;

    public String getApplicationId() {
        return applicationId;
    }

    @OneToMany(mappedBy="application")
    private Set<ApplicationInputEntity> inputs;

    @OneToMany(mappedBy="application")
    private Set<ApplicationOutputEntity> outputs;

    public Set<ApplicationInputEntity> getInputs() {
        return inputs;
    }

    public void setInputs(Set<ApplicationInputEntity> inputs) {
        this.inputs = inputs;
    }

    public Set<ApplicationOutputEntity> getOutputs() {
        return outputs;
    }

    public void setOutputs(Set<ApplicationOutputEntity> outputs) {
        this.outputs = outputs;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

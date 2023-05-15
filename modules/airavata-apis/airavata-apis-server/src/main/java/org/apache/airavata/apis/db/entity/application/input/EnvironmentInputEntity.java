package org.apache.airavata.apis.db.entity.application.input;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class EnvironmentInputEntity {

    @Id
    @Column(name = "INPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String inputId;

    @Column
    private String key;

    @Column
    private String value;

    @OneToOne(mappedBy = "environmentInput")
    private ApplicationInputEntity applicationInput;

    public ApplicationInputEntity getApplicationInput() {
        return applicationInput;
    }

    public void setApplicationInput(ApplicationInputEntity applicationInput) {
        this.applicationInput = applicationInput;
    }

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

package org.apache.airavata.apis.db.entity.application.input;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class CommandLineInputEntity {

    @Id
    @Column(name = "INPUT_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String inputId;

    @Column
    private int position = 1;

    @Column
    private String prefix;

    @Column
    private String value;

    @OneToOne(mappedBy = "commandLineInput")
    private ApplicationInputEntity applicationInput;
    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ApplicationInputEntity getApplicationInput() {
        return applicationInput;
    }

    public void setApplicationInput(ApplicationInputEntity applicationInput) {
        this.applicationInput = applicationInput;
    }
}

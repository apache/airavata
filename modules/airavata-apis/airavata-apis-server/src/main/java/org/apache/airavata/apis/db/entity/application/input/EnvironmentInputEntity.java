package org.apache.airavata.apis.db.entity.application.input;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class EnvironmentInputEntity extends ApplicationInputValueEntity {

    @Column
    private String key;

    @Column
    private String value;

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

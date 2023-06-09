package org.apache.airavata.apis.db.entity.application.input;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class CommandLineInputEntity extends ApplicationInputValueEntity {

    @Column
    private int position = 1;

    @Column
    private String prefix;

    @Column
    private String value;

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
}

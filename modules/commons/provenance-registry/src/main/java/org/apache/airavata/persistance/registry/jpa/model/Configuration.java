package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;

@Entity
public class Configuration {
    @Id
    private int config_ID;
    private String config_key;
    private String config_val;
    private Date expire_date;

    public int getConfig_ID() {
        return config_ID;
    }

    public void setConfig_ID(int config_ID) {
        this.config_ID = config_ID;
    }

    public String getConfig_key() {
        return config_key;
    }

    public String getConfig_val() {
        return config_val;
    }

    public Date getExpire_date() {
        return expire_date;
    }

    public void setConfig_key(String config_key) {
        this.config_key = config_key;
    }

    public void setConfig_val(String config_val) {
        this.config_val = config_val;
    }

    public void setExpire_date(Date expire_date) {
        this.expire_date = expire_date;
    }
}

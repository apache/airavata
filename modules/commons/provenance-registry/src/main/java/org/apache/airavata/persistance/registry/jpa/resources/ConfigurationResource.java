package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Configuration;

import java.sql.Date;
import java.util.List;

public class ConfigurationResource extends AbstractResource {
    private int configID = -1;
    private String configKey;
    private String configVal;
    private Date expireDate;

    public ConfigurationResource(int configID) {
        this.configID = configID;
    }

    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public Date getExpireDate() {
        return expireDate;

    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public void save() {
        begin();
        Configuration configuration = new Configuration();
        configuration.setConfig_key(configKey);
        configuration.setConfig_val(configVal);
        configuration.setExpire_date(expireDate);
        if (configID != -1) {
            configuration.setConfig_ID(configID);
        }
        em.persist(configuration);
        end();
    }

    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public int getConfigID() {
        return configID;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getConfigVal() {
        return configVal;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public void setConfigVal(String configVal) {
        this.configVal = configVal;
    }
}

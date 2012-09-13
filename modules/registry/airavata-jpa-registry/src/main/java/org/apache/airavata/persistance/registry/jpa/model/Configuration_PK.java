package org.apache.airavata.persistance.registry.jpa.model;


public class Configuration_PK {
    private String config_key;
    private String config_val;

    public Configuration_PK(String config_key, String config_val) {
        this.config_key = config_key;
        this.config_val = config_val;
    }

    public Configuration_PK() {
        ;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getConfig_key() {
        return config_key;
    }

    public void setConfig_key(String config_key) {
        this.config_key = config_key;
    }

    public void setConfig_val(String config_val) {
        this.config_val = config_val;
    }

    public String getConfig_val() {
        return config_val;
    }
}

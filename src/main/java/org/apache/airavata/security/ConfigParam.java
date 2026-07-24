package org.apache.airavata.security;

public class ConfigParam {

    public enum ConfigParamType {
        STRING,
        CRED_STORE_PASSWORD_TOKEN,
    }

    private boolean optional = false;
    private String name;
    private String description;
    private ConfigParamType type = ConfigParamType.STRING;

    public ConfigParam(String name) {
        this.name = name;
    }

    public boolean isOptional() {
        return optional;
    }

    public ConfigParam setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public String getName() {
        return name;
    }

    public ConfigParam setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ConfigParam setDescription(String description) {
        this.description = description;
        return this;
    }

    public ConfigParamType getType() {
        return type;
    }

    public ConfigParam setType(ConfigParamType type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ConfigParam))
            return false;

        ConfigParam that = (ConfigParam) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

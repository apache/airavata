package org.apache.airavata.services.registry.rest.resourcemappings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConfigurationList {

    private Object[] configValList = new Object[]{};

    public Object[] getConfigValList() {
        return configValList;
    }

    public void setConfigValList(Object[] configValList) {
        this.configValList = configValList;
    }
}

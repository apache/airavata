package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.List;

import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;

public class ServiceDescriptions {
    private Registry registry;

    public ServiceDescriptions(Registry registry) {
        setRegistry(registry);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public List<ServiceDescription> getDescriptions() throws RegistryException {
        return getRegistry().searchServiceDescription("");
    }
}

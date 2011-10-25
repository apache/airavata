package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.List;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;

public class HostDescriptions {
    private Registry registry;

    public HostDescriptions(Registry registry) {
        setRegistry(registry);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public List<HostDescription> getDescriptions() throws RegistryException{
        return getRegistry().searchHostDescription(".*");
    }
}

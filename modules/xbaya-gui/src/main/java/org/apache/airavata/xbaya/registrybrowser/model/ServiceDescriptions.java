package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;

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

    public List<ServiceDescription> getDescriptions() throws ServiceDescriptionRetrieveException {
        try {
            return getRegistry().searchServiceDescription("");
        } catch (PathNotFoundException e) {
            // has no descriptions defined
            return new ArrayList<ServiceDescription>();
        }
    }
}

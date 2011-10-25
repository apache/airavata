package org.apache.airavata.xbaya.registrybrowser.model;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;

public class ApplicationDeploymentDescriptionWrap {
    private ApplicationDeploymentDescription applicationDeploymentDescription;
    private String service;
    private String host;
    private Registry registry;

    public ApplicationDeploymentDescriptionWrap(Registry registry,
            ApplicationDeploymentDescription applicationDeploymentDescription, String service, String host) {
        setApplicationDeploymentDescription(applicationDeploymentDescription);
        setService(service);
        setHost(host);
        setRegistry(registry);
    }

    public ApplicationDeploymentDescription getDescription() {
        return applicationDeploymentDescription;
    }

    public void setApplicationDeploymentDescription(ApplicationDeploymentDescription applicationDeploymentDescription) {
        this.applicationDeploymentDescription = applicationDeploymentDescription;
    }

    public String getService() {
        return service;
    }

    public ServiceDescription getServiceDescription() throws RegistryException{
        return getRegistry().getServiceDescription(getService());
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHost() {
        return host;
    }

    public HostDescription getHostDescription() throws RegistryException{
        return getRegistry().getHostDescription(getHost());
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}

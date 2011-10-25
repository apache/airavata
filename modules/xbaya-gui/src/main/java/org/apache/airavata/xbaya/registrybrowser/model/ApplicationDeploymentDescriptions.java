package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;

public class ApplicationDeploymentDescriptions {
    private Registry registry;

    public ApplicationDeploymentDescriptions(Registry registry) {
        setRegistry(registry);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public List<ApplicationDeploymentDescriptionWrap> getDescriptions() throws RegistryException {
        List<ApplicationDeploymentDescriptionWrap> list = new ArrayList<ApplicationDeploymentDescriptionWrap>();
        Map<ApplicationDeploymentDescription, String> deploymentDescriptions = getRegistry()
                .searchDeploymentDescription();
        for (ApplicationDeploymentDescription descriptionWrap : deploymentDescriptions.keySet()) {
            String[] descDetails = deploymentDescriptions.get(descriptionWrap).split("\\$");
            list.add(new ApplicationDeploymentDescriptionWrap(getRegistry(), descriptionWrap, descDetails[0],
                    descDetails[1]));
        }
        return list;
    }
}

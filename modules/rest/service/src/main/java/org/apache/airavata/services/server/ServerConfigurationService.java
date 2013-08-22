package org.apache.airavata.services.server;

import javax.ws.rs.Path;

import org.apache.airavata.rest.mappings.utils.ResourcePathConstants.ServerManagerConstants;
import org.apache.airavata.services.registry.rest.resources.ConfigurationRegistryResource;

@Path(ServerManagerConstants.PATH)
public class ServerConfigurationService extends ConfigurationRegistryResource{

}

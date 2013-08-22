package org.apache.airavata.services.gateway;

import javax.ws.rs.Path;

import org.apache.airavata.rest.mappings.utils.ResourcePathConstants.ApplicationDataConstants;
import org.apache.airavata.services.registry.rest.resources.DescriptorRegistryResource;

@Path(ApplicationDataConstants.PATH)
public class ApplicationDataService extends DescriptorRegistryResource {

}

package org.apache.airavata.services.gateway;

import javax.ws.rs.Path;

import org.apache.airavata.rest.mappings.utils.ResourcePathConstants.WorkflowDataConstants;
import org.apache.airavata.services.registry.rest.resources.UserWorkflowRegistryResource;

@Path(WorkflowDataConstants.PATH)
public class WorkflowDataService extends UserWorkflowRegistryResource {

}

package org.apache.airavata.services.gateway;

import javax.ws.rs.Path;

import org.apache.airavata.persistance.registry.jpa.resources.UserWorkflowResource;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants.WorkflowDataConstants;

@Path(WorkflowDataConstants.PATH)
public class PublishedWorkflowDataService extends UserWorkflowResource {

}

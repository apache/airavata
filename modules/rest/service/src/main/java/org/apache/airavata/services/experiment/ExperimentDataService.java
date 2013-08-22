package org.apache.airavata.services.experiment;

import javax.ws.rs.Path;

import org.apache.airavata.rest.mappings.utils.ResourcePathConstants.ExperimentDataConstants;
import org.apache.airavata.services.registry.rest.resources.ProvenanceRegistryResource;

@Path(ExperimentDataConstants.PATH)
public class ExperimentDataService extends ProvenanceRegistryResource{

}

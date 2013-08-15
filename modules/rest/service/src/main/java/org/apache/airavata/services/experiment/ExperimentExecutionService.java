/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.services.experiment;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.airavata.client.AiravataAPIUtils;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.common.context.WorkflowContext;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.WebAppUtil;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorSkeleton;

@Path(ResourcePathConstants.ExperimentExecutionConstants.EXP_EXEC_PATH)
public class ExperimentExecutionService {
	private WorkflowInterpretorSkeleton interpreterService;
	
    @POST
    @Path(ResourcePathConstants.ExperimentExecutionConstants.EXEC_EXPERIMENT)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response runExperiment(@QueryParam("workflowTemplateName") String workflowTemplateName, Map<String, String> workflowInputs, ExperimentAdvanceOptions advanceOptions){
    	String user =  WorkflowContext.getRequestUser();
        String gatewayId = WorkflowContext.getGatewayId();
    	try {
			String experimentId = getInterpreterService().setupAndLaunch(workflowTemplateName, advanceOptions.getCustomExperimentId(), gatewayId, user, workflowInputs, true, AiravataAPIUtils.createWorkflowContextHeaderBuilder(advanceOptions, advanceOptions.getExperimentExecutionUser(),user));
	    	Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	        builder.entity(experimentId);
	        return builder.build();
		} catch (Exception e) {
			return WebAppUtil.reportInternalServerError(ResourcePathConstants.ExperimentExecutionConstants.EXEC_EXPERIMENT, e);
		}

    }

    @DELETE
    @Path(ResourcePathConstants.ExperimentExecutionConstants.CANCEL_EXPERIMENT)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response cancelExperiment(@QueryParam("experimentId") String experimentId){
    	try {
			getInterpreterService().haltWorkflow(experimentId);
	    	Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	        return builder.build();
		} catch (Exception e) {
			return WebAppUtil.reportInternalServerError(ResourcePathConstants.ExperimentExecutionConstants.CANCEL_EXPERIMENT, e);
		}
    }

    @DELETE
    @Path(ResourcePathConstants.ExperimentExecutionConstants.SUSPEND_EXPERIMENT)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response suspendExperiment(@QueryParam("experimentId") String experimentId){
    	try {
			getInterpreterService().suspendWorkflow(experimentId);
	    	Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	        return builder.build();
		} catch (Exception e) {
			return WebAppUtil.reportInternalServerError(ResourcePathConstants.ExperimentExecutionConstants.SUSPEND_EXPERIMENT, e);
		}
    }

    @GET
    @Path(ResourcePathConstants.ExperimentExecutionConstants.RESUME_EXPERIMENT)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response resumeExperiment(@QueryParam("experimentId") String experimentId){
    	try {
			getInterpreterService().resumeWorkflow(experimentId);
	    	Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	        return builder.build();
		} catch (Exception e) {
			return WebAppUtil.reportInternalServerError(ResourcePathConstants.ExperimentExecutionConstants.RESUME_EXPERIMENT, e);
		}
    }
    
	public WorkflowInterpretorSkeleton getInterpreterService() {
		if (interpreterService==null){
			interpreterService=new WorkflowInterpretorSkeleton();
		}
		return interpreterService;
	}
    

}

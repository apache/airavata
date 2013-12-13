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

package org.apache.airavata.services.registry.rest.resources;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.rest.mappings.utils.RegPoolUtils;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.WebAppUtil;

/**
 * This class provides a REST interface to all the operations related to credential store
 */
@Path(ResourcePathConstants.CredentialResourceConstants.REGISTRY_API_CREDENTIALREGISTRY)
public class CredentialRegistryResource {
    
	@Context
    ServletContext context;

    /**
     * This method will check whether a credential exists for a given tokenId and gateway
     *
     * @param String gatewayId
     * @param String tokenId
     * @return HTTP response boolean
     */
    @GET
    @Path(ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL_EXIST)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response isCredentialExist(@QueryParam("gatewayId") String gatewayId, @QueryParam("tokenId") String tokenId) {
    	if(gatewayId==null || gatewayId.isEmpty() || tokenId == null || tokenId.isEmpty()) {
    		Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    		builder.entity("gatewayId or username can't be null");
    		return builder.build();
    	}
    	
    	AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
    	try {
         	String publicKey = airavataRegistry.getCredentialPublicKey(gatewayId,tokenId);
	    	if (publicKey!=null && publicKey.isEmpty()) {
	    		Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	    		builder.entity("true");
	    		return builder.build();
	    	} else {
	    		Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	    		builder.entity("false");
	    		return builder.build();
	    	}
    	} catch (Throwable e) {
    		return WebAppUtil.reportInternalServerError(ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL, e);
    	}
    }
    
    
    /**
     * This method will get the public key of the ssh credential exists for a given user and gateway
     *
     * @param String gatewayId
     * @param String tokenId
     * @return HTTP response - The public key of the credential 
     */
    @GET
    @Path(ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCredentialPublicKey(@QueryParam("gatewayId") String gatewayId, @QueryParam("tokenId") String tokenId) {
    	if(gatewayId==null || gatewayId.isEmpty() || tokenId == null || tokenId.isEmpty()) {
    		Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    		builder.entity("gatewayId or username can't be null");
    		return builder.build();
    	}
    	
    	AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
    	try {
         	String publicKey = airavataRegistry.getCredentialPublicKey(gatewayId,tokenId);
	    	if (publicKey!=null && publicKey.isEmpty()) {
	    		Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	    		builder.entity(publicKey);
	    		return builder.build();
	    	} else {
	    		Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
	    		return builder.build();
	    	}
		} catch (Throwable e) {
			return WebAppUtil.reportInternalServerError(ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL, e);
		}
    }
    
    
    /**
     * This method will create a new ssh credential for a given user, gateway and return the public key of the keypair
     *
     * @param String gatewayId
     * @param String tokenId
     * @return HTTP response - The public key of the credential 
     */
    @POST
    @Path(ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createCredential(@QueryParam("gatewayId") String gatewayId, @QueryParam("tokenId") String tokenId) {
    	if(gatewayId==null || gatewayId.isEmpty() || tokenId == null || tokenId.isEmpty()) {
    		Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    		builder.entity("gatewayId or username can't be null");
    		return builder.build();
    	}
    	AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
    	try {
         	String publicKey = airavataRegistry.createCredential(gatewayId, tokenId);
	    	if (publicKey!=null && publicKey.isEmpty()) {
	    		Response.ResponseBuilder builder = Response.status(Response.Status.OK);
	    		builder.entity(publicKey);
	    		return builder.build();
	    	} else {
	    		Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
	    		builder.entity("Error creating credential");
	    		return builder.build();
	    	}
    	} catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.CredentialResourceConstants.SSH_CREDENTIAL, e);
        }
    }
    
    

}

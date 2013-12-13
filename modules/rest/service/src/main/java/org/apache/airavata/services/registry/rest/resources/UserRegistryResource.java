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

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.rest.mappings.resourcemappings.UserList;
import org.apache.airavata.rest.mappings.utils.RegPoolUtils;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.WebAppUtil;

/**
 * This class provides a REST interface to all the user management related operations
 */
@Path(ResourcePathConstants.UserResourceConstants.REGISTRY_API_USERREGISTRY)
public class UserRegistryResource {

    @Context
    ServletContext context;

    /**
     * This method gets all users of Airavata present in the registry
     *
     * @return HTTP response - List of AiravataUsers
     */
    @GET
    @Path(ResourcePathConstants.UserResourceConstants.GET_ALL_USERS)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAllUsers() {
        AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
        try {
        	List<AiravataUser> users = airavataRegistry.getUsers();
        	UserList userList = new UserList();
        	userList.setUserList(users);
            if (users.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(userList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.UserResourceConstants.GET_ALL_USERS, e);
        } finally {
            if (airavataRegistry != null) {
                RegPoolUtils.releaseRegistry(context, airavataRegistry);
            }
        }
    }

}

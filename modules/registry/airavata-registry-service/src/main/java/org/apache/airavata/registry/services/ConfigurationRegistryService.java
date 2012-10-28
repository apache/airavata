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

package org.apache.airavata.registry.services;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Date;

public interface ConfigurationRegistryService {
    public Response getConfiguration(String key);

    public Response getConfigurationList(String key);

    public Response setConfiguration(String key, String value, Date expire);

    public Response addConfiguration(String key, String value, Date expire);

    public Response removeAllConfiguration(String key);

    public Response removeConfiguration(String key, String value);

    public Response getGFacURIs();

    public Response getWorkflowInterpreterURIs();

    public Response getEventingServiceURI();

    public Response getMessageBoxURI();

    public Response addGFacURI(URI uri);

    public Response addWorkflowInterpreterURI(URI uri);

    public Response setEventingURI(URI uri);

    public Response setMessageBoxURI(URI uri);

    public Response addGFacURIByDate(URI uri, Date expire);

    public Response addWorkflowInterpreterURI(URI uri, Date expire);

    public Response setEventingURIByDate(URI uri, Date expire);

    public Response setMessageBoxURIByDate(URI uri, Date expire);

    public Response removeGFacURI(URI uri);

    public Response removeAllGFacURI();

    public Response removeWorkflowInterpreterURI(URI uri);

    public Response removeAllWorkflowInterpreterURI();

    public Response unsetEventingURI();

    public Response unsetMessageBoxURI();

}

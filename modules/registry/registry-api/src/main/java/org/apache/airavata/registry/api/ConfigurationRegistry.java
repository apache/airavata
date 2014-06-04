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

package org.apache.airavata.registry.api;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.airavata.registry.api.exception.RegException;

public interface ConfigurationRegistry extends AiravataSubRegistry {
	public Object getConfiguration(String key) throws RegException;
	public List<Object> getConfigurationList(String key) throws RegException;
	public void setConfiguration(String key, String value, Date expire) throws RegException;
	public void addConfiguration(String key, String value, Date expire) throws RegException;
	public void removeAllConfiguration(String key) throws RegException;
	public void removeConfiguration(String key, String value) throws RegException;
	
	public List<URI> getGFacURIs() throws RegException;
	public List<URI> getWorkflowInterpreterURIs() throws RegException;
	public URI getEventingServiceURI() throws RegException;
	public URI getMessageBoxURI() throws RegException;
	
	public void addGFacURI(URI uri) throws RegException;
	public void addWorkflowInterpreterURI(URI uri) throws RegException;
	public void setEventingURI(URI uri) throws RegException;
	public void setMessageBoxURI(URI uri) throws RegException;

	public void addGFacURI(URI uri, Date expire) throws RegException;
	public void addWorkflowInterpreterURI(URI uri, Date expire) throws RegException;
	public void setEventingURI(URI uri, Date expire) throws RegException;
	public void setMessageBoxURI(URI uri, Date expire) throws RegException;
	
	public void removeGFacURI(URI uri) throws RegException;
	public void removeAllGFacURI() throws RegException;
	public void removeWorkflowInterpreterURI(URI uri) throws RegException;
	public void removeAllWorkflowInterpreterURI() throws RegException;
	public void unsetEventingURI() throws RegException;
	public void unsetMessageBoxURI() throws RegException;
}

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

import org.apache.airavata.registry.api.exception.RegistryException;

public interface ConfigurationRegistry extends AiravataSubRegistry {
	public Object getConfiguration(String key) throws RegistryException;
	public List<Object> getConfigurationList(String key) throws RegistryException;
	public void setConfiguration(String key, String value, Date expire) throws RegistryException;
	public void addConfiguration(String key, String value, Date expire) throws RegistryException;
	public void removeAllConfiguration(String key) throws RegistryException;
	public void removeConfiguration(String key, String value) throws RegistryException;
	
	public List<URI> getGFacURIs() throws RegistryException;
	public List<URI> getWorkflowInterpreterURIs() throws RegistryException;
	public URI getEventingServiceURI() throws RegistryException;
	public URI getMessageBoxURI() throws RegistryException;
	
	public void addGFacURI(URI uri) throws RegistryException;
	public void addWorkflowInterpreterURI(URI uri) throws RegistryException;
	public void setEventingURI(URI uri) throws RegistryException;
	public void setMessageBoxURI(URI uri) throws RegistryException;

	public void addGFacURI(URI uri, Date expire) throws RegistryException;
	public void addWorkflowInterpreterURI(URI uri, Date expire) throws RegistryException;
	public void setEventingURI(URI uri, Date expire) throws RegistryException;
	public void setMessageBoxURI(URI uri, Date expire) throws RegistryException;
	
	public void removeGFacURI(URI uri) throws RegistryException;
	public void removeAllGFacURI() throws RegistryException;
	public void removeWorkflowInterpreterURI(URI uri) throws RegistryException;
	public void removeAllWorkflowInterpreterURI() throws RegistryException;
	public void unsetEventingURI() throws RegistryException;
	public void unsetMessageBoxURI() throws RegistryException;
}

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

public interface ConfigurationRegistry extends AiravataSubRegistry {
	public Object getConfiguration(String key);
	public List<Object> getConfigurationList(String key);
	public void setConfiguration(String key, String value, Date expire);
	public void addConfiguration(String key, String value, Date expire);
	public void removeAllConfiguration(String key);
	public void removeConfiguration(String key, String value);
	
	public List<URI> getGFacURIs();
	public List<URI> getWorkflowInterpreterURIs();
	public URI getEventingServiceURI();
	public URI getMessageBoxURI();
	
	public void addGFacURI(URI uri);
	public void addWorkflowInterpreterURI(URI uri);
	public void setEventingURI(URI uri);
	public void setMessageBoxURI(URI uri);

	public void addGFacURI(URI uri, Date expire);
	public void addWorkflowInterpreterURI(URI uri, Date expire);
	public void setEventingURI(URI uri, Date expire);
	public void setMessageBoxURI(URI uri, Date expire);
	
	public void removeGFacURI(URI uri);
	public void removeAllGFacURI();
	public void removeWorkflowInterpreterURI(URI uri);
	public void removeAllWorkflowInterpreterURI();
	public void unsetEventingURI();
	public void unsetMessageBoxURI();
}

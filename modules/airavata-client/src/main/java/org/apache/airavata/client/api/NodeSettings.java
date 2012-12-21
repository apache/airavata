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

package org.apache.airavata.client.api;

public interface NodeSettings {
	
	/**
	 * Get the id of the node in the workflow
	 * @return
	 */
	public String getNodeId();
	
	/**
	 * Get the id of the service descriptor which the node binds to
	 * @return
	 */
	public String getServiceId();
	
	/**
	 * host settings for the service descriptor 
	 * @return
	 */
	public HostSchedulingSettings getHostSettings();
	
	/**
	 * HPC settings for the grid application exposed by the service descriptor
	 * @return
	 */
	public HPCSchedulingSettings getHPCSettings();
	
	/**
	 * Set the node Id of the workflow
	 * @param nodeId
	 */
	public void setNodeId(String nodeId);
	
	/**
	 * Set the id of the service descriptor which the node should binds to 
	 * @param serviceId
	 */
	public void setServiceId(String serviceId);
}

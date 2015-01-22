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

public interface WorkflowSchedulingSettings {
	
	/**
	 * Get all currently defined scheduling settings for the workflow node
	 * @return
	 */
	public NodeSettings[] getNodeSettingsList();
	
	/**
	 * Add and return a new Node scheduling settings  
	 * @param nodeId
	 * @return
	 */
	public NodeSettings addNewNodeSettings(String nodeId);
	
	/**
	 * Add and return a new Node scheduling settings
	 * @param nodeId
	 * @param serviceId
	 * @param cpuCount
	 * @param nodeCount
	 * @return
	 */
	public NodeSettings addNewNodeSettings(String nodeId, String serviceId, int cpuCount, int nodeCount);
	
	/**
	 * Add a list of Node scheduling settings
	 * @param nodeSettings
	 */
	public void addNewNodeSettings(NodeSettings...nodeSettings);
	
	/**
	 * Is there a Node scheduling settings defined for the given node id
	 * @param nodeId
	 * @return
	 */
	public boolean hasNodeSettings(String nodeId);
	
	/**
	 * Return the Node scheduling settings for the given node Id
	 * @param nodeId
	 * @return
	 */
	public NodeSettings getNodeSettings(String nodeId);
	
	/**
	 * Remove the node scheduling settings given by the node Id
	 * @param nodeId
	 */
	public void removeNodeSettings(String nodeId);
	
	/**
	 * Clear all node scheduling settings
	 */
	public void removeAllNodeSettings();
}

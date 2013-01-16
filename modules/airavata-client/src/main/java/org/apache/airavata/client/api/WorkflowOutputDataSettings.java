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

public interface WorkflowOutputDataSettings {
	
	/**
	 * Retrive the list of application output data settings 
	 * @return
	 */
	public OutputDataSettings[] getOutputDataSettingsList();
	
	/**
	 * Add a new appliation output data settings
	 * @param nodeId - id of the node which the output data settings will be specified
	 * @return
	 */
	public OutputDataSettings addNewOutputDataSettings(String nodeId);
	
	/**
	 * Add a new appliation output data settings
	 * @param outputDataDirectory
	 * @param dataRegistryURL
	 * @param isDataPersistent
	 * @return
	 */
	public OutputDataSettings addNewOutputDataSettings(String outputDataDirectory, String dataRegistryURL, boolean isDataPersistent);
	
	/**
	 * Add new application putput data settings
	 * @param outputDataSettings
	 */
	public void addNewOutputDataSettings(OutputDataSettings...outputDataSettings);
	
	/**
	 * Remove the application output data settings from the workflow output data settings
	 * @param outputDataSettings
	 */
	public void removeOutputDataSettings(OutputDataSettings outputDataSettings);
	
	/**
	 * Remove all application output data settings
	 */
	public void removeAllOutputDataSettings();

}

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

public interface OutputDataSettings {
	
	/**
	 * Retrieve the id of the node which these output data settings are relevant 
	 * @return
	 */
	public String getNodeId();
	
	/**
	 * Retrieve the pre-specified staging location for output data
	 * @return
	 */
	public String getOutputDataDirectory();
	
	/**
	 * Retrieve the associated data registry location for cataloging data separately
	 * @return
	 */
	public String getDataRegistryUrl();
	
	/**
	 * Should the intermediate workflow output data should be kept or discarded
	 * @return
	 */
	public Boolean isDataPersistent();
	
	/**
	 * Setup the id of the node which these output data settings are relevant
	 */
	public void setNodeId(String nodeId);
	
	/**
	 * Setup a custom pre-specified staging location for output data 
	 * @param outputDataDirectory - Path for output data directory
	 */
	public void setOutputDataDirectory(String outputDataDirectory);
	
	/**
	 * Setup a URL for pre-specified data registry (instead of the default) which will 
	 * catalog data  
	 * @param dataRegistryUrl
	 */
	public void setDataRegistryUrl(String dataRegistryUrl);
	
	/**
	 * Retain or discard intermediate output data 
	 * @param isDataPersistance - if true (default value) the intermediate output data is 
	 * kept, else discarded.
	 */
	public void setDataPersistent(Boolean isDataPersistance);
	
	/**
	 * Default output data directory will be used.
	 */
	public void resetOutputDataDirectory();
	
	/**
	 * Default data registry location will be used.
	 */
	public void resetDataRegistryUrl();
	
	/**
	 * Default data persistent settings will be used.
	 */
	public void resetDataPersistent();

}

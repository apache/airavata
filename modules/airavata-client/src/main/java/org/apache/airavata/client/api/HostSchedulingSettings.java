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

public interface HostSchedulingSettings {
	
	/**
	 * Get the id of the host descriptor
	 * @return
	 */
	public String getHostId();
	
	/**
	 * Using WS-Gram or not (Pre WS-GRAM) 
	 * @return
	 */
	public Boolean isWSGRAMPreffered();
	
	/**
	 * Get the gatekeeper endpoint reference
	 * @return
	 */
	public String getGatekeeperEPR();
	
	/**
	 * Set the id of the host descriptor use
	 * @param hostId
	 */
	public void setHostId(String hostId);
	
	/**
	 * Set whether to use WS-GRAM or Pre WS-GRAM
	 * @param wsgramPreffered
	 */
	public void setWSGramPreffered(Boolean wsgramPreffered);
	
	/**
	 * Set the gatekeeper endpoint reference
	 * @param gatekeeperEPR
	 */
	public void setGatekeeperEPR(String gatekeeperEPR);
	
	/**
	 * reset the values for the WS-GRAM preference
	 */
	public void resetWSGramPreffered();
	
	/**
	 * Reset the values for gatekeeper endpoint reference
	 */
	public void resetGatekeeperEPR();
}

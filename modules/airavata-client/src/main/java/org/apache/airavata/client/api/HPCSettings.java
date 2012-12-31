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

public interface HPCSettings {
	
	/**
	 * Get the job manager to use for this job submission
	 * @return
	 */
	public String getJobManager();
	
	/**
	 * Get the no of CPU cores to allocate for this job 
	 * @return
	 */
	public Integer getCPUCount();
	
	/**
	 * Get the no of nodes to allocate for this job
	 * @return
	 */
	public Integer getNodeCount();
	
	/**
	 * Get the job queue name 
	 * @return
	 */
	public String getQueueName();
	
	/**
	 * Get the maximum time the job should be allocated for execution 
	 * @return
	 */
	public Integer getMaxWallTime();
	
	/**
	 * Set the job manager to use for this job submission
	 * @param jobManager
	 */
	public void setJobManager(String jobManager);
	
	/**
	 * Set the no of CPU cores to allocate for this job
	 * @param cpuCount
	 */
	public void setCPUCount(Integer cpuCount);
	
	/**
	 * Set the no of nodes to allocate for this job
	 * @param nodeCount
	 */
	public void setNodeCount(Integer nodeCount);
	
	/**
	 * Set the job queue name 
	 * @param queueName
	 */
	public void setQueueName(String queueName);
	
	/**
	 * Set the maximum time the job should be allocated for execution 
	 * @param maxWallTime
	 */
	public void setMaxWallTime(Integer maxWallTime);
	
	/**
	 * Reset the job manager values
	 */
	public void resetJobManager();
	
	/**
	 * Reset the CPU count
	 */
	public void resetCPUCount();
	
	/**
	 * Reset the Node count
	 */
	public void resetNodeCount();
	
	/**
	 * Reset the queue name for the job
	 */
	public void resetQueueName();
	
	/**
	 * Reset the maximum time allocated for the job
	 */
	public void resetMaxWallTime();
	
}

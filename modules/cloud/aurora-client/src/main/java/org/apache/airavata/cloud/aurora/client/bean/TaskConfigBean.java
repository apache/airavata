/**
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
 */
package org.apache.airavata.cloud.aurora.client.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class TaskConfigBean.
 */
public class TaskConfigBean {
	
	/** The task name. */
	private String taskName;
	
	/** The resources. */
	private ResourceBean resources;
	
	/** The max failures. */
	private int maxFailures;
	
	/** The processes. */
	private Set<ProcessBean> processes;

	/**
	 * Instantiates a new task config bean.
	 *
	 * @param taskName the task name
	 * @param processes the processes
	 * @param resources the resources
	 */
	public TaskConfigBean(String taskName, Set<ProcessBean> processes, ResourceBean resources) {
		this.taskName = taskName;
		this.processes = processes;
		this.resources = resources;
		
		// set default value
		this.maxFailures = 1;
	}
	
	/**
	 * Gets the task name.
	 *
	 * @return the task name
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * Sets the task name.
	 *
	 * @param taskName the new task name
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}



	/**
	 * Gets the resources.
	 *
	 * @return the resources
	 */
	public ResourceBean getResources() {
		return resources;
	}

	/**
	 * Sets the resources.
	 *
	 * @param resources the new resources
	 */
	public void setResources(ResourceBean resources) {
		this.resources = resources;
	}

	/**
	 * Gets the max failures.
	 *
	 * @return the max failures
	 */
	public int getMaxFailures() {
		return maxFailures;
	}

	/**
	 * Sets the max failures.
	 *
	 * @param maxFailures the new max failures
	 */
	public void setMaxFailures(int maxFailures) {
		this.maxFailures = maxFailures;
	}

	/**
	 * Gets the processes.
	 *
	 * @return the processes
	 */
	public Set<ProcessBean> getProcesses() {
		if(processes == null) {
			processes = new HashSet<>();
		}
		
		return processes;
	}

	/**
	 * Sets the processes.
	 *
	 * @param processes the new processes
	 */
	public void setProcesses(Set<ProcessBean> processes) {
		this.processes = processes;
	}
	
}

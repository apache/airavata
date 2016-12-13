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

import java.util.List;

import org.apache.airavata.cloud.aurora.client.sdk.ScheduledTask;

/**
 * The Class JobDetailsResponseBean.
 */
public class JobDetailsResponseBean extends ResponseBean {

	/**
	 * Instantiates a new job details response bean.
	 *
	 * @param responseBean the response bean
	 */
	public JobDetailsResponseBean(ResponseBean responseBean) {
		this.setResponseCode(responseBean.getResponseCode());
		this.setServerInfo(responseBean.getServerInfo());
	}
	
	/** The tasks. */
	private List<ScheduledTask> tasks;

	/**
	 * Gets the tasks.
	 *
	 * @return the tasks
	 */
	public List<ScheduledTask> getTasks() {
		return tasks;
	}

	/**
	 * Sets the tasks.
	 *
	 * @param tasks the new tasks
	 */
	public void setTasks(List<ScheduledTask> tasks) {
		this.tasks = tasks;
	}

	/* (non-Javadoc)
	 * @see org.apache.airavata.cloud.aurora.client.bean.ResponseBean#toString()
	 */
	@Override
	public String toString() {
		return "JobDetailsResponseBean [tasks=" + tasks + "]";
	}
}

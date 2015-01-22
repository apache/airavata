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

package org.apache.airavata.client.impl;

import org.apache.airavata.client.api.HPCSettings;

public class HPCSettingsImpl implements HPCSettings {
	private String jobManager;
	private String queueName;
	private Integer cpuCount;
	private Integer nodeCount;
	private Integer maxWallTime;
	
	@Override
	public String getJobManager() {
		return jobManager;
	}

	@Override
	public Integer getCPUCount() {
		return cpuCount;
	}

	@Override
	public Integer getNodeCount() {
		return nodeCount;
	}

	@Override
	public String getQueueName() {
		return queueName;
	}

	@Override
	public Integer getMaxWallTime() {
		return maxWallTime;
	}

	@Override
	public void setJobManager(String jobManager) {
		this.jobManager=jobManager;
	}

	@Override
	public void setCPUCount(Integer cpuCount) {
		this.cpuCount=cpuCount;
	}

	@Override
	public void setNodeCount(Integer nodeCount) {
		this.nodeCount=nodeCount;
	}

	@Override
	public void setQueueName(String queueName) {
		this.queueName=queueName;
	}

	@Override
	public void setMaxWallTime(Integer maxWallTime) {
		this.maxWallTime=maxWallTime;
	}

	@Override
	public void resetJobManager() {
		this.jobManager=null;
	}

	@Override
	public void resetCPUCount() {
		this.cpuCount=null;
	}

	@Override
	public void resetNodeCount() {
		this.nodeCount=null;
	}

	@Override
	public void resetQueueName() {
		this.queueName=null;
	}

	@Override
	public void resetMaxWallTime() {
		this.maxWallTime=null;
	}

}

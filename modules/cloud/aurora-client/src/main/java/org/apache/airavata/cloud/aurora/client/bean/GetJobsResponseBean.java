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
 * The Class GetJobsResponseBean.
 */
public class GetJobsResponseBean extends ResponseBean {

	/** The job configs. */
	private Set<JobConfigBean> jobConfigs;
	
	/**
	 * Instantiates a new gets the jobs response bean.
	 *
	 * @param responseBean the response bean
	 */
	public GetJobsResponseBean(ResponseBean responseBean) {
		this.setResponseCode(responseBean.getResponseCode());
		this.setServerInfo(responseBean.getServerInfo());
	}

	/**
	 * Gets the job configs.
	 *
	 * @return the job configs
	 */
	public Set<JobConfigBean> getJobConfigs() {
		if(jobConfigs == null) {
			jobConfigs = new HashSet<>();
		}
		return jobConfigs;
	}

	/**
	 * Sets the job configs.
	 *
	 * @param jobConfigs the new job configs
	 */
	public void setJobConfigs(Set<JobConfigBean> jobConfigs) {
		this.jobConfigs = jobConfigs;
	}
}

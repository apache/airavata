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

import java.util.Set;

import org.apache.airavata.cloud.aurora.client.sdk.PendingReason;

/**
 * The Class PendingJobResponseBean.
 */
public class PendingJobReasonBean extends ResponseBean {

	/** The reasons. */
	private Set<PendingReason> reasons;
	
	/**
	 * Instantiates a new pending job response bean.
	 *
	 * @param responseBean the response bean
	 */
	public PendingJobReasonBean(ResponseBean responseBean) {
		this.setResponseCode(responseBean.getResponseCode());
		this.setServerInfo(responseBean.getServerInfo());
	}

	/**
	 * Gets the reasons.
	 *
	 * @return the reasons
	 */
	public Set<PendingReason> getReasons() {
		return reasons;
	}

	/**
	 * Sets the reasons.
	 *
	 * @param reasons the new reasons
	 */
	public void setReasons(Set<PendingReason> reasons) {
		this.reasons = reasons;
	}

	/* (non-Javadoc)
	 * @see org.apache.airavata.cloud.aurora.client.bean.ResponseBean#toString()
	 */
	@Override
	public String toString() {
		return "PendingJobResponseBean [reasons=" + reasons + "]";
	}
	
}

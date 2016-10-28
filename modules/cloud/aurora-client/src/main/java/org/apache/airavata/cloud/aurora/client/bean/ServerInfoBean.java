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

/**
 * The Class ServerInfoBean.
 */
public class ServerInfoBean {

	/** The cluster name. */
	private String clusterName;
	
	/** The stats url prefix. */
	private String statsUrlPrefix;

	/**
	 * Instantiates a new server info bean.
	 *
	 * @param clusterName the cluster name
	 * @param statsUrlPrefix the stats url prefix
	 */
	public ServerInfoBean(String clusterName, String statsUrlPrefix) {
		this.clusterName = clusterName;
		this.statsUrlPrefix = statsUrlPrefix;
	}
	
	/**
	 * Gets the cluster name.
	 *
	 * @return the cluster name
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * Sets the cluster name.
	 *
	 * @param clusterName the new cluster name
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	/**
	 * Gets the stats url prefix.
	 *
	 * @return the stats url prefix
	 */
	public String getStatsUrlPrefix() {
		return statsUrlPrefix;
	}

	/**
	 * Sets the stats url prefix.
	 *
	 * @param statsUrlPrefix the new stats url prefix
	 */
	public void setStatsUrlPrefix(String statsUrlPrefix) {
		this.statsUrlPrefix = statsUrlPrefix;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServerInfoBean [clusterName=" + clusterName + ", statsUrlPrefix=" + statsUrlPrefix + "]";
	}
}

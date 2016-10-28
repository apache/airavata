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

import org.apache.airavata.cloud.aurora.util.ResponseCodeEnum;

/**
 * The Class ResponseBean.
 */
public class ResponseBean {

	/** The response code. */
	private ResponseCodeEnum responseCode;
	
	/** The server info. */
	private ServerInfoBean serverInfo;

	/**
	 * Gets the response code.
	 *
	 * @return the response code
	 */
	public ResponseCodeEnum getResponseCode() {
		return responseCode;
	}

	/**
	 * Sets the response code.
	 *
	 * @param responseCode the new response code
	 */
	public void setResponseCode(ResponseCodeEnum responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Gets the server info.
	 *
	 * @return the server info
	 */
	public ServerInfoBean getServerInfo() {
		return serverInfo;
	}

	/**
	 * Sets the server info.
	 *
	 * @param serverInfo the new server info
	 */
	public void setServerInfo(ServerInfoBean serverInfo) {
		this.serverInfo = serverInfo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResponseBean [responseCode=" + responseCode + ", serverInfo=" + serverInfo + "]";
	}
	
}

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

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.client.api.HPCSettings;
import org.apache.airavata.client.api.HostSchedulingSettings;
import org.apache.airavata.client.api.NodeSettings;
import org.apache.airavata.client.tools.NameValuePairType;

public class NodeSettingsImpl implements NodeSettings {
	private String nodeId;
	private String serviceId;
	private HPCSettings hpcSettings;
	private HostSchedulingSettings hostSchedulingSettings;
	private List<NameValuePairType> nameValuePair;
	
	public NodeSettingsImpl(String nodeId) {
		this(nodeId,null);
	}

	public NodeSettingsImpl(String nodeId, String serviceId) {
		setNodeId(nodeId);
		setServiceId(serviceId);
	}
	
	@Override
	public String getNodeId() {
		return nodeId;
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public HostSchedulingSettings getHostSettings() {
		if (hostSchedulingSettings==null){
			hostSchedulingSettings=new HostSchedulingSettingsImpl();
		}
		return hostSchedulingSettings;
	}

	@Override
	public HPCSettings getHPCSettings() {
		if (hpcSettings==null){
			hpcSettings=new HPCSettingsImpl();
		}
		return hpcSettings;
	}

	@Override
	public void setNodeId(String nodeId) {
		this.nodeId=nodeId;
	}

	@Override
	public void setServiceId(String serviceId) {
		this.serviceId=serviceId;
	}

	@Override
	public void setHostSettings(HostSchedulingSettings hostSchedulingSettings) {
		this.hostSchedulingSettings = hostSchedulingSettings;
	}

	@Override
	public void setHPCSettings(HPCSettings hpcSettings) {
		this.hpcSettings = hpcSettings;
	}

	@Override
	public List<NameValuePairType> getNameValuePair() {
		if (nameValuePair==null){
			nameValuePair=new ArrayList<NameValuePairType>();
		}
		return nameValuePair;
	}

	@Override
	public void setNameValuePair(List<NameValuePairType> nameValuePair) {
		this.nameValuePair = nameValuePair;
		
	}

}

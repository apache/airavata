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
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.client.api.NodeSettings;
import org.apache.airavata.client.api.WorkflowSchedulingSettings;

public class WorkflowSchedulingSettingsImpl implements
		WorkflowSchedulingSettings {

	private List<NodeSettings> nodeSettingsList;
	
	private List<NodeSettings> getNodeSettingsList(){
		if (nodeSettingsList==null){
			nodeSettingsList=new ArrayList<NodeSettings>();
		}
		return nodeSettingsList;
	}
	
	@Override
	public NodeSettings[] getNodeSettings() {
		return getNodeSettingsList().toArray(new NodeSettings[]{});
	}

	@Override
	public NodeSettings addNewNodeSettings(String nodeId) {
		getNodeSettingsList().add(new NodeSettingsImpl(nodeId));
		return getNodeSettingsList().get(getNodeSettingsList().size()-1);
	}

	@Override
	public NodeSettings addNewNodeSettings(String nodeId, String serviceId,
			int cpuCount, int nodeCount) {
		NodeSettingsImpl nodeSettings = new NodeSettingsImpl(nodeId, serviceId);
		nodeSettings.getHPCSettings().setCPUCount(cpuCount);
		nodeSettings.getHPCSettings().setNodeCount(nodeCount);
		return nodeSettings;
	}

	@Override
	public void addNewNodeSettings(NodeSettings... newNodeSettingsList) {
		getNodeSettingsList().addAll(Arrays.asList(newNodeSettingsList));
	}

	@Override
	public boolean hasNodeSettings(String nodeId) {
		return getNodeSettings(nodeId)!=null;
	}

	@Override
	public NodeSettings getNodeSettings(String nodeId) {
		for(NodeSettings nodeSettings:getNodeSettingsList()){
			if (nodeSettings.getNodeId().equals(nodeId)){
				return nodeSettings;
			}
		}
		return null;
	}

	@Override
	public void removeNodeSettings(String nodeId) {
		if (hasNodeSettings(nodeId)){
			getNodeSettingsList().remove(getNodeSettings(nodeId));
		}

	}

	@Override
	public void removeAllNodeSettings() {
		getNodeSettingsList().clear();
	}

}

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
package org.apache.airavata.xbaya.ui.graph.system;

import java.util.List;

import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.system.DifferedInputNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.XBayaGUI;

public class DifferedInputHandler {
	
	
	public static void handleDifferredInputsofDependentNodes(Node node, final XBayaGUI xbayaGUI){
		List<DataPort> inputPorts = node.getInputPorts();
		for (DataPort dataPort : inputPorts) {
			Node fromNode = dataPort.getFromNode();
			if(isDifferedInputNode(fromNode)){
				final DifferedInputNode differedInputNode = (DifferedInputNode)fromNode;
				if(!differedInputNode.isConfigured()){
					//not configured differed node this is what we are looking for
					//set the flag and ensure all the rest is finished
					Runnable task = new Runnable() {
						
						@Override
						public void run() {
							((DifferedInputNodeGUI)NodeController.getGUI(differedInputNode)).showConfigurationDialog(xbayaGUI);
						}
					};
					new Thread(task).start();
					
					
					
				}
			}
		}
	}
	
	
	
	public static boolean onlyWaitingOnIncompleteDifferedInputNode(Node node){
		List<DataPort> inputPorts = node.getInputPorts();
		boolean atleadOneDifferedInputNodeIsIncomplete = false; 
		for (DataPort dataPort : inputPorts) {
			Node fromNode = dataPort.getFromNode();
			if(NodeController.isFinished(fromNode)){
				//no op
			}else if(isDifferedInputNode(fromNode)){
				//not finished
				if(!((DifferedInputNode)node).isConfigured()){
					//not configured differed node this is what we are looking for
					//set the flag and ensure all the rest is finished
					atleadOneDifferedInputNodeIsIncomplete = true;
				}
			}else{
				//there is a not finished non differed input node
				return false;
			}
		}
		//if not finished nodes were found we wil not be here so
		return atleadOneDifferedInputNodeIsIncomplete;
	}
	public static boolean isDifferedInputNode(Node node){
		return node instanceof DifferedInputNode;
	}

}
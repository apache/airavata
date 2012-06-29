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


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2012 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */

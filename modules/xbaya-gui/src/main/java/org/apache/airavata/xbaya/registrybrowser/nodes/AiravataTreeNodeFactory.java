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

package org.apache.airavata.xbaya.registrybrowser.nodes;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.registrybrowser.model.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class AiravataTreeNodeFactory {
	public static TreeNode getTreeNode(Object o,TreeNode parent){
        if (o instanceof XBayaEngine){
            return new RegistryNode((XBayaEngine)o,parent);
        }/*else if (o instanceof Registry){
//			return new RegistryNode((Registry)o,parent);
		}*/else if (o instanceof GFacURLs){
			return new GFacURLsNode((GFacURLs)o,parent);
		}else if (o instanceof GFacURL){
			return new GFacURLNode((GFacURL)o,parent);
		}else if (o instanceof HostDescriptions){
			return new HostDescriptionsNode((HostDescriptions)o,parent);
		}else if (o instanceof HostDescription){
			return new HostDescriptionNode((HostDescription)o,parent);
		}else if (o instanceof ServiceDescriptions){
			return new ServiceDescriptionsNode((ServiceDescriptions)o,parent);
		}else if (o instanceof ServiceDescription){
			return new ServiceDescriptionNode((ServiceDescription)o,parent);
		}else if (o instanceof ApplicationDeploymentDescriptions){
			return new ApplicationDeploymentDescriptionsNode((ApplicationDeploymentDescriptions)o,parent);
		}else if (o instanceof ApplicationDeploymentDescriptionWrap){
			return new ApplicationDeploymentDescriptionNode((ApplicationDeploymentDescriptionWrap)o,parent);
		}else if (o instanceof XBayaWorkflowTemplates){
			return new XBayaWorkflowTemplatesNode((XBayaWorkflowTemplates)o,parent);
		}else if (o instanceof XBayaWorkflowTemplate){
			return new XBayaWorkflowTemplateNode((XBayaWorkflowTemplate)o,parent);
		}else if (o instanceof ServiceParameter){
			return new ParameterNode((ServiceParameter)o,parent);
		}else if (o instanceof InputParameters){
			return new InputParametersNode((InputParameters)o,parent);
		}else if (o instanceof OutputParameters){
			return new OutputParametersNode((OutputParameters)o,parent);
		}else if (o instanceof ServiceParameters){
			return new ParametersNode((ServiceParameters)o,parent);
		}else if (o instanceof XBayaWorkflowExperiments){
			return new XBayaWorkflowExperimentsNode((XBayaWorkflowExperiments)o,parent);
		}else if (o instanceof XBayaWorkflowExperiment){
			return new XBayaWorkflowExperimentNode((XBayaWorkflowExperiment)o,parent);
		}else if (o instanceof XBayaWorkflow){
			return new XBayaWorkflowNode((XBayaWorkflow)o,parent);
		}else if (o instanceof XBayaWorkflowService){
			return new XBayaWorkflowServiceNode((XBayaWorkflowService)o,parent);
		}else{
			return new DefaultMutableTreeNode(o);
		}
	}
}

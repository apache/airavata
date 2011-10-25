package org.apache.airavata.xbaya.registrybrowser.nodes;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptionWrap;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptions;
import org.apache.airavata.xbaya.registrybrowser.model.GFacURL;
import org.apache.airavata.xbaya.registrybrowser.model.GFacURLs;
import org.apache.airavata.xbaya.registrybrowser.model.HostDescriptions;
import org.apache.airavata.xbaya.registrybrowser.model.InputParameters;
import org.apache.airavata.xbaya.registrybrowser.model.OutputParameters;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceDescriptions;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceParameter;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceParameters;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflow;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowExperiment;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowExperiments;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowService;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowTemplate;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowTemplates;

public class AiravataTreeNodeFactory {
	public static TreeNode getTreeNode(Object o,TreeNode parent){
		if (o instanceof Registry){
			return new RegistryNode((Registry)o,parent);
		}else if (o instanceof GFacURLs){
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

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
import org.apache.airavata.xbaya.registrybrowser.model.ServiceDescriptions;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflow;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflows;

public class AiravataTreeNodeFactory {
    public static TreeNode getTreeNode(Object o, TreeNode parent) {
        if (o instanceof Registry) {
            return new RegistryNode((Registry) o, parent);
        } else if (o instanceof GFacURLs) {
            return new GFacURLsNode((GFacURLs) o, parent);
        } else if (o instanceof GFacURL) {
            return new GFacURLNode((GFacURL) o, parent);
        } else if (o instanceof HostDescriptions) {
            return new HostDescriptionsNode((HostDescriptions) o, parent);
        } else if (o instanceof HostDescription) {
            return new HostDescriptionNode((HostDescription) o, parent);
        } else if (o instanceof ServiceDescriptions) {
            return new ServiceDescriptionsNode((ServiceDescriptions) o, parent);
        } else if (o instanceof ServiceDescription) {
            return new ServiceDescriptionNode((ServiceDescription) o, parent);
        } else if (o instanceof ApplicationDeploymentDescriptions) {
            return new ApplicationDeploymentDescriptionsNode((ApplicationDeploymentDescriptions) o, parent);
        } else if (o instanceof ApplicationDeploymentDescriptionWrap) {
            return new ApplicationDeploymentDescriptionNode((ApplicationDeploymentDescriptionWrap) o, parent);
        } else if (o instanceof XBayaWorkflows) {
            return new XBayaWorkflowsNode((XBayaWorkflows) o, parent);
        } else if (o instanceof XBayaWorkflow) {
            return new XBayaWorkflowNode((XBayaWorkflow) o, parent);
        } else {
            return new DefaultMutableTreeNode(o);
        }
    }
}

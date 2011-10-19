package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptions;
import org.apache.airavata.xbaya.registrybrowser.model.GFacURLs;
import org.apache.airavata.xbaya.registrybrowser.model.HostDescriptions;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceDescriptions;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflows;

public class RegistryNode  extends AbstractAiravataTreeNode{
	private Registry registry;
	
	public RegistryNode(Registry registry, TreeNode parent) {
		super(parent);
		setRegistry(registry);
	}

	protected List<TreeNode> getChildren() {
		List<Object> children = new ArrayList<Object>();
		GFacURLs gFacURLs = new GFacURLs(getRegistry());
		children.add(gFacURLs);
		HostDescriptions hostDescriptions = new HostDescriptions(getRegistry());
		children.add(hostDescriptions);
		ServiceDescriptions serviceDescriptions = new ServiceDescriptions(getRegistry());
		children.add(serviceDescriptions);
		ApplicationDeploymentDescriptions applicationDeploymentDescriptions = new ApplicationDeploymentDescriptions(getRegistry());
		children.add(applicationDeploymentDescriptions);
		XBayaWorkflows xBayaWorkflows = new XBayaWorkflows(getRegistry());
		children.add(xBayaWorkflows);
		return getTreeNodeList(children.toArray(), this);
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return getRegistry().getName()+" - "+getRegistry().getUsername()+"@"+getRegistry().getRepositoryURI().toString();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("registry.png");
	}
	
//	@Override
//	public void refresh() {
//		List<TreeNode> children = getChildren();
//		for (TreeNode node : children) {
//			if (node instanceof AbstractAiravataTreeNode){
//				((AbstractAiravataTreeNode)node).refresh();
//			}
//		}
//	}

}

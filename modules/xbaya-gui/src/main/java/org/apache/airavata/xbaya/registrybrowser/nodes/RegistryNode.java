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

public class RegistryNode  extends AbstractAiravataTreeNode{
	private Registry registry;
	
	public RegistryNode(Registry registry, TreeNode parent) {
		super(parent);
		setRegistry(registry);
	}

	protected List<TreeNode> getChildren() {
		List<Object> children = new ArrayList<Object>();
		children.add(new GFacURLs(getRegistry()));
		children.add(new HostDescriptions(getRegistry()));
		children.add(new ServiceDescriptions(getRegistry()));
		children.add(new ApplicationDeploymentDescriptions(getRegistry()));
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
		return getRegistry().getUsername()+"@"+getRegistry().getRepositoryURI().toString();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("registry.png");
	}

}

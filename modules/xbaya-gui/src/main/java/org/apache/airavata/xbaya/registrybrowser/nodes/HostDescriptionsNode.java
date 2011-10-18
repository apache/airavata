package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.xbaya.registrybrowser.model.HostDescriptions;

public class HostDescriptionsNode extends AbstractAiravataTreeNode {
	private HostDescriptions hostDescriptions;
	
	public HostDescriptionsNode(HostDescriptions hostDescriptions,TreeNode parent) {
		super(parent);
		setHostDescriptions(hostDescriptions);
	}

	@Override
	protected List<TreeNode> getChildren() {
		try {
			return getTreeNodeList(getHostDescriptions().getDescriptions().toArray(), this);
		} catch (HostDescriptionRetrieveException e) {
			e.printStackTrace();
			return emptyList();
		}
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Hosts";
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("cloud.png");
	}

	public HostDescriptions getHostDescriptions() {
		return hostDescriptions;
	}

	public void setHostDescriptions(HostDescriptions hostDescriptions) {
		this.hostDescriptions = hostDescriptions;
	}

}

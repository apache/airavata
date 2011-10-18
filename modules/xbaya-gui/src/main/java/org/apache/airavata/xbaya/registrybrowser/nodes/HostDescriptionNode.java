package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;

public class HostDescriptionNode extends AbstractAiravataTreeNode {
	private HostDescription hostDescription;
	
	public HostDescriptionNode(HostDescription hostDescription, TreeNode parent) {
		super(parent);
		setHostDescription(hostDescription);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return getHostDescription().getId();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("host.png");
	}

	public HostDescription getHostDescription() {
		return hostDescription;
	}

	public void setHostDescription(HostDescription hostDescription) {
		this.hostDescription = hostDescription;
	}

}

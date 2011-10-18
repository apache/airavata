package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptionWrap;

public class ApplicationDeploymentDescriptionNode extends AbstractAiravataTreeNode {
	private ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap;
	public ApplicationDeploymentDescriptionNode(ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap,TreeNode parent) {
		super(parent);
		setApplicationDeploymentDescriptionWrap(applicationDeploymentDescriptionWrap);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return getApplicationDeploymentDescriptionWrap().getDescription().getId();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("application.png");
	}

	public ApplicationDeploymentDescriptionWrap getApplicationDeploymentDescriptionWrap() {
		return applicationDeploymentDescriptionWrap;
	}

	public void setApplicationDeploymentDescriptionWrap(
			ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap) {
		this.applicationDeploymentDescriptionWrap = applicationDeploymentDescriptionWrap;
	}

}

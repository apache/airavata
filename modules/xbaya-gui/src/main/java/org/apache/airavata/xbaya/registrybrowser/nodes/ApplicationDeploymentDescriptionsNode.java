package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.exception.DeploymentDescriptionRetrieveException;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptions;

public class ApplicationDeploymentDescriptionsNode extends AbstractAiravataTreeNode {
	private ApplicationDeploymentDescriptions applicationDeploymentDescriptions;
	public ApplicationDeploymentDescriptionsNode(ApplicationDeploymentDescriptions applicationDeploymentDescriptions,TreeNode parent) {
		super(parent);
		setApplicationDeploymentDescriptions(applicationDeploymentDescriptions);
	}

	@Override
	protected List<TreeNode> getChildren() {
		try {
			return getTreeNodeList(getApplicationDeploymentDescriptions().getDescriptions().toArray(), this);
		} catch (DeploymentDescriptionRetrieveException e) {
			e.printStackTrace();
			return emptyList();
		}
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Applications";
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("applications.png");
	}

	public ApplicationDeploymentDescriptions getApplicationDeploymentDescriptions() {
		return applicationDeploymentDescriptions;
	}

	public void setApplicationDeploymentDescriptions(
			ApplicationDeploymentDescriptions applicationDeploymentDescriptions) {
		this.applicationDeploymentDescriptions = applicationDeploymentDescriptions;
	}


}

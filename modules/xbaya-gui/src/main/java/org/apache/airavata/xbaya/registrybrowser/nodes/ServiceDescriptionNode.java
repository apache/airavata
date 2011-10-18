package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

public class ServiceDescriptionNode extends AbstractAiravataTreeNode {
	private ServiceDescription serviceDescription;
	public ServiceDescriptionNode(ServiceDescription serviceDescription,TreeNode parent) {
		super(parent);
		setServiceDescription(serviceDescription);
	}

	@Override
	protected List<TreeNode> getChildren() {
		//TODO perhaps we should show the parameters as children
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return getServiceDescription().getId();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("service.png");
	}

	public ServiceDescription getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(ServiceDescription serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

}

package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceDescriptions;

public class ServiceDescriptionsNode extends AbstractAiravataTreeNode {
	private ServiceDescriptions serviceDescriptions;
	public ServiceDescriptionsNode(ServiceDescriptions serviceDescriptions,TreeNode parent) {
		super(parent);
		setServiceDescriptions(serviceDescriptions);
	}

	@Override
	protected List<TreeNode> getChildren() {
		try {
			return getTreeNodeList(getServiceDescriptions().getDescriptions().toArray(), this);
		} catch (ServiceDescriptionRetrieveException e) {
			e.printStackTrace();
			return emptyList();
		}
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Services";
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("services.png");
	}

	public ServiceDescriptions getServiceDescriptions() {
		return serviceDescriptions;
	}

	public void setServiceDescriptions(ServiceDescriptions serviceDescriptions) {
		this.serviceDescriptions = serviceDescriptions;
	}

}

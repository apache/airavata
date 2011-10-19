package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflows;

public class XBayaWorkflowsNode extends AbstractAiravataTreeNode {
	private XBayaWorkflows xbayaWorkflows;
	public XBayaWorkflowsNode(XBayaWorkflows xbayaWorkflows,TreeNode parent) {
		super(parent);
		setXbayaWorkflows(xbayaWorkflows);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return getTreeNodeList(getXbayaWorkflows().getWorkflows().toArray(), this);
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Workflows";
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("workflows.png");
	}

	public XBayaWorkflows getXbayaWorkflows() {
		return xbayaWorkflows;
	}

	public void setXbayaWorkflows(XBayaWorkflows xbayaWorkflows) {
		this.xbayaWorkflows = xbayaWorkflows;
	}

}

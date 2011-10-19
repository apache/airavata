package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflow;

public class XBayaWorkflowNode extends AbstractAiravataTreeNode {
	private XBayaWorkflow xbayaWorkflow;
	
	public XBayaWorkflowNode(XBayaWorkflow xbayaWorkflow,TreeNode parent) {
		super(parent);
		setXbayaWorkflow(xbayaWorkflow);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return getXbayaWorkflow().getWorkflowName();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("workflow.png");
	}

	public XBayaWorkflow getXbayaWorkflow() {
		return xbayaWorkflow;
	}

	public void setXbayaWorkflow(XBayaWorkflow xbayaWorkflow) {
		this.xbayaWorkflow = xbayaWorkflow;
	}

}

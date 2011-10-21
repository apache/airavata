package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.AddAction;
import org.apache.airavata.xbaya.registrybrowser.menu.RefreshAction;
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

	@Override
	public List<String> getSupportedActions() {
		return Arrays.asList(AddAction.ID, RefreshAction.ID);
	}
	
	public boolean triggerAction(JTree tree,String action) throws Exception{
		if (action.equals(AddAction.ID)){
			JOptionPane.showMessageDialog(null, "TODO");
			//TODO
			return true;
		} 
		return super.triggerAction(tree, action);
	}

	@Override
	public String getActionCaption(AbstractBrowserActionItem action) {
		if (action.getID().equals(AddAction.ID)){
			return "New workflow...";
		}
		return action.getDefaultCaption();
	}

	@Override
	public Icon getActionIcon(AbstractBrowserActionItem action) {
		return null;
	}

	@Override
	public String getActionDescription(AbstractBrowserActionItem action) {
		return null;
	}
}

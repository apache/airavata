package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceParameters;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowService;

public class XBayaWorkflowServiceNode extends AbstractAiravataTreeNode {
    private XBayaWorkflowService xbayaWorkflowService;

    public XBayaWorkflowServiceNode(XBayaWorkflowService xbayaWorkflowService, TreeNode parent) {
        super(parent);
        setXbayaWorkflowService(xbayaWorkflowService);
    }

    @Override
    protected List<TreeNode> getChildren() {
		List<ServiceParameters> parameterTypeList=new ArrayList<ServiceParameters>();
		if (getXbayaWorkflowService().getInputParameters()!=null && getXbayaWorkflowService().getInputParameters().getParameters().size()>0){
			parameterTypeList.add(getXbayaWorkflowService().getInputParameters());
		}
		if (getXbayaWorkflowService().getOutputParameters()!=null && getXbayaWorkflowService().getOutputParameters().getParameters().size()>0){
			parameterTypeList.add(getXbayaWorkflowService().getOutputParameters());
		}
		return getTreeNodeList(parameterTypeList.toArray(), this);
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getXbayaWorkflowService().getServiceNodeId();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("workflow.png");
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList();
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        return super.triggerAction(tree, action);
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
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

	public XBayaWorkflowService getXbayaWorkflowService() {
		return xbayaWorkflowService;
	}

	public void setXbayaWorkflowService(XBayaWorkflowService xbayaWorkflowService) {
		this.xbayaWorkflowService = xbayaWorkflowService;
	}
}

package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflow;

public class XBayaWorkflowNode extends AbstractAiravataTreeNode {
    private XBayaWorkflow xbayaWorkflow;

    public XBayaWorkflowNode(XBayaWorkflow xbayaWorkflow, TreeNode parent) {
        super(parent);
        setXbayaWorkflow(xbayaWorkflow);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return getTreeNodeList(getXbayaWorkflow().getWorkflowServices().toArray(),this);
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getXbayaWorkflow().getWorkflowName()+" : "+getXbayaWorkflow().getWorkflowId();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("workflow.png");
    }

    public XBayaWorkflow getXbayaWorkflow() {
        return xbayaWorkflow;
    }

    public void setXbayaWorkflow(XBayaWorkflow xbayaWorkflow) {
        this.xbayaWorkflow = xbayaWorkflow;
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
}

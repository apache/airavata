package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.RefreshAction;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowExperiments;

public class XBayaWorkflowExperimentsNode extends AbstractAiravataTreeNode {
	private XBayaWorkflowExperiments experiments;
	
    public XBayaWorkflowExperimentsNode(XBayaWorkflowExperiments experiments, TreeNode parent) {
        super(parent);
        setExperiments(experiments);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return getTreeNodeList(getExperiments().getAllExperiments().toArray(), this);
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return "Experiments";
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("workflows.png");
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(RefreshAction.ID);
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

	public XBayaWorkflowExperiments getExperiments() {
		return experiments;
	}

	public void setExperiments(XBayaWorkflowExperiments experiments) {
		this.experiments = experiments;
	}
}

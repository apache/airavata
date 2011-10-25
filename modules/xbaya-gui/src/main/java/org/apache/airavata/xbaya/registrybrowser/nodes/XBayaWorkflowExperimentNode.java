package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowExperiment;

public class XBayaWorkflowExperimentNode extends AbstractAiravataTreeNode {
	private XBayaWorkflowExperiment experiment;
	
    public XBayaWorkflowExperimentNode(XBayaWorkflowExperiment experiment, TreeNode parent) {
        super(parent);
        setExperiment(experiment);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return getTreeNodeList(getExperiment().getWorkflows().toArray(), this);
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getExperiment().getExperimentId();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("workflows.png");
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

	public XBayaWorkflowExperiment getExperiment() {
		return experiment;
	}

	public void setExperiment(XBayaWorkflowExperiment experiment) {
		this.experiment = experiment;
	}
}

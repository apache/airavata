package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceParameters;

public class ParametersNode extends AbstractAiravataTreeNode {
	private ServiceParameters parametersList;
	public ParametersNode(ServiceParameters parameters,TreeNode parent) {
		super(parent);
		setParametersList(parameters);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return getTreeNodeList(getParametersList().getParameters(), this);
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Parameters";
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("parameter.png");
	}

	@Override
	public List<String> getSupportedActions() {
		return Arrays.asList();
	}
	
	public boolean triggerAction(JTree tree,String action) throws Exception{
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

	public ServiceParameters getParametersList() {
		return parametersList;
	}

	public void setParametersList(ServiceParameters parametersList) {
		this.parametersList = parametersList;
	}

}

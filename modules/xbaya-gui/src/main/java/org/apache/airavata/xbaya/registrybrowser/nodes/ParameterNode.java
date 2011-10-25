package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceParameter;

public class ParameterNode extends AbstractAiravataTreeNode {
	private ServiceParameter parameter;
	
	public ParameterNode(ServiceParameter parameter, TreeNode parent) {
		super(parent);
		setParameter(parameter);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		if (getParameter().getValue()!=null){
			return getParameter().getName()+":"+getParameter().getValue().toString();
		}else{
			return getParameter().getName()+":<"+getParameter().getType().getType().toString()+">";
		}
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

	public ServiceParameter getParameter() {
		return parameter;
	}

	public void setParameter(ServiceParameter parameter) {
		this.parameter = parameter;
	}
}

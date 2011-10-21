package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.exception.DeploymentDescriptionRetrieveException;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.DeleteAction;
import org.apache.airavata.xbaya.registrybrowser.menu.EditAction;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptionWrap;

public class ApplicationDeploymentDescriptionNode extends AbstractAiravataTreeNode {
	private ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap;
	public ApplicationDeploymentDescriptionNode(ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap,TreeNode parent) {
		super(parent);
		setApplicationDeploymentDescriptionWrap(applicationDeploymentDescriptionWrap);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return getApplicationDeploymentDescriptionWrap().getDescription().getId();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("application.png");
	}

	public ApplicationDeploymentDescriptionWrap getApplicationDeploymentDescriptionWrap() {
		return applicationDeploymentDescriptionWrap;
	}

	public void setApplicationDeploymentDescriptionWrap(
			ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap) {
		this.applicationDeploymentDescriptionWrap = applicationDeploymentDescriptionWrap;
	}

	@Override
	public List<String> getSupportedActions() {
		return Arrays.asList(EditAction.ID, DeleteAction.ID);
	}

	public boolean triggerAction(JTree tree,String action) throws Exception{
		if (action.equals(DeleteAction.ID)){
			deleteApplicationDescription(tree);
			return true;
		}else if (action.equals(EditAction.ID)){
			JOptionPane.showMessageDialog(null, "TODO");
			return true;
		} 
		return super.triggerAction(tree, action);
	}

	private void deleteApplicationDescription(JTree tree)
			throws PathNotFoundException,
			DeploymentDescriptionRetrieveException {
		if (askQuestion("Application description", "Are you sure that you want to remove the application description \""+getApplicationDeploymentDescriptionWrap().getDescription().getId()+"\"?")) {
			getRegistry().deleteDeploymentDescription(
					getApplicationDeploymentDescriptionWrap().getService(),
					getApplicationDeploymentDescriptionWrap().getHost(),
					getApplicationDeploymentDescriptionWrap()
							.getDescription().getId());
			((AbstractAiravataTreeNode)getParent()).refresh();
			reloadTreeNode(tree, getParent());
		}
	}

	@Override
	public String getActionCaption(AbstractBrowserActionItem action) {
		if (action.getID().equals(DeleteAction.ID)){
			return "Remove";
		}else if (action.getID().equals(EditAction.ID)){
			return "Edit";
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

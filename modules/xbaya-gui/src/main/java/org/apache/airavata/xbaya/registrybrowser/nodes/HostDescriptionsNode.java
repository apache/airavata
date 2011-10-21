package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.xbaya.appwrapper.HostDescriptionDialog;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.AddAction;
import org.apache.airavata.xbaya.registrybrowser.menu.DeleteAction;
import org.apache.airavata.xbaya.registrybrowser.menu.RefreshAction;
import org.apache.airavata.xbaya.registrybrowser.model.HostDescriptions;

public class HostDescriptionsNode extends AbstractAiravataTreeNode {
	private HostDescriptions hostDescriptions;
	
	public HostDescriptionsNode(HostDescriptions hostDescriptions,TreeNode parent) {
		super(parent);
		setHostDescriptions(hostDescriptions);
	}

	@Override
	protected List<TreeNode> getChildren() {
		try {
			return getTreeNodeList(getHostDescriptions().getDescriptions().toArray(), this);
		} catch (HostDescriptionRetrieveException e) {
			e.printStackTrace();
			return emptyList();
		}
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Hosts";
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("cloud.png");
	}

	public HostDescriptions getHostDescriptions() {
		return hostDescriptions;
	}

	public void setHostDescriptions(HostDescriptions hostDescriptions) {
		this.hostDescriptions = hostDescriptions;
	}
	
	@Override
	public List<String> getSupportedActions() {
		return Arrays.asList(AddAction.ID, RefreshAction.ID, DeleteAction.ID);
	}
	
	public boolean triggerAction(JTree tree,String action) throws Exception{
		if (action.equals(DeleteAction.ID)){
			deleteHostDescription(tree);
			return true;
		}else if (action.equals(AddAction.ID)){
			HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(getRegistry());
			hostDescriptionDialog.open();
			if (hostDescriptionDialog.isHostCreated()) {
				refresh();
				reloadTreeNode(tree, this);
			}
			return true;
		} 
		return super.triggerAction(tree, action);
	}

	private void deleteHostDescription(JTree tree)
			throws Exception {
		if (askQuestion("Host descriptions", "Are you sure that you want to remove all host descriptions in this registry?")) {
			Registry registry = getRegistry();
			List<HostDescription> descriptions = getHostDescriptions().getDescriptions();
			for (HostDescription descriptionWrap : descriptions) {
				registry.deleteHostDescription(descriptionWrap.getId());
			}
			refresh();
			reloadTreeNode(tree, this);
		}
	}

	@Override
	public String getActionCaption(AbstractBrowserActionItem action) {
		if (action.getID().equals(DeleteAction.ID)){
			return "Remove all hosts";
		}else if (action.getID().equals(AddAction.ID)){
			return "New host...";
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

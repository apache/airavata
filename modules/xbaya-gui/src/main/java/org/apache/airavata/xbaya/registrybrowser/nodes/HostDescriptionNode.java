package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.DeleteAction;
import org.apache.airavata.xbaya.registrybrowser.menu.EditAction;

public class HostDescriptionNode extends AbstractAiravataTreeNode {
    private HostDescription hostDescription;

    public HostDescriptionNode(HostDescription hostDescription, TreeNode parent) {
        super(parent);
        setHostDescription(hostDescription);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return emptyList();
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getHostDescription().getId();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("host.png");
    }

    public HostDescription getHostDescription() {
        return hostDescription;
    }

    public void setHostDescription(HostDescription hostDescription) {
        this.hostDescription = hostDescription;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(EditAction.ID, DeleteAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
            deleteHostDescription(tree);
            return true;
        } else if (action.equals(EditAction.ID)) {
            JOptionPane.showMessageDialog(null, "TODO");
            return true;
        }
        return super.triggerAction(tree, action);
    }

    private void deleteHostDescription(JTree tree) throws RegistryException {
        if (askQuestion("Host description", "Are you sure that you want to remove the host description \""
                + getHostDescription().getId() + "\"?")) {
            getRegistry().deleteHostDescription(getHostDescription().getId());
            ((AbstractAiravataTreeNode) getParent()).refresh();
            reloadTreeNode(tree, getParent());
        }
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(DeleteAction.ID)) {
            return "Remove";
        } else if (action.getID().equals(EditAction.ID)) {
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

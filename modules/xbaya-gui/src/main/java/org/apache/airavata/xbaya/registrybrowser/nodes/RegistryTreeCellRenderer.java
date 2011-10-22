package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class RegistryTreeCellRenderer implements TreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        if (value instanceof AbstractAiravataTreeNode) {
            AbstractAiravataTreeNode node = (AbstractAiravataTreeNode) value;
            return node.getNodeComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
        return new DefaultTreeCellRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                hasFocus);
    }

}

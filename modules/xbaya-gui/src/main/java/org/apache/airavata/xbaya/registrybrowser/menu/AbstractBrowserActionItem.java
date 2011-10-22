package org.apache.airavata.xbaya.registrybrowser.menu;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

public abstract class AbstractBrowserActionItem {

    private String caption;
    private Icon icon;
    private JMenuItem menuItem;
    private String description;

    public abstract String getID();

    public void setIcon(Icon icon) {
        this.icon = icon;
        getMenuItem().setIcon(getIcon());
    }

    public Icon getIcon() {
        return icon;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
        getMenuItem().setText(getCaption());
    }

    public JMenuItem getMenuItem() {
        if (menuItem == null) {
            menuItem = new JMenuItem(getCaption());
        }
        menuItem.setText(getCaption());
        return menuItem;
    }

    public void addActionListener(ActionListener listener) {
        getMenuItem().addActionListener(listener);
    }

    public void removeActionListener(ActionListener listener) {
        getMenuItem().removeActionListener(listener);
    }

    public void setVisible(boolean visible) {
        getMenuItem().setVisible(visible);
    }

    public void setEnabled(boolean enabled) {
        getMenuItem().setEnabled(enabled);
    }

    public abstract String getDefaultCaption();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        getMenuItem().setToolTipText(getDescription());
    }
}

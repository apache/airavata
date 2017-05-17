/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.xbaya.ui.actions;

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

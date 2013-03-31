package org.apache.airavata.xbaya.ui.menues;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.amazon.AmazonEC2InvokerWindow;
import org.apache.airavata.xbaya.ui.dialogs.amazon.AmazonS3UtilsWindow;
import org.apache.airavata.xbaya.ui.dialogs.amazon.ChangeCredentialWindow;
import org.apache.airavata.xbaya.ui.dialogs.amazon.EC2InstancesManagementWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AmazonEC2MenuItem {

    private JMenu amazonEC2Menu;

    private JMenuItem amazonAuthenticationItem;

    private JMenuItem amazonEC2ToolItem;

    private JMenuItem amazonS3ToolItem;

    private JMenuItem configAndDeploy;

    private XBayaEngine engine;

    /**
     * Constructs a AmazonEC2MenuItem.
     *
     * @param engine XBaya engine
     */
    public AmazonEC2MenuItem(XBayaEngine engine) {
        this.engine = engine;

        createAmazonEC2Menu();
    }

    /**
     * create menu
     */
    private void createAmazonEC2Menu() {
        createAmazonAuthenticationItem();
        createAmazonEC2ToolItem();
        createAmazonS3ToolItem();
        createConfigAndRunItem();

        this.amazonEC2Menu = new JMenu("Amazon");

        this.amazonEC2Menu.add(this.amazonAuthenticationItem);
        this.amazonEC2Menu.addSeparator();
        this.amazonEC2Menu.add(this.amazonEC2ToolItem);
//        this.amazonEC2Menu.add(this.amazonS3ToolItem);
        this.amazonEC2Menu.addSeparator();
//        this.amazonEC2Menu.add(this.configAndDeploy);
    }

    private void createAmazonAuthenticationItem() {
        this.amazonAuthenticationItem = new JMenuItem("Security Credentials");
        this.amazonAuthenticationItem.addActionListener(new AbstractAction() {
            private ChangeCredentialWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new ChangeCredentialWindow(AmazonEC2MenuItem.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2MenuItem.this.engine.getGUI().getErrorWindow().error(e1);
                }
            }
        });
    }

    private void createAmazonEC2ToolItem(){
        this.amazonEC2ToolItem = new JMenuItem("EC2 Instances Management");
        this.amazonEC2ToolItem.addActionListener(new AbstractAction() {
            private EC2InstancesManagementWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new EC2InstancesManagementWindow(AmazonEC2MenuItem.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2MenuItem.this.engine.getGUI().getErrorWindow().error(e1);
                }
            }
        });
    }

    private void createAmazonS3ToolItem() {
        this.amazonS3ToolItem = new JMenuItem("S3 Upload/Download Tool");
        this.amazonS3ToolItem.addActionListener(new AbstractAction() {
            private AmazonS3UtilsWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = AmazonS3UtilsWindow.getInstance(AmazonEC2MenuItem.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2MenuItem.this.engine.getGUI().getErrorWindow().error(e1);
                }
            }
        });
    }

    private void createConfigAndRunItem() {
        this.configAndDeploy = new JMenuItem("Config and Deploy Job Flow");
        this.configAndDeploy.addActionListener(new AbstractAction() {
            private AmazonEC2InvokerWindow window;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new AmazonEC2InvokerWindow(AmazonEC2MenuItem.this.engine);
                }
                try {
                    this.window.show();
                } catch (Exception e1) {
                    AmazonEC2MenuItem.this.engine.getGUI().getErrorWindow().error(e1);
                }

            }
        });

    }

    public JMenu getMenu() {
        return this.amazonEC2Menu;
    }

}


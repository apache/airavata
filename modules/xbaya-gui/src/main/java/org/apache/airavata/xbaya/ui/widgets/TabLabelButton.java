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
package org.apache.airavata.xbaya.ui.widgets;
 
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;
 
public class TabLabelButton extends JPanel implements ActionListener{
 
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane tabPanel;
	
	private ActionListener closeButtonListener;
	
	
	public TabLabelButton(final JTabbedPane pane, String closeButtonTip) {
        super();
        setTabPanel(pane);
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        layout.setAlignment(FlowLayout.LEFT);
        layout.setVgap(0);
        layout.setHgap(0);
        setLayout(layout);
        setOpaque(false);
        JLabel label = new JLabel() {
        	String previousText=null;
			private static final long serialVersionUID = 1L;

			public String getText() {
                int i = pane.indexOfTabComponent(TabLabelButton.this);
                if (i != -1) {
                	if (!pane.getTitleAt(i).equals(previousText)){
                		previousText=pane.getTitleAt(i);
                    	TabLabelButton.this.updateUI();
                	}
                    return previousText;
                }
                return null;
            }
        };
         
        add(label);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        final JButton button = new JButton(){
			private static final long serialVersionUID = 1L;
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                //Only if the selected tab is this tab we draw the close
                if (pane.indexOfTabComponent(TabLabelButton.this)==pane.getSelectedIndex()){
                	Graphics2D drawer = (Graphics2D) g.create();
                    if (getModel().isPressed()) {
                        drawer.translate(1, 1);
                    }
                    drawer.setStroke(new BasicStroke(2));
                	drawer.setColor(Color.GRAY);
                	if (getModel().isRollover()) {
                        drawer.setColor(new Color(200,0,0));
                    }
                	setBorderPainted(getModel().isRollover());
                    int delta = 7;
					int right = (getWidth()-1) - delta;
					int bottom = (getHeight()-1) - delta;
					drawer.drawLine(delta, delta, right, bottom);
                    drawer.drawLine(right, delta, delta, bottom);
                    drawer.dispose();
                }
            }
        };
        button.setPreferredSize(new Dimension(20, 20));
        button.setToolTipText(closeButtonTip);
        button.setUI(new BasicButtonUI());
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        button.setBorderPainted(false);
        button.setRolloverEnabled(true);
        button.addActionListener(this);
        
        add(button);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

	public JTabbedPane getTabPanel() {
		return tabPanel;
	}

	public void setTabPanel(JTabbedPane tabPanel) {
		this.tabPanel = tabPanel;
	}

	public ActionListener getCloseButtonListener() {
		return closeButtonListener;
	}

	public void setCloseButtonListener(ActionListener closeButtonListener) {
		this.closeButtonListener = closeButtonListener;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
        if (getTabPanel().indexOfTabComponent(TabLabelButton.this)==getTabPanel().getSelectedIndex()){
        	if (getCloseButtonListener()!=null){
        		getCloseButtonListener().actionPerformed(event);
        	}
        }else{
        	getTabPanel().setSelectedIndex(getTabPanel().indexOfTabComponent(TabLabelButton.this));
        }
	}
}

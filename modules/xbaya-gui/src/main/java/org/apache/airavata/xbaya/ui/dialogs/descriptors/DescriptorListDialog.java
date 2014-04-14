/*
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
 *
 */

package org.apache.airavata.xbaya.ui.dialogs.descriptors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
//import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.xbaya.registrybrowser.nodes.JCRBrowserIcons;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;

public class DescriptorListDialog extends JDialog {

	private static final long serialVersionUID = 478151437279682576L;

	private XBayaGUI xbayaGUI;

    private XBayaDialog dialog;

    private AiravataAPI registry;

	private JList descriptorList;

	private Map<String[],ApplicationDescription> dlist;

	private JButton okButton;
	
	private boolean serviceSelected=false;

	public enum DescriptorType{
		HOST,
		SERVICE,
		APPLICATION
	};

	public DescriptorType descriptorType;

    /**
     *
     * @param registry
     * @param descriptorType
     */
    public DescriptorListDialog(AiravataAPI registry, DescriptorType descriptorType) {
        setRegistry(registry);
        this.descriptorType=descriptorType;
        initGUI();
        
    }

    /**
     * Displays the dialog.
     */
    public void open() {
    	pack();
    	setModal(true);
        // Adjust the size if it's bigger than the screen.
        Dimension size = getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int inset = 100;
        int width = size.width;
        if (width > screenSize.width) {
            width = screenSize.width - inset;
        }
        int height = size.height;
        if (height > screenSize.height) {
            height = screenSize.height - inset;
        }
        setSize(width, height);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void close() {
        setVisible(false);
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
    	descriptorList= new JList(new DefaultListModel());
    	descriptorList.setCellRenderer(new DescriptorListCellRenderer(descriptorType));
    	JScrollPane pane = new JScrollPane(descriptorList);

    	GridPanel infoPanel=new GridPanel();
        infoPanel.add(pane);
        infoPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        SwingUtil.layoutToGrid(infoPanel.getSwingComponent(), 1, 1, 0, 0);

        descriptorList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				boolean isSelected=descriptorList.getSelectedIndex()!=-1;
				okButton.setEnabled(isSelected);
			}
        	
        });
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	serviceSelected=true;
            	close();
            }

        });
        JButton closeButton = new JButton("Cancel");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        

        GridPanel buttonPanel = new GridPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(closeButton);
        buttonPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        String title=null; 
        switch (descriptorType){
        	case HOST:
        		title="Host Descriptions";
        		break;
        	case SERVICE:
        		title="Service Descriptions";
        		break;
        	case APPLICATION:
        		title="Application Descriptions";
        		break;
        }
        getContentPane().add(infoPanel.getSwingComponent());
        getContentPane().add(buttonPanel.getSwingComponent());
        SwingUtil.layoutToGrid(getContentPane(), 2, 1, 0, 0);
        getRootPane().setDefaultButton(okButton);
        okButton.setEnabled(false);
        loadDescriptors();
    }
    
	public Object getSelected() {
		return descriptorList.getModel().getElementAt(descriptorList.getSelectedIndex());
	}

	protected boolean askQuestion(String title, String question) {
        return JOptionPane.showConfirmDialog(null, question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    
    private void loadDescriptors() {
    	((DefaultListModel)descriptorList.getModel()).removeAllElements();
    	try {
    		List<?> descriptors=null;
			switch (descriptorType){
	    	case HOST:
	    		descriptors = getRegistry().getApplicationManager().getAllHostDescriptions();
	    		break;
	    	case SERVICE:
	    		descriptors = getRegistry().getApplicationManager().getAllServiceDescriptions();
	    		break;
	    	case APPLICATION:
	    		dlist=getRegistry().getApplicationManager().getAllApplicationDescriptions();
	    		descriptors =Arrays.asList(dlist.values().toArray(new ApplicationDescription[]{}));
	    		break;
    		}
    		for (Object d : descriptors) {
				((DefaultListModel)descriptorList.getModel()).addElement(d);
			}
		} catch (AiravataAPIInvocationException e) {
			xbayaGUI.getErrorWindow().error(e);
		}
	}
    
    private static class DescriptorListCellRenderer extends DefaultListCellRenderer{
		private static final long serialVersionUID = -1019715929291926180L;
		private DescriptorType descriptorType;
		public DescriptorListCellRenderer(DescriptorType descriptorType) {
			this.descriptorType=descriptorType;
		}
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (c instanceof JLabel){
				switch (descriptorType){
		    	case HOST:
		    		((JLabel) c).setText(((HostDescription)value).getType().getHostName());
					((JLabel) c).setIcon(JCRBrowserIcons.HOST_ICON);
		    		break;
		    	case SERVICE:
		    		((JLabel) c).setText(((ServiceDescription)value).getType().getName());
					((JLabel) c).setIcon(JCRBrowserIcons.SERVICE_ICON);
		    		break;
		    	case APPLICATION:
		    		((JLabel) c).setText(((ApplicationDescription)value).getType().getApplicationName().getStringValue());
					((JLabel) c).setIcon(JCRBrowserIcons.APPLICATION_ICON);
		    		break;
				}
				
			}
			return c;
		}
    	
    }
    public AiravataAPI getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataAPI registry) {
        this.registry = registry;
    }

	public boolean isServiceSelected() {
		return serviceSelected;
	}

	public void setServiceSelected(boolean serviceSelected) {
		this.serviceSelected = serviceSelected;
	}
}
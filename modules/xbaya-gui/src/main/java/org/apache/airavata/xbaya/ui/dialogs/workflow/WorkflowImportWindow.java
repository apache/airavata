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
package org.apache.airavata.xbaya.ui.dialogs.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.xml.namespace.QName;

import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.ThriftClientData;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.experiment.RegistrySearchResult;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XbayaEnhancedList;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public class WorkflowImportWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private JButton deleteButton;

    private XbayaEnhancedList<RegistrySearchResult> list;

    private Client client;

    private static final Logger log = LoggerFactory.getLogger(WorkflowImportWindow.class);
    /**
     * Constructs a RegistryLoaderWindow.
     *
     * @param engine
     * @throws Exception 
     */
    public WorkflowImportWindow(XBayaEngine engine) throws Exception {
        this.engine = engine;
        if (engine.getGUI().setupThriftClientData(ThriftServiceType.API_SERVICE)) {
        	ThriftClientData thriftClientData = engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE);
        	setClient(AiravataClientFactory.createAiravataClient(thriftClientData.getServerAddress(), thriftClientData.getServerPort()));
            initGUI();
        } else {
        	throw new Exception("Thrift data not setup for workflow service!!!");
        }
    }

    /**
     * Shows the window.
     */
    public void show() {

        /*
         * this.list.getList().setListData( new String[]{ "Loading the workflow list from the Registry.",
         * "Please wait for a moment."});
         */
        this.list.setEnabled(false);
        this.okButton.setEnabled(false);
        this.deleteButton.setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                try {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
								//FIXME: Update the gateway id fetched from UI
                                List<String> resultList = getClient().getAllWorkflows(engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE).getGatewayId());
								if (resultList == null || resultList.size() == 0) {
								    /*
								     * OGCEXRegistryLoaderWindow.this.list.getList(). setListData( new
								     * String[]{"No workflow"});
								     */
								} else {
								    Vector<RegistrySearchResult> results = new Vector<RegistrySearchResult>();
								    String val = null;
								    for (String key : resultList) {
								       results.add(new RegistrySearchResult(new QName(key),key,key));
								    }
								    WorkflowImportWindow.this.list.setListData(results);
								    WorkflowImportWindow.this.list.setEnabled(true);
								}
							} catch (InvalidRequestException e) {
                                log.error(e.getMessage(), e);
							} catch (AiravataClientException e) {
                                log.error(e.getMessage(), e);
							} catch (AiravataSystemException e) {
                                log.error(e.getMessage(), e);
							} catch (TException e) {
                                log.error(e.getMessage(), e);
							}
                        }
                    });
                } catch (RuntimeException e) {
                	WorkflowImportWindow.this.engine.getGUI().getErrorWindow().error(
                            ErrorMessages.REGISTRY_WORKFLOW_LIST_LOAD_ERROR, e);
                    hide();
                } catch (Error e) {
                	WorkflowImportWindow.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                    hide();
                }
            }
        }.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.dialog.show();
    }

    /**
     * Hides the window.
     */
    public void hide() {
        this.dialog.hide();
    }

    private void ok() {
    	List<RegistrySearchResult> selectedValues = this.list.getSelectedValues();
		try {
	    	for (RegistrySearchResult result : selectedValues) {
            	org.apache.airavata.model.Workflow workflow = getClient().getWorkflow(result.getResourceName());
                XmlElement workflowElement = XMLUtil.stringToXmlElement(workflow.getGraph());
                Workflow w = new Workflow(workflowElement);
                GraphCanvas newGraphCanvas = engine.getGUI().newGraphCanvas(true);
                newGraphCanvas.setWorkflow(w);
                engine.getGUI().getGraphCanvas().setWorkflowFile(null);
			}
	    	hide();
        } catch (Exception e) {
        	engine.getGUI().getErrorWindow().error(e);
        }
    }

    private void delete() {
        for (RegistrySearchResult i : this.list.getSelectedValues()) {
            try {
                getClient().deleteWorkflow(i.getResourceName());
            } catch (Exception e) {
	        	engine.getGUI().getErrorWindow().error(e);
			}
        }
        this.list.removeSelectedRows();
        hide();
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {

        this.list = new XbayaEnhancedList<RegistrySearchResult>();

        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    // double click is same as cliking the OK button.
                	WorkflowImportWindow.this.okButton.doClick();
                }
                if (WorkflowImportWindow.this.list.getSelectedIndex() == -1) {
                	WorkflowImportWindow.this.okButton.setEnabled(false);
                	WorkflowImportWindow.this.deleteButton.setEnabled(false);
                } else {
                	WorkflowImportWindow.this.okButton.setEnabled(true);
                    WorkflowImportWindow.this.deleteButton.setEnabled(true);
                }

//                if (WorkflowImportWindow.this.list.getSelectedIndex() == 2) {
//                	WorkflowImportWindow.this.okButton.setEnabled(false);
//                	WorkflowImportWindow.this.deleteButton.setEnabled(true);
//                } else if (WorkflowImportWindow.this.list.getSelectedIndex() != 1) {
//                	WorkflowImportWindow.this.okButton.setEnabled(true);
//                	WorkflowImportWindow.this.deleteButton.setEnabled(true);
//                } else {
//                	WorkflowImportWindow.this.okButton.setEnabled(false);
//                    WorkflowImportWindow.this.deleteButton.setEnabled(false);
//                }
            }
        });

        GridPanel mainPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Select a workflow to load");
        mainPanel.getSwingComponent().setBorder(border);
        mainPanel.add(this.list);
        mainPanel.layout(1, 1, 0, 0);

        JPanel buttonPanel = new JPanel();
        this.okButton = new JButton("Load");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(this.okButton);

        this.deleteButton = new JButton("Delete");
        this.deleteButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        buttonPanel.add(this.deleteButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Load a Workflow from the Registry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}

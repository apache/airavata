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

package org.apache.airavata.xbaya.appwrapper;

public class SearchAndEditWindow {
//    private XBayaDialog dialog;
//
//    private XBayaEngine engine;
//
//    private XBayaTextField nameTextField;
//    private XBayaComboBox docTypeComboBox;
//    private JButton searchButton;
//
//    private JButton editButton;
//    private JButton deleteButton;
//    private JButton saveButton;
//    private JButton cancelButton;
//
//    private XbayaEnhancedList<OGCEXRegistrySearchResult> list;
//
//    private static SearchAndEditWindow window;
//
//    /**
//     * Constructs a AmazonS3UtilsWindow.
//     * 
//     * @param engine
//     */
//    private SearchAndEditWindow(XBayaEngine engine) {
//        this.engine = engine;
//        initGUI();
//    }
//
//    private SearchAndEditWindow() {
//    }
//
//    /**
//     * @return Status
//     */
//    public boolean isEngineSet() {
//        if (this.engine == null) {
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * @param engine
//     */
//    public void setXBayaEngine(XBayaEngine engine) {
//        this.engine = engine;
//        initGUI();
//    }
//
//    /**
//     * @return ApplicationRegistrationWindow
//     */
//    public static SearchAndEditWindow getInstance() {
//        if (window == null) {
//            window = new SearchAndEditWindow();
//        }
//
//        return window;
//    }
//
//    private void initGUI() {
//
//        GridPanel searchPanel = new GridPanel();
//        this.docTypeComboBox = new XBayaComboBox(new javax.swing.DefaultComboBoxModel(new String[] { "Host",
//                "Application", "Service" }));
//        this.docTypeComboBox.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent arg0) {
//                SearchAndEditWindow.this.list.clear();
//                SearchAndEditWindow.this.editButton.setEnabled(false);
//                SearchAndEditWindow.this.deleteButton.setEnabled(false);
//                SearchAndEditWindow.this.saveButton.setEnabled(false);
//            }
//
//        });
//        XBayaLabel docTypeLabel = new XBayaLabel("Document Type", this.docTypeComboBox);
//        this.nameTextField = new XBayaTextField();
//        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);
//        JLabel dummyLabel = new JLabel("");
//        this.searchButton = new JButton("search");
//        this.searchButton.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                SearchAndEditWindow.this.searchButton.setEnabled(false);
//                search();
//            }
//        });
//        searchPanel.add(docTypeLabel);
//        searchPanel.add(this.docTypeComboBox);
//        searchPanel.add(nameLabel);
//        searchPanel.add(this.nameTextField);
//        searchPanel.add(dummyLabel);
//        searchPanel.add(this.searchButton);
//        searchPanel.layout(3, 2, GridPanel.WEIGHT_NONE, 1);
//
//        // this.list = new XBayaList<OGCEXRegistrySearchResult>();
//        this.list = new XbayaEnhancedList<OGCEXRegistrySearchResult>();
//
//        this.list.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() >= 2) {
//                    // double click is same as cliking the OK button.
//                    SearchAndEditWindow.this.editButton.doClick();
//                }
//
//                if (SearchAndEditWindow.this.list.getSelectedIndex() == -2) {
//                    SearchAndEditWindow.this.editButton.setEnabled(false);
//                    SearchAndEditWindow.this.deleteButton.setEnabled(true);
//                    SearchAndEditWindow.this.saveButton.setEnabled(false);
//                } else if (SearchAndEditWindow.this.list.getSelectedIndex() != -1) {
//                    SearchAndEditWindow.this.editButton.setEnabled(true);
//                    SearchAndEditWindow.this.deleteButton.setEnabled(true);
//                    SearchAndEditWindow.this.saveButton.setEnabled(true);
//                } else {
//                    SearchAndEditWindow.this.editButton.setEnabled(false);
//                    SearchAndEditWindow.this.deleteButton.setEnabled(false);
//                    SearchAndEditWindow.this.saveButton.setEnabled(false);
//                }
//            }
//        });
//
//        GridPanel listPanel = new GridPanel();
//        TitledBorder border = new TitledBorder(new EtchedBorder(), "Select an item to config");
//        listPanel.getSwingComponent().setBorder(border);
//        listPanel.add(this.list);
//        listPanel.layout(1, 1, 0, 0);
//
//        GridPanel mainPanel = new GridPanel();
//        mainPanel.add(searchPanel);
//        mainPanel.add(listPanel);
//        mainPanel.layout(2, 1, GridPanel.WEIGHT_EQUALLY, 0);
//
//        JPanel buttonPanel = new JPanel();
//        this.editButton = new JButton("Edit");
//        this.editButton.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                edit();
//            }
//        });
//        this.editButton.setEnabled(false);
//        buttonPanel.add(this.editButton);
//
//        this.deleteButton = new JButton("Delete");
//        this.deleteButton.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                SearchAndEditWindow.this.editButton.setEnabled(false);
//                SearchAndEditWindow.this.deleteButton.setEnabled(false);
//                SearchAndEditWindow.this.saveButton.setEnabled(false);
//                delete();
//            }
//        });
//        this.deleteButton.setEnabled(false);
//        buttonPanel.add(this.deleteButton);
//
//        this.saveButton = new JButton("Save");
//        this.saveButton.addActionListener(new AbstractAction() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                save();
//            }
//        });
//        this.saveButton.setEnabled(false);
//        buttonPanel.add(this.saveButton);
//
//        this.cancelButton = new JButton("Cancel");
//        this.cancelButton.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                hide();
//            }
//        });
//        buttonPanel.add(this.cancelButton);
//
//        this.dialog = new XBayaDialog(this.engine, "Search from the XRegistry", mainPanel, buttonPanel);
//        this.dialog.setDefaultButton(this.cancelButton);
//    }
//
//    /**
//	 * 
//	 */
//    protected void save() {
//        XRegistryAccesser xregistryAccesser = new XRegistryAccesser(SearchAndEditWindow.this.engine);
//
//        String content = null;
//        JFileChooser chooser = new JFileChooser();
//        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//
//        if (this.docTypeComboBox.getText().equals("Host")) {
//
//            content = xregistryAccesser.getHostDesc(this.list.getSelectedValue().getQname().toString());
//
//            int returnVal = chooser.showOpenDialog(this.dialog.getDialog());
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                String filePath = chooser.getSelectedFile().getAbsolutePath() + "/HostDescription.xml";
//                this.writeStringToFile(filePath, content);
//            }
//
//        } else if (this.docTypeComboBox.getText().equals("Application")) {
//
//            content = xregistryAccesser.getApplicationDesc(this.list.getSelectedValue().getQname().toString(),
//                    this.list.getSelectedValue().getDescription());
//
//            int returnVal = chooser.showOpenDialog(this.dialog.getDialog());
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                String filePath = chooser.getSelectedFile().getAbsolutePath() + "/ApplicationDescription.xml";
//                this.writeStringToFile(filePath, content);
//            }
//
//        } else {
//
//            content = xregistryAccesser.getServiceDesc(this.list.getSelectedValue().getQname());
//
//            int returnVal = chooser.showOpenDialog(this.dialog.getDialog());
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                String filePath = chooser.getSelectedFile().getAbsolutePath() + "/ServiceDescription.xml";
//                this.writeStringToFile(filePath, content);
//            }
//        }
//    }
//
//    /**
//     * hide the dialog
//     */
//    public void hide() {
//        this.dialog.hide();
//    }
//
//    /**
//     * show the dialog
//     */
//    public void show() {
//        this.dialog.show();
//    }
//
//    /**
//     * edit an entry
//     */
//    public void edit() {
//        XRegistryAccesser xregistryAccesser = new XRegistryAccesser(SearchAndEditWindow.this.engine);
//
//        if (this.docTypeComboBox.getText().equals("Host")) {
//            // Update Host
//            this.hide();
//
//            HostDescriptionRegistrationWindow hostWindow = HostDescriptionRegistrationWindow.getInstance();
//
//            if (!hostWindow.isEngineSet()) {
//                hostWindow.setXBayaEngine(this.engine);
//            }
//
//            HostBean hostBean = xregistryAccesser.getHostBean(this.list.getSelectedValue().getQname().toString());
//            if (hostBean != null) {
//
//                hostBean.setHostName(this.list.getSelectedValue().getQname().getLocalPart());
//
//                hostWindow.show(hostBean);
//            } else {
//                this.engine.getErrorWindow().error(this.dialog.getDialog(), "Cannot get value from Xregistry");
//            }
//        } else if (this.docTypeComboBox.getText().equals("Application")) {
//            // Update Application
//            this.hide();
//
//            ApplicationDescriptionRegistrationWindow appWindow = ApplicationDescriptionRegistrationWindow.getInstance();
//
//            if (!appWindow.isEngineSet()) {
//                appWindow.setXBayaEngine(this.engine);
//            }
//
//            ApplicationBean appBean = xregistryAccesser.getApplicationBean(this.list.getSelectedValue().getQname()
//                    .toString(), this.list.getSelectedValue().getDescription());
//
//            if (appBean != null) {
//
//                appBean.setApplicationName(this.list.getSelectedValue().getQname().getLocalPart());
//                appBean.setObjectNamespace(this.list.getSelectedValue().getQname().getNamespaceURI());
//                appBean.setHostName(this.list.getSelectedValue().getDescription());
//
//                appWindow.show(appBean);
//            } else {
//                this.engine.getErrorWindow().error(this.dialog.getDialog(), "Cannot get value from Xregistry");
//            }
//        } else {
//            // Update Service
//            this.hide();
//
//            ServiceDescriptionRegistrationWindow serviceWindow = ServiceDescriptionRegistrationWindow.getInstance();
//
//            if (!serviceWindow.isEngineSet()) {
//                serviceWindow.setXBayaEngine(this.engine);
//            }
//
//            ServiceBean serviceBean = xregistryAccesser.getServiceBean(this.list.getSelectedValue().getQname());
//            if (serviceBean != null) {
//
//                serviceBean.setServiceName(this.list.getSelectedValue().getQname().getLocalPart());
//                serviceBean.setObjectNamespace(this.list.getSelectedValue().getQname().getNamespaceURI());
//
//                serviceWindow.show(serviceBean);
//            } else {
//                this.engine.getErrorWindow().error(this.dialog.getDialog(), "Cannot get value from Xregistry");
//            }
//
//        }
//    }
//
//    /**
//     * delete an entry
//     */
//    public void delete() {
//
//        XRegistryAccesser xregistryAccesser = new XRegistryAccesser(SearchAndEditWindow.this.engine);
//
//        if (this.docTypeComboBox.getText().equals("Host")) {
//            // Delete Host
//            List<OGCEXRegistrySearchResult> hostDescs = this.list.getSelectedValues();
//
//            try {
//                for (OGCEXRegistrySearchResult hostDesc : hostDescs) {
//                    xregistryAccesser.deleteHostDescription(hostDesc.getQname().toString());
//                }
//            } catch (XRegistryClientException e) {
//                this.engine.getErrorWindow().error(this.dialog.getDialog(), e.getMessage(), e);
//            }
//
//            this.list.removeSelectedRows();
//        } else if (this.docTypeComboBox.getText().equals("Application")) {
//            // Delete Application
//            List<OGCEXRegistrySearchResult> appDescs = this.list.getSelectedValues();
//            try {
//                for (OGCEXRegistrySearchResult appDesc : appDescs) {
//                    xregistryAccesser.deleteAppDescription(appDesc.getQname(), appDesc.getDescription());
//                }
//            } catch (XRegistryClientException e) {
//                this.engine.getErrorWindow().error(this.dialog.getDialog(), e.getMessage(), e);
//            }
//
//            this.list.removeSelectedRows();
//        } else {
//            // Delete Service
//            List<OGCEXRegistrySearchResult> serviceDescs = this.list.getSelectedValues();
//            try {
//                for (OGCEXRegistrySearchResult serviceDesc : serviceDescs) {
//                    xregistryAccesser.deleteServiceDescrption(serviceDesc.getQname());
//                }
//            } catch (Exception e) {
//                this.engine.getErrorWindow().error(this.dialog.getDialog(), e.getMessage(), e);
//            }
//
//            this.list.removeSelectedRows();
//        }
//
//    }
//
//    /**
//     * Search in XRegistry
//     */
//    public void search() {
//        this.list.clear();
//        this.list.setEnabled(false);
//        this.editButton.setEnabled(false);
//        this.deleteButton.setEnabled(false);
//        this.saveButton.setEnabled(false);
//
//        new Thread() {
//            @SuppressWarnings({ "unchecked" })
//            @Override
//            public void run() {
//                try {
//                    XRegistryAccesser xregistryAccesser = new XRegistryAccesser(SearchAndEditWindow.this.engine);
//
//                    String searchType = SearchAndEditWindow.this.docTypeComboBox.getText();
//                    String searchKeyWord = SearchAndEditWindow.this.nameTextField.getText();
//
//                    final Map resultList;
//                    if (searchType.equals("Host")) {
//                        resultList = xregistryAccesser.getHostDescByName(searchKeyWord);
//                    } else if (searchType.equals("Application")) {
//                        resultList = xregistryAccesser.getApplicationDescByName(searchKeyWord);
//                    } else {
//                        resultList = xregistryAccesser.getServiceDescByName(searchKeyWord);
//                    }
//                    final Set<QName> keys = resultList.keySet();
//
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (resultList == null || resultList.size() == 0) {
//                                /*
//                                 * OGCEXRegistryLoaderWindow.this.list.getList(). setListData( new
//                                 * String[]{"No workflow"});
//                                 */
//                            } else {
//                                Vector<OGCEXRegistrySearchResult> results = new Vector<OGCEXRegistrySearchResult>();
//                                for (QName key : keys) {
//                                    //todo fix this search
////                                    results.add(new OGCEXRegistrySearchResult(resultList.get(key)));
//                                }
//                                SearchAndEditWindow.this.list.setListData(results);
//                                SearchAndEditWindow.this.list.setEnabled(true);
//                            }
//                        }
//                    });
//                } catch (RuntimeException e) {
//                    SearchAndEditWindow.this.engine.getErrorWindow().error(
//                            ErrorMessages.XREGISTRY_WORKFLOW_LIST_LOAD_ERROR, e);
//                    hide();
//                } catch (Error e) {
//                    SearchAndEditWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
//                    hide();
//                } catch (XRegistryClientException e) {
//                    SearchAndEditWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
//                    hide();
//                } finally {
//                    SearchAndEditWindow.this.searchButton.setEnabled(true);
//                }
//            }
//
//        }.start();
//
//        // This has to be the last because it blocks when the dialog is modal.
//        this.dialog.simpeShow();
//    }
//
//    private boolean writeStringToFile(String filePath, String content) {
//        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
//            out.write(content);
//            out.close();
//            return true;
//        } catch (IOException e) {
//            return false;
//        }
//    }

}
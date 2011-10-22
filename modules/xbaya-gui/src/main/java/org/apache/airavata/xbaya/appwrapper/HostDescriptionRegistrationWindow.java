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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaComboBox;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.registry.RegistryAccesser;
import org.ogce.schemas.gfac.beans.HostBean;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class HostDescriptionRegistrationWindow {
    private XBayaDialog dialog;

    private XBayaEngine engine;

    private JButton addHostButton;
    private JButton cancelButton;
    private XBayaLabel gateKeeperEndPointLabel;
    private XBayaLabel jobManagerLabel;
    private XBayaComboBox gateKeeprEndpointComboBox;
    private XBayaLabel gridFTPendpointLabel;
    private XBayaComboBox gridFTPEndPointComboBox;
    private XBayaLabel hostEnvLabel;
    private XBayaTextField hostEnvTextField;
    private XBayaComboBox hostNameComboBox;
    private XBayaLabel hostNameLabel;
    private XBayaLabel isPublicLabel;
    private JCheckBox isPublicCheckBox;
    private JList serviceTypesList;
    private JScrollPane serviceTypesScrollPanel;
    private XBayaComboBox jobManagerComboBox;
    private XBayaLabel serviceTypesLabel;
    private JCheckBox sshEnabledCheckBox;
    private XBayaLabel sshEnabledLabel;
    private XBayaLabel tempDirLabel;
    private XBayaTextField tempDirTextField;
    private XBayaLabel wsGramLabel;
    private JCheckBox wsGramPresentCheckBox;
    private XBayaLabel gfacPathLabel;
    private XBayaTextField gfacPathTextField;
    private XBayaLabel jdkNameLabel;
    private XBayaComboBox jdkNameComboBox;
    private XBayaLabel jdkPathLabel;
    private XBayaTextField jdkPathTextField;

    private boolean isEditing = false;

    private HostBean editingHostBean;

    private static HostDescriptionRegistrationWindow window;

    /**
     * Constructs a AmazonS3UtilsWindow.
     * 
     * @param engine
     */
    private HostDescriptionRegistrationWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private HostDescriptionRegistrationWindow() {
        // Intend to be blank;
    }

    /**
     * @return ApplicationRegistrationWindow
     */
    public static HostDescriptionRegistrationWindow getInstance() {
        if (window == null) {
            window = new HostDescriptionRegistrationWindow();
        }

        return window;
    }

    private Object[] initHostNameList() {

        Document document = XMLUtil.retrievalXMLDocFromUrl("http://info.teragrid.org/web-apps/XML/ctss-resources-v1/");
        NodeList nodeList = document.getElementsByTagName("Resource");
        List<String> nameList = new ArrayList<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            nameList.add(nodeList.item(i).getAttributes().getNamedItem("UniqueID").getNodeValue());
        }
        return nameList.toArray();

    }

    private Object[] initGridFtpEndPoint() {
        Document document = XMLUtil.retrievalXMLDocFromUrl("http://info.teragrid.org/restdemo/xml/tg/services/gridftp");
        NodeList nodeList = document.getElementsByTagName("Service");
        List<String> endPointList = new ArrayList<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            String resourceID = nodeList.item(i).getAttributes().getNamedItem("ResourceID").getNodeValue();
            if (resourceID.equals(this.hostNameComboBox.getText())) {
                endPointList.add(nodeList.item(i).getAttributes().getNamedItem("Endpoint").getNodeValue());
            }
        }
        return endPointList.toArray();
    }

    private Object[] gateWayEndPoint() {

        Document document = XMLUtil
                .retrievalXMLDocForParse("http://info.teragrid.org/web-apps/xml/ctss-services-v1/resource/"
                        + this.hostNameComboBox.getText());
        NamespaceContext ctx = new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                String uri;
                if (prefix.equals("tg"))
                    uri = "http://mds.teragrid.org/2007/02/ctss";
                else if (prefix.equals("xlink"))
                    uri = "http://www.w3.org/1999/xlink";
                else
                    uri = null;
                return uri;
            }

            // Dummy implementation - not used!
            @SuppressWarnings("unchecked")
            public Iterator getPrefixes(String val) {
                return null;
            }

            // Dummy implemenation - not used!
            public String getPrefix(String uri) {
                return null;
            }
        };

        // Now the XPath expression
        String xpathStr = "/tg:Kit.Services/Service/Endpoint/text()";
        XPathFactory xpathFact = XPathFactory.newInstance();
        XPath xpath = xpathFact.newXPath();
        xpath.setNamespaceContext(ctx);

        try {
            Object endPoint;

            endPoint = xpath.evaluate(xpathStr, document, XPathConstants.NODESET);

            NodeList nodeList = (NodeList) endPoint;

            Set<String> endPoints = new HashSet<String>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                String ep = nodeList.item(i).getNodeValue();
                if (ep.indexOf("jobmanager") > 0) {
                    endPoints.add(ep);
                }
            }

            return endPoints.toArray();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Clear All TextFields
     */
    private void clearAllTextFields() {
        this.hostEnvTextField.setText("");
        this.tempDirTextField.setText("/tmp");
        this.gfacPathTextField.setText("");
        this.jdkPathTextField.setText("");
        this.jdkNameComboBox.setSelectedItem("jdk1.5");
    }

    /**
     * @param hostBean
     */
    public void initTextField(HostBean hostBean) {
        this.isEditing = true;
        this.editingHostBean = hostBean;
        this.hostNameComboBox.setSelectedItem(hostBean.getHostName());
        if (!(hostBean.getServiceType() == null)) {
            for (String serviceType : hostBean.getServiceType()) {
                this.serviceTypesList.setSelectedValue(serviceType, false);
            }
        }
        this.gateKeeprEndpointComboBox.setModel(new javax.swing.DefaultComboBoxModel(gateWayEndPoint()));

        this.gridFTPEndPointComboBox.setModel(new javax.swing.DefaultComboBoxModel(initGridFtpEndPoint()));
        this.hostEnvTextField.setText(hostBean.getHostEnv());
        this.tempDirTextField.setText(hostBean.getTmpDir());
        this.sshEnabledCheckBox.setSelected(hostBean.isSshEnabled());
        this.jobManagerComboBox.setSelectedItem(hostBean.getGateKeeperName());
        this.gateKeeprEndpointComboBox.setSelectedItem(hostBean.getGateKeeperendPointReference());
        this.wsGramPresentCheckBox.setSelected(hostBean.isWsGram());
        this.gridFTPEndPointComboBox.setSelectedItem(hostBean.getGridFtpendPointReference());

        if (hostBean.getgFacPath() != null && !hostBean.getgFacPath().isEmpty()) {
            this.gfacPathTextField.setText(hostBean.getgFacPath());
        }

        if (hostBean.getJdkPath() != null && !hostBean.getJdkPath().isEmpty()) {
            this.jdkPathTextField.setText(hostBean.getJdkPath());
            this.jdkNameComboBox.setSelectedItem(hostBean.getJdkName());
        }

        this.addHostButton.setText("Update Host");
    }

    private void initGUI() {
        GridPanel infoPanel = new GridPanel();
        this.hostNameComboBox = new XBayaComboBox(new javax.swing.DefaultComboBoxModel(initHostNameList()));
        this.hostNameComboBox.setEditable(true);
        this.hostNameComboBox.getJComboBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                HostDescriptionRegistrationWindow.this.gateKeeprEndpointComboBox
                        .setModel(new javax.swing.DefaultComboBoxModel(gateWayEndPoint()));

                HostDescriptionRegistrationWindow.this.gridFTPEndPointComboBox
                        .setModel(new javax.swing.DefaultComboBoxModel(initGridFtpEndPoint()));
            }
        });
        this.hostNameLabel = new XBayaLabel("Host Name", this.hostNameComboBox);

        this.serviceTypesScrollPanel = new JScrollPane();
        this.serviceTypesList = new JList();
        this.serviceTypesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "gram5", "ws-gram" };

            @Override
            public int getSize() {
                return this.strings.length;
            }

            @Override
            public Object getElementAt(int i) {
                return this.strings[i];
            }
        });
        this.serviceTypesScrollPanel.setViewportView(this.serviceTypesList);
        this.serviceTypesLabel = new XBayaLabel("Service Types", this.serviceTypesList);

        this.hostEnvTextField = new XBayaTextField();
        this.hostEnvLabel = new XBayaLabel("Host Environment", this.hostEnvTextField);

        this.tempDirTextField = new XBayaTextField("/tmp");
        this.tempDirLabel = new XBayaLabel("Temp Dir", this.tempDirTextField);

        this.sshEnabledCheckBox = new JCheckBox();
        this.sshEnabledCheckBox.setSelected(false);
        this.sshEnabledLabel = new XBayaLabel("SSH Enabled", this.sshEnabledCheckBox);

        this.jobManagerComboBox = new XBayaComboBox(new javax.swing.DefaultComboBoxModel(new String[] { "PBS", "FORK",
                "LSF", "MULTI", "CONDOR", "SPRUCE", "LoadLeveler" }));
        this.jobManagerLabel = new XBayaLabel("Gate Keeper JobManager", this.jobManagerComboBox);

        this.gateKeeprEndpointComboBox = new XBayaComboBox(new javax.swing.DefaultComboBoxModel(gateWayEndPoint()));
        this.gateKeeperEndPointLabel = new XBayaLabel("GateKeeper EndPoint", this.gateKeeprEndpointComboBox);

        this.wsGramPresentCheckBox = new JCheckBox();
        this.wsGramPresentCheckBox.setSelected(false);
        this.wsGramLabel = new XBayaLabel("WSGram Present", this.wsGramPresentCheckBox);

        this.gridFTPEndPointComboBox = new XBayaComboBox(new javax.swing.DefaultComboBoxModel(initGridFtpEndPoint()));
        this.gridFTPendpointLabel = new XBayaLabel("GridFtp Endpoint", this.gridFTPEndPointComboBox);

        this.isPublicCheckBox = new JCheckBox();
        this.isPublicCheckBox.setSelected(false);
        this.isPublicLabel = new XBayaLabel("Is Public", this.isPublicCheckBox);

        this.gfacPathTextField = new XBayaTextField();
        this.gfacPathLabel = new XBayaLabel("GFac Path (Optional)", this.gfacPathTextField);

        this.jdkNameComboBox = new XBayaComboBox(new javax.swing.DefaultComboBoxModel(new String[] { "jdk 1.4",
                "jdk 1.5", "jdk 1.6" }));
        this.jdkNameComboBox.setSelectedItem("jdk 1.5");
        this.jdkNameLabel = new XBayaLabel("JDK Name (Optional)", this.jdkNameComboBox);

        this.jdkPathTextField = new XBayaTextField();
        this.jdkPathLabel = new XBayaLabel("JDK Path (Optional)", this.jdkPathTextField);

        infoPanel.add(this.hostNameLabel);
        infoPanel.add(this.hostNameComboBox);
        infoPanel.add(this.serviceTypesLabel);
        infoPanel.add(this.serviceTypesScrollPanel);
        infoPanel.add(this.hostEnvLabel);
        infoPanel.add(this.hostEnvTextField);
        infoPanel.add(this.tempDirLabel);
        infoPanel.add(this.tempDirTextField);
        infoPanel.add(this.sshEnabledLabel);
        infoPanel.add(this.sshEnabledCheckBox);
        infoPanel.add(this.jobManagerLabel);
        infoPanel.add(this.jobManagerComboBox);
        infoPanel.add(this.gateKeeperEndPointLabel);
        infoPanel.add(this.gateKeeprEndpointComboBox);
        infoPanel.add(this.wsGramLabel);
        infoPanel.add(this.wsGramPresentCheckBox);
        infoPanel.add(this.gridFTPendpointLabel);
        infoPanel.add(this.gridFTPEndPointComboBox);
        infoPanel.add(this.isPublicLabel);
        infoPanel.add(this.isPublicCheckBox);
        infoPanel.add(this.gfacPathLabel);
        infoPanel.add(this.gfacPathTextField);
        infoPanel.add(this.jdkNameLabel);
        infoPanel.add(this.jdkNameComboBox);
        infoPanel.add(this.jdkPathLabel);
        infoPanel.add(this.jdkPathTextField);

        infoPanel.layout(13, 2, GridPanel.WEIGHT_NONE, 1);

        JPanel buttonPanel = new JPanel();
        this.addHostButton = new JButton();
        this.addHostButton.setText("Add Host");
        this.addHostButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addHostButtonActionPerformed();
            }
        });

        this.cancelButton = new JButton();
        this.cancelButton.setText("Cancel");
        this.cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        buttonPanel.add(this.addHostButton);
        buttonPanel.add(this.cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Register Host Description", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(this.cancelButton);
    }

    private void addHostButtonActionPerformed() {// GEN-FIRST:event_addHostButtonActionPerformed

        this.addHostButton.setText("Add Host");

        /* Input Validity Check */
        // TODO

        try {
            /* Generate Bean Object */
            HostBean hostBean = new HostBean();
            hostBean.setHostName(this.hostNameComboBox.getText());
            Object[] serviceTypes = this.serviceTypesList.getSelectedValues();
            List<String> serviceTypesArrayList = new ArrayList<String>();
            for (Object obj : serviceTypes) {
                serviceTypesArrayList.add((String) obj);
            }
            hostBean.setServiceType(serviceTypesArrayList);
            hostBean.setHostEnv(StringUtil.trimSpaceInString(this.hostEnvTextField.getText()));
            hostBean.setTmpDir(StringUtil.trimSpaceInString(this.tempDirTextField.getText()));
            hostBean.setSshEnabled(this.sshEnabledCheckBox.isSelected());
            hostBean.setGateKeeperJobManager(this.jobManagerComboBox.getText());
            hostBean.setGateKeeperendPointReference(this.gateKeeprEndpointComboBox.getText());
            hostBean.setWsGram(this.wsGramPresentCheckBox.isSelected());
            hostBean.setGridFtpendPointReference(this.gridFTPEndPointComboBox.getText());

            if (!this.gfacPathTextField.getText().isEmpty()) {
                hostBean.setgFacPath(this.gfacPathTextField.getText());
            }

            if (!this.jdkPathTextField.getText().isEmpty()) {
                hostBean.setJdkPath(this.jdkPathTextField.getText());
                hostBean.setJdkName(this.jdkNameComboBox.getText());
            }

            /* Register to XRegistry */
            RegistryAccesser xRegAccesser = new RegistryAccesser(this.engine);

            // if (!this.isEditing) {
            // xRegAccesser.registerHost(hostBean);
            // } else {
            // /* Delete old host bean */
            // xRegAccesser.deleteHostDescription(this.editingHostBean.getHostName());
            //
            // /* Register new host bean */
            // xRegAccesser.registerHost(hostBean);
            //
            // this.isEditing = false;
            // this.addHostButton.setText("Add Host");
            // }

        } catch (Exception e) {
            e.printStackTrace();
            this.hide();
            return;
        }

        /* Clear All The Fields */
        clearAllTextFields();

        /* "Close" the windows */
        this.hide();

    }// GEN-LAST:event_addHostButtonActionPerformed

    private void cancelButtonActionPerformed() {// GEN-FIRST:event_cancelButtonActionPerformed
        this.isEditing = false;
        this.addHostButton.setText("Add Host");
        clearAllTextFields();
        this.hide();
    }// GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @return Status
     */
    public boolean isEngineSet() {
        if (this.engine == null) {
            return false;
        }
        return true;
    }

    /**
     * @param engine
     */
    public void setXBayaEngine(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    /**
     * hide the dialog
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * show the dialog
     */
    public void show() {
        this.dialog.show();
    }

    /**
     * @param hostBean
     */
    public void show(HostBean hostBean) {
        initTextField(hostBean);
        this.show();
    }

}
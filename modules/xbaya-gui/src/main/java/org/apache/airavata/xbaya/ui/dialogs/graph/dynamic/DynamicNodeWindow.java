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

package org.apache.airavata.xbaya.ui.dialogs.graph.dynamic;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.xml.namespace.QName;

import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.workflow.model.component.url.URLComponentRegistry;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.dynamic.BasicTypeMapping;
import org.apache.airavata.workflow.model.graph.dynamic.DynamicNode;
import org.apache.airavata.workflow.model.graph.dynamic.SchemaCompilerUtil;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentController;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.invoker.DynamicServiceCreator;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentTreeNode;
import org.apache.airavata.xbaya.util.RegistryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;
import org.xmlpull.v1.builder.XmlBuilderException;

import xsul5.wsdl.WsdlDefinitions;

public class DynamicNodeWindow {

    private static final Logger log = LoggerFactory.getLogger(DynamicNodeWindow.class);

    /**
     * CLASSES_DIR
     */
    private static final String CLASSES_DIR = "classes";

    /**
     * SRC_DIR
     */
    private static final String SRC_DIR = "src";


    /**
     * CLASS
     */
    private static final String CLASS = "class";

    /**
     * PACKAGE
     */
    private static final String PACKAGE = "package";

    /**
     * LINE
     */
    private static final String LINE = "\n";

    /**
     * TAB
     */
    private static final String TAB = "\t";

    /**
     * SPACE
     */
    private static final String SPACE = " ";

    private XBayaEngine engine;

    private DynamicNode node;

    private XBayaDialog dialog;

    private XBayaTextArea javaCodeTxtArea;

    private String typesPath;

    private String functionStr;

    private JCheckBox checkBox;

    /**
     * Constructs a WSNodeWindow.
     * 
     * @param engine
     *            The XBayaEngine
     * @param node
     */
    public DynamicNodeWindow(XBayaEngine engine, DynamicNode node) {
        this.engine = engine;
        this.node = node;
        initGUI();

    }

    /**
     * Shows the notification.
     * 
     * @param event
     *            The notification to show
     */
    public void show() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = fileChooser.showOpenDialog(this.engine.getGUI().getFrame());
        if (JFileChooser.APPROVE_OPTION != ret) {
            throw new WorkflowRuntimeException("Cannot proceed without valid directory");
        }
        File selectedDir = fileChooser.getSelectedFile();
        File rootDir = new File(selectedDir, "xbaya");
        deleteDir(rootDir);
        rootDir.mkdir();
        File srcDir = new File(rootDir, SRC_DIR);
        srcDir.mkdir();
        File classesDir = new File(rootDir, CLASSES_DIR);
        classesDir.mkdir();
        initSchema(rootDir, srcDir, srcDir);

        this.javaCodeTxtArea.setText(generateClass());

        this.dialog.show();
        if (this.checkBox.isSelected()) {
            deployWebservice();
        } else {
            this.compileAndRegisterJar(rootDir, srcDir, srcDir);
        }

    }

    private void deployWebservice() {
        try {
            DynamicServiceCreator factory = new DynamicServiceCreator(
                    "http://129.79.246.108:8080/axis2/services/ServiceCreator?wsdl");
            String code = this.javaCodeTxtArea.getText();
            factory.createService(code);
            URLComponentRegistry registry = null;
            Thread.sleep(10000);
            registry = new URLComponentRegistry(new URI("http://129.79.246.108:8080/axis2/services/"
                    + getClassName(code) + "?wsdl"));
            ComponentRegistryLoader.getLoader(this.engine, RegistryConstants.REGISTRY_TYPE_URL).load(registry);

            Node newNode = this.engine
                    .getGUI()
                    .getGraphCanvas()
                    .addNode(
                            ((ComponentTreeNode) ComponentController.getComponentTree(registry).getFirstLeaf()).getComponentReference()
                                    .getComponent(), this.node.getPosition());
            List<DataPort> inputPorts = newNode.getInputPorts();
            Graph graph = this.engine.getGUI().getGraphCanvas().getGraph();
            for (int i = 0; i < inputPorts.size(); ++i) {
                graph.addEdge(this.node.getInputPort(i).getFromPort(), inputPorts.get(i));
            }
            List<DataPort> outputPorts = newNode.getOutputPorts();

            for (int i = 0; i < outputPorts.size(); ++i) {
                List<Port> toPorts = this.node.getOutputPort(i).getToPorts();
                for (Port port : toPorts) {
                    graph.removeEdge(this.node.getOutputPort(i), port);
                    graph.addEdge(outputPorts.get(i), port);
                }

            }

            this.engine.getGUI().getWorkflow().removeNode(this.node);

        } catch (Exception e) {
            this.engine.getGUI().getErrorWindow().error(e);
        }

    }

    private void hide() {
        this.dialog.hide();
    }

    private void initGUI() {
        BasicTypeMapping.reset();

        this.javaCodeTxtArea = new XBayaTextArea();
        XBayaLabel operationLabel = new XBayaLabel("Operation", this.javaCodeTxtArea);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(operationLabel);
        infoPanel.add(this.javaCodeTxtArea);
        checkBox = new JCheckBox("Export as webservice");
        infoPanel.add(new XBayaLabel("", checkBox));

        infoPanel.add(checkBox);
        infoPanel.layout(2, 2, 0, 0);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                hide();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), this.node.getName(), infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private String generateClass() {

        String ret = "package org.apache.airavata.xbaya;";
        ret += LINE + LINE;
        ret += "public class DefaultClassName{";
        ret += LINE + LINE + TAB + "public";
        String function = "";
        List<Port> toNodes = this.node.getOutputPort(0).getToPorts();
        XmlElement returnElement = null;
        DataType returnType = null;
        if (toNodes.size() == 0) {
            function += SPACE + "void";
        } else {

            if (toNodes.size() == 1 && toNodes.get(0) instanceof WSPort) {
                WSPort outPort = (WSPort) toNodes.get(0);
                returnElement = outPort.getComponentPort().getElement();
                returnType = outPort.getType();
            } else {
                throw new WorkflowRuntimeException("Unhandled  port type for Dynamic component or to many outputs");
            }
            for (Port port : toNodes) {
                if (toNodes.get(0) instanceof DataPort) {
                    if (!returnType.equals(((DataPort) toNodes.get(0)).getType())) {
                        throw new WorkflowRuntimeException(
                                "Dynamic output port connected to input ports of different types.");
                    }
                } else {
                    throw new WorkflowRuntimeException("Unhandled  port type for Dynamic component");
                }
            }
            int index = BasicTypeMapping.getSimpleTypeIndex(returnElement);
            if (-1 != index) {
                function += SPACE + BasicTypeMapping.getTypeName(index);
            } else {
                throw new WorkflowRuntimeException("WIll be fixed with complex type mappign");
            }
        }

        function += SPACE + "operationName(";
        List<DataPort> inputPorts = this.node.getInputPorts();
        boolean first = true;

        // variable list in function prototype
        for (DataPort inPort : inputPorts) {
            Port fromPort = inPort.getFromPort();
            if (fromPort instanceof WSPort) {
                WSPort wsPort = (WSPort) fromPort;
                XmlElement element = wsPort.getComponentPort().getElement();

                // QName inType = ((DataPort) fromPort).getType();
                int typeIndex = BasicTypeMapping.getSimpleTypeIndex(element);
                if (-1 != typeIndex) {
                    if (first) {
                        first = false;
                    } else {
                        function += SPACE + ",";
                    }
                    function += BasicTypeMapping.getTypeName(typeIndex) + SPACE
                            + BasicTypeMapping.getTypeVariableName(typeIndex);
                } else {
                    throw new WorkflowRuntimeException("Complex Type occured:This will be fixed!!!!!");
                }
            } else {
                throw new WorkflowRuntimeException("Dynamic Node connected to non data port");
            }
        }

        function += ")";
        ret += function;
        this.functionStr = function;
        // body
        ret += "{" + LINE + LINE;
        if (null != returnElement) {
            ret += TAB + TAB + "return" + SPACE
                    + BasicTypeMapping.getTypeDefault(BasicTypeMapping.getSimpleTypeIndex(returnElement)) + ";";
        }
        ret += LINE;
        ret += TAB + "}";
        ret += LINE + "}";
        return ret;

    }

    private void initSchema(File rootDir, File srcDir, File classesDir) {

        List<DataPort> inputPorts = node.getInputPorts();
        for (DataPort inPort : inputPorts) {
            Port fromPort = inPort.getFromPort();
            Node fromNode = inPort.getFromNode();
            if (fromNode instanceof WSNode) {
                WSNode fromWsNode = (WSNode) fromNode;
                if (null != fromPort && fromPort instanceof DataPort) {
                    DataPort fromDataPort = (DataPort) fromPort;
//                    WsdlDefinitions wsdl = engine.getGUI().getWorkflow().getWSDLs().get(fromWsNode.getWSDLID());
//                    Iterator<XmlNamespace> itr = wsdl.xml().namespaces().iterator();
//                    try {
//                        XmlElement schema = wsdl.getTypes().element("schema").clone();
//                        // do not change the following ordering of setting
//                        // namespaces.
//                        schema.setNamespace(xsul5.XmlConstants.BUILDER.newNamespace("http://www.w3.org/2001/XMLSchema"));
//                        while (itr.hasNext()) {
//                            XmlNamespace next = itr.next();
//                            if (!"".equals(next.getPrefix()) && null != next.getPrefix()) {
//                                schema.setAttributeValue("xmlns:" + next.getPrefix(), next.getName());
//                            }
//
//                        }
//
//                        try {
//                            xsul5.XmlConstants.BUILDER
//                                    .serializeToOutputStream(schema, new FileOutputStream(rootDir.getCanonicalPath()
//                                            + File.separatorChar + "types.xsd"));
//                        } catch (Exception e) {
//                            log.error(e.getMessage(), e);
//                        }
//
//                        typesPath = rootDir.getCanonicalPath() + File.separatorChar + "mytype.jar";
//                        String[] args = new String[] { "-d", classesDir.getCanonicalPath(), "-src",
//                                srcDir.getCanonicalPath(), "-out", typesPath,
//                                rootDir.getCanonicalPath() + File.separatorChar + "types.xsd" };
//                        SchemaCompilerUtil.compile(args);
//
//                    } catch (XmlBuilderException e) {
//                        this.engine.getGUI().getErrorWindow().error(e);
//                    } catch (CloneNotSupportedException e) {
//                        this.engine.getGUI().getErrorWindow().error(e);
//                    } catch (IOException e) {
//                        log.error(e.getMessage(), e);
//                    }

                } else {
                    throw new WorkflowRuntimeException("Unknown port for code generation" + fromPort);
                }
            } else {
                throw new WorkflowRuntimeException("Unknown from node for code generation" + fromNode);
            }
        }
    }

    private void compileAndRegisterJar(File rootDir, File srcDir, File classesDir) {
        // String code = this.javaCodeTxtArea.getText();
        // String packageName = getPackageName(code);
        // String className = getClassName(code);
        // try {
        // File classFile = new File(srcDir.getCanonicalPath()+ File.separator
        // + packageName.replace('.', File.separatorChar)
        // + File.separator + className+".java");
        // classFile.getParentFile().mkdirs();
        //
        // FileWriter out = new FileWriter(classFile);
        // out.write(code);
        // out.flush();
        // out.close();
        //
        // JarHelper jarHelper = new JarHelper();
        // jarHelper.unjarDir(new File(this.typesPath), classesDir);
        //
        // Main.compile(new String[]{classFile.getCanonicalPath(), "-d", classesDir.getCanonicalPath()});
        // File implJar = new File(rootDir, "impl.jar");
        // jarHelper.jarDir( classesDir, implJar);
        // node.setImplURL(implJar.toURL());
        // node.setOperationName(getOperationName(code));
        // node.setClassName(getPackageName(code)+"."+getClassName(code));
        // } catch (IOException e) {
        // this.engine.getGUI().getErrorWindow().error(e);
        // }

    }

    private String getOperationName(String code) {
        String[] publicSplit = code.split("public");
        String searchStr = this.functionStr.substring(this.functionStr.indexOf("("), this.functionStr.indexOf(")"));
        int index = -1;
        for (int i = 0; i < publicSplit.length; ++i) {
            if (publicSplit[i].indexOf(searchStr) != -1) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new WorkflowRuntimeException("Operation name not found");
        }
        return publicSplit[index].substring(0, publicSplit[index].indexOf(searchStr)).trim().split(" ")[1];
    }

    private String getPackageName(String code) {
        return code.substring(code.indexOf(PACKAGE) + PACKAGE.length(), code.indexOf(";")).trim();
    }

    private String getClassName(String code) {
        return code.substring(code.indexOf(CLASS) + CLASS.length(), code.indexOf("{")).trim().split(" ")[0].trim();

    }

    private boolean hasComplexTypes() {
        List<DataPort> inputPorts = node.getInputPorts();
        for (DataPort inPort : inputPorts) {
            Port fromPort = inPort.getFromPort();
            Node fromNode = inPort.getFromNode();
            if (fromNode instanceof WSNode) {
                if (null != fromPort && fromPort instanceof DataPort) {

                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

}
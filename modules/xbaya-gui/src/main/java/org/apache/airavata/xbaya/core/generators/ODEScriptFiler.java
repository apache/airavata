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
package org.apache.airavata.xbaya.core.generators;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.gpel.model.GpelProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ODEScriptFiler {

    private XBayaEngine engine;

    private JFileChooser bpelFileChooser;
    private static final Logger log = LoggerFactory.getLogger(XBayaEngine.class);

    private final FileFilter bpelFileFilter = new FileFilter() {

        @Override
        public String getDescription() {
            return "BPEL Files";
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String name = file.getName();
            if (name.endsWith(XBayaConstants.BPEL_SUFFIX)) {
                return true;
            }
            return false;
        }
    };

    /**
     * Constructs a ODEScriptFiler.
     * 
     * @param engine
     */
    public ODEScriptFiler(XBayaEngine engine) {
        this.engine = engine;

        this.bpelFileChooser = new JFileChooser(XBayaPathConstants.BPEL_SCRIPT_DIRECTORY);
        this.bpelFileChooser.addChoosableFileFilter(this.bpelFileFilter);
    }

    /**
	 * 
	 */
    public void save() {
        Workflow wf = this.engine.getGUI().getWorkflow();
        if (0 == wf.getGraph().getNodes().size()) {
            this.engine.getGUI().getErrorWindow().warning("Workflow is Empty");
            return;
        }
        GpelProcess process;
        try {

            int returnVal = this.bpelFileChooser.showSaveDialog(this.engine.getGUI().getFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = this.bpelFileChooser.getSelectedFile();

                String path = file.getPath();

                // Remove ".bpel" at the end if any
                if (path.endsWith(XBayaConstants.BPEL_SUFFIX)) {
                    path = path.substring(0, path.length() - XBayaConstants.BPEL_SUFFIX.length());
                }

                // Add ".bpel" at the end of the file name
                File bpelFile = new File(path + XBayaConstants.BPEL_SUFFIX);
                // Add ".wsdl" at the end of the file name
                File wsdlFile = new File(path + XBayaConstants.WSDL_SUFFIX);
                // todo this has to fix, for compilation purpose passing dummy value instead of xregistry url
                URI temp = null;
                try {
                    temp = new URI("temp");
                } catch (URISyntaxException e) {
                    log.error(e.getMessage(), e);
                }
//                process = wf.getOdeProcess(WSDLUtil.appendWSDLQuary(temp), this.engine.getConfiguration().getODEURL());
//                String processString = process.xmlStringPretty();
//                FileWriter writer = new FileWriter(bpelFile);
//                writer.write(processString);
//                writer.close();
//
//                WsdlDefinitions workflowWSDL = wf.getOdeWorkflowWSDL(this.engine.getConfiguration().getDSCURL(),
//                        this.engine.getConfiguration().getODEURL());
//                String workflowWsdlStr = XmlConstants.BUILDER.serializeToStringPretty(workflowWSDL.xml());
//                writer = new FileWriter(wsdlFile);
//                writer.write(workflowWsdlStr);
//
//                Map<String, WsdlDefinitions> wsdlMap = wf.getOdeServiceWSDLs(
//                        this.engine.getConfiguration().getDSCURL(), this.engine.getConfiguration().getODEURL());
//                Set<String> keySet = wsdlMap.keySet();
//                for (String string : keySet) {
//                    writer = new FileWriter(new File(wsdlFile.getParent(), QName.valueOf(string).getLocalPart()));
//                    writer.write(XmlConstants.BUILDER.serializeToStringPretty(wsdlMap.get(string).xml()));
//                    writer.close();
//                }
//
//                XmlElement deployDescriptor = wf.getODEDeploymentDescriptor(this.engine.getConfiguration().getDSCURL(),
//                        this.engine.getConfiguration().getODEURL());
//                writer = new FileWriter(new File(wsdlFile.getParent(), "deploy.xml"));
//                writer.write(XmlConstants.BUILDER.serializeToString(deployDescriptor));
//                writer.close();
//
            }
//
//        } catch (IOException e) {
//            this.engine.getGUI().getErrorWindow().error(ErrorMessages.WRITE_FILE_ERROR, e);
//        } catch (GraphException e) {
//            this.engine.getGUI().getErrorWindow().error(ErrorMessages.GRAPH_FORMAT_ERROR, e);
        } catch (RuntimeException e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (Error e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
//        } catch (ComponentException e) {
//            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }

    }

}
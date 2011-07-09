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

package org.apache.airavata.xbaya.file.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.gpel.script.WorkflowWSDL;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.gpel.model.GpelProcess;

import xsul5.MLogger;

public class BPELFiler {

    private static MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private JFileChooser bpelFileChooser;

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
     * Constructs a BPELFiler.
     * 
     * @param engine
     */
    public BPELFiler(XBayaEngine engine) {
        this.engine = engine;

        this.bpelFileChooser = new JFileChooser(XBayaPathConstants.BPEL_SCRIPT_DIRECTORY);
        this.bpelFileChooser.addChoosableFileFilter(this.bpelFileFilter);

    }

    /**
     * Exports a BPEL process to the local file
     */
    public void exportBPEL() {
        Workflow workflow = this.engine.getWorkflow();
        BPELScript bpel = new BPELScript(workflow);

        // Check if there is any errors in the workflow first.
        ArrayList<String> warnings = new ArrayList<String>();
        if (!bpel.validate(warnings)) {
            StringBuilder buf = new StringBuilder();
            for (String warning : warnings) {
                buf.append("- ");
                buf.append(warning);
                buf.append("\n");
            }
            this.engine.getErrorWindow().warning(buf.toString());
            return;
        }

        int returnVal = this.bpelFileChooser.showSaveDialog(this.engine.getGUI().getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = this.bpelFileChooser.getSelectedFile();
            logger.finest(file.getPath());

            String path = file.getPath();

            // Remove ".bpel" at the end if any
            if (path.endsWith(XBayaConstants.BPEL_SUFFIX)) {
                path = path.substring(0, path.length() - XBayaConstants.BPEL_SUFFIX.length());
            }

            // Add ".bpel" at the end of the file name
            File bpelFile = new File(path + XBayaConstants.BPEL_SUFFIX);
            // Add ".wsdl" at the end of the file name
            File wsdlFile = new File(path + XBayaConstants.WSDL_SUFFIX);

            try {
                // Create the script.
                bpel.create(BPELScriptType.BPEL2);

                GpelProcess gpelProcess = bpel.getGpelProcess();
                XMLUtil.saveXML(gpelProcess.xml(), bpelFile);

                WorkflowWSDL workflowWSDL = bpel.getWorkflowWSDL();
                XMLUtil.saveXML(workflowWSDL.getWsdlDefinitions().xml(), wsdlFile);

            } catch (IOException e) {
                this.engine.getErrorWindow().error(ErrorMessages.WRITE_FILE_ERROR, e);
            } catch (GraphException e) {
                this.engine.getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
            } catch (RuntimeException e) {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            } catch (Error e) {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            }
        }
    }

}
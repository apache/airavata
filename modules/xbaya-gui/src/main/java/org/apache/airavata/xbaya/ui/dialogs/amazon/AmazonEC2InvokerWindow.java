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
package org.apache.airavata.xbaya.ui.dialogs.amazon;

//import edu.indiana.extreme.amazonec2webservice.AmazonEC2WebserviceCallbackHandler;
//import edu.indiana.extreme.amazonec2webservice.AmazonEC2WebserviceStub;
//import edu.indiana.extreme.amazonec2webservice.AmazonEC2WebserviceStub.JobStatusResponse;
//import edu.indiana.extreme.amazonec2webservice.AmazonEC2WebserviceStub.JobSubmissionReceipt;
//import edu.indiana.extreme.amazonec2webservice.AmazonEC2WebserviceStub.JobSubmitOperation;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.axis2.AxisFault;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

public class AmazonEC2InvokerWindow {
    private XBayaEngine engine;

    private XBayaTextField accessKeyIDTextField;
    private XBayaTextField secretAccessKeyTextField;
    private XBayaTextField keyPairNameTextField;
    private XBayaTextField numOfInstancesTextField;
    private XBayaTextField jobFlowNameTextField;
    private XBayaTextField logLocationOnS3TextField;
    private XBayaTextField inputLocationOnS3TextField;
    private XBayaTextField outputLocationOnS3TextField;
    private XBayaTextField jarFilePathOnS3TextField;
    private XBayaTextField mainClassNameTextField;
    private XBayaDialog dialog;

    /**
     * Constructs a PegasusInvokerWindow.
     */
    public AmazonEC2InvokerWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    @SuppressWarnings("serial")
	protected void initGUI() {

        this.accessKeyIDTextField = new XBayaTextField();
        XBayaLabel accessKeyIDLabel = new XBayaLabel("Access Key", this.accessKeyIDTextField);

        this.secretAccessKeyTextField = new XBayaTextField();
        XBayaLabel secretAccessKeyLabel = new XBayaLabel("Secret Access Key", this.secretAccessKeyTextField);

        this.keyPairNameTextField = new XBayaTextField();
        XBayaLabel keyPairNameLabel = new XBayaLabel("Key Pair Name", this.keyPairNameTextField);

        this.numOfInstancesTextField = new XBayaTextField();
        XBayaLabel numOfInstancesLabel = new XBayaLabel("Number of Instances", this.numOfInstancesTextField);

        this.jobFlowNameTextField = new XBayaTextField();
        XBayaLabel jobFlowNameLabel = new XBayaLabel("Job Flow Name", this.jobFlowNameTextField);

        this.logLocationOnS3TextField = new XBayaTextField();
        XBayaLabel logLocationOnS3Label = new XBayaLabel("Log Location(S3)", this.logLocationOnS3TextField);

        this.inputLocationOnS3TextField = new XBayaTextField();
        XBayaLabel inputLocationOnS3Label = new XBayaLabel("Input Location(S3)", this.inputLocationOnS3TextField);

        this.outputLocationOnS3TextField = new XBayaTextField();
        XBayaLabel outputLocationOnS3Label = new XBayaLabel("Output Location(S3)", this.outputLocationOnS3TextField);

        this.jarFilePathOnS3TextField = new XBayaTextField();
        XBayaLabel jarFilePathOnS3Label = new XBayaLabel("Jar File Location(S3)", this.jarFilePathOnS3TextField);

        this.mainClassNameTextField = new XBayaTextField();
        XBayaLabel mainClassNameLabel = new XBayaLabel("Main Class Name", this.mainClassNameTextField);

        this.accessKeyIDTextField.setText("AKIAI3GNMQVYA5LSQNEQ");
        this.secretAccessKeyTextField.setText("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");
        this.keyPairNameTextField.setText("XbayaHadoopTest");
        this.numOfInstancesTextField.setText("4");
        this.jobFlowNameTextField.setText("Test-job-flow");
        this.logLocationOnS3TextField.setText("s3n://xbaya-ec2-test/logs");
        this.inputLocationOnS3TextField.setText("s3n://xbaya-ec2-test/input/");
        this.outputLocationOnS3TextField.setText("s3n://xbaya-ec2-test/output/");
        this.jarFilePathOnS3TextField.setText("s3n://xbaya-ec2-test/jars/Hadoopv400.jar");
        this.mainClassNameTextField.setText("edu.indiana.extreme.HadoopRayTracer");

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(accessKeyIDLabel);
        infoPanel.add(this.accessKeyIDTextField);
        infoPanel.add(secretAccessKeyLabel);
        infoPanel.add(this.secretAccessKeyTextField);
        infoPanel.add(keyPairNameLabel);
        infoPanel.add(this.keyPairNameTextField);
        infoPanel.add(numOfInstancesLabel);
        infoPanel.add(this.numOfInstancesTextField);
        infoPanel.add(jobFlowNameLabel);
        infoPanel.add(this.jobFlowNameTextField);
        infoPanel.add(logLocationOnS3Label);
        infoPanel.add(this.logLocationOnS3TextField);
        infoPanel.add(inputLocationOnS3Label);
        infoPanel.add(this.inputLocationOnS3TextField);
        infoPanel.add(outputLocationOnS3Label);
        infoPanel.add(this.outputLocationOnS3TextField);
        infoPanel.add(jarFilePathOnS3Label);
        infoPanel.add(this.jarFilePathOnS3TextField);
        infoPanel.add(mainClassNameLabel);
        infoPanel.add(this.mainClassNameTextField);

        infoPanel.layout(10, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(infoPanel);
        mainPanel.layout(1, 1, 0, 0);

        JButton invokeButton = new JButton("Invoke");
        invokeButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                execute();
            }

        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(invokeButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Deploy Workflow", mainPanel, buttonPanel);
    }

    /**
     * Deploy Work Flow to Amazon EC2
     */
    protected void execute() {
//        try {
//            AmazonEC2WebserviceStub stub = new AmazonEC2WebserviceStub();
//            JobSubmitOperation jobInfo = new JobSubmitOperation();
//            jobInfo.setAccessKeyID(this.accessKeyIDTextField.getText());
//            jobInfo.setSecretAccessKey(this.secretAccessKeyTextField.getText());
//            jobInfo.setKeyPairName(this.keyPairNameTextField.getText());
//            jobInfo.setNumOfInstances(this.numOfInstancesTextField.getText());
//            jobInfo.setJobFlowName(this.jobFlowNameTextField.getText());
//            jobInfo.setLogLocationOnS3(this.logLocationOnS3TextField.getText());
//            jobInfo.setInputLocationOnS3(this.inputLocationOnS3TextField.getText());
//            jobInfo.setOutputLocationOnS3(this.outputLocationOnS3TextField.getText());
//            jobInfo.setJarFilePathOnS3(this.jarFilePathOnS3TextField.getText());
//            jobInfo.setMainClassName(this.mainClassNameTextField.getText());
//            stub.startjobSubmitOperation(jobInfo, new MyHandler());
//            this.hide();
//        } catch (AxisFault e) {
//            e.printStackTrace();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * hide the dialog (when user clicked on cancel)
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * show the dialog (when user clicked on invoke)
     */
    public void show() {
        this.dialog.show();
    }

//    class MyHandler extends AmazonEC2WebserviceCallbackHandler {
//
//        @Override
//        public void receiveResultjobSubmitOperation(JobSubmissionReceipt result) {
//            JOptionPane.showMessageDialog(null, "Job Submitted, ID: " + result.getJobFlowID(), "",
//                    JOptionPane.INFORMATION_MESSAGE);
//        }
//
//        @Override
//        public void receiveErrorjobSubmitOperation(java.lang.Exception e) {
//            JOptionPane.showMessageDialog(null, "Job Submit Failed!", "", JOptionPane.ERROR_MESSAGE);
//        }
//
//        @Override
//        public void receiveResultjobStatus(JobStatusResponse result) {
//            System.out.println("In call back, response is: " + result.getStatus());
//        }
//
//        @Override
//        public void receiveErrorjobStatus(java.lang.Exception e) {
//            throw new WorkflowRuntimeException(e);
//        }

//    }
}
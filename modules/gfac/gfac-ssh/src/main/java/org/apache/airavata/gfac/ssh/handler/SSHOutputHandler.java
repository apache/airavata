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
package org.apache.airavata.gfac.ssh.handler;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.core.utils.OutputUtils;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.util.GFACSSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.DataTransferDetails;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.TransferState;
import org.apache.airavata.model.workspace.experiment.TransferStatus;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SSHOutputHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(SSHOutputHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        String hostAddress = jobExecutionContext.getHostName();
        try {
            if (jobExecutionContext.getSecurityContext(hostAddress) == null) {
                GFACSSHUtils.addSecurityContext(jobExecutionContext);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                GFacUtils.saveErrorDetails(jobExecutionContext,  errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
            } catch (GFacException e1) {
                log.error(e1.getLocalizedMessage());
            }
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        }

        super.invoke(jobExecutionContext);
        DataTransferDetails detail = new DataTransferDetails();
        detail.setTransferDescription("Output data staging");
        TransferStatus status = new TransferStatus();

        Cluster cluster = null;
        try {
             cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(hostAddress)).getPbsCluster();
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }

            // Get the Stdouts and StdErrs
            String timeStampedExperimentID = GFacUtils.createUniqueNameWithDate(jobExecutionContext.getExperimentID());

            TaskDetails taskData = jobExecutionContext.getTaskData();
            String outputDataDir = ServerSettings.getSetting(Constants.OUTPUT_DATA_DIR);
            File localStdOutFile;
            File localStdErrFile;
            //FIXME: AdvancedOutput is remote location and third party transfer should work to make this work 
//            if (taskData.getAdvancedOutputDataHandling() != null) {
//                outputDataDir = taskData.getAdvancedOutputDataHandling().getOutputDataDir();
//            }
            if (outputDataDir == null || outputDataDir.equals("")) {
                outputDataDir = File.separator + "tmp";
            }
            outputDataDir = outputDataDir + File.separator + jobExecutionContext.getExperimentID() + "-" + jobExecutionContext.getTaskData().getTaskID();
            (new File(outputDataDir)).mkdirs();


            localStdOutFile = new File(outputDataDir + File.separator + timeStampedExperimentID + "stdout");
            localStdErrFile = new File(outputDataDir + File.separator + timeStampedExperimentID + "stderr");
//            cluster.makeDirectory(outputDataDir);
            int i = 0;
            String stdOutStr = "";
            while (stdOutStr.isEmpty()) {
                try {
                    cluster.scpFrom(jobExecutionContext.getStandardOutput(), localStdOutFile.getAbsolutePath());
                    stdOutStr = GFacUtils.readFileToString(localStdOutFile.getAbsolutePath());
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                    Thread.sleep(2000);
                }
                i++;
                if (i == 3) break;
            }
            Thread.sleep(1000);
            cluster.scpFrom(jobExecutionContext.getStandardError(), localStdErrFile.getAbsolutePath());
            Thread.sleep(1000);

            String stdErrStr = GFacUtils.readFileToString(localStdErrFile.getAbsolutePath());
            status.setTransferState(TransferState.STDOUT_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDOUT:" + localStdOutFile.getAbsolutePath());
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());

            status.setTransferState(TransferState.STDERROR_DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDERR:" + localStdErrFile.getAbsolutePath());
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());


            List<OutputDataObjectType> outputArray = new ArrayList<OutputDataObjectType>();
            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            Set<String> keys = output.keySet();
            for (String paramName : keys) {
                OutputDataObjectType actualParameter = (OutputDataObjectType) output.get(paramName);
                if (DataType.URI == actualParameter.getType()) {
                    List<String> outputList = null;
                    int retry = 3;
                    while (retry > 0) {
                        outputList = cluster.listDirectory(jobExecutionContext.getOutputDir());
                        if (outputList.size() > 0) {
                            break;
                        }
                        retry--;
                        Thread.sleep(2000);
                    }

                    if (outputList.size() == 0 || outputList.get(0).isEmpty() || outputList.size() > 1) {
                        OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr, outputArray);
                        Set<String> strings = output.keySet();
                        outputArray.clear();
                        for (String key : strings) {
                            OutputDataObjectType actualParameter1 = (OutputDataObjectType) output.get(key);
                            if (DataType.URI == actualParameter1.getType()) {
                                String downloadFile = actualParameter1.getValue();
                                cluster.scpFrom(downloadFile, outputDataDir);
                                String fileName = downloadFile.substring(downloadFile.lastIndexOf(File.separatorChar) + 1, downloadFile.length());
                                String localFile = outputDataDir + File.separator + fileName;
                                jobExecutionContext.addOutputFile(localFile);
                                actualParameter1.setValue(localFile);
                                OutputDataObjectType dataObjectType = new OutputDataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setName(key);
                                dataObjectType.setType(DataType.URI);
                                outputArray.add(dataObjectType);
                            }else if (DataType.STDOUT == actualParameter.getType()) {
                                String fileName = localStdOutFile.getName();
                                String localFile = outputDataDir + File.separator + fileName;
                                jobExecutionContext.addOutputFile(localFile);
                                actualParameter.setValue(localFile);
                                OutputDataObjectType dataObjectType = new OutputDataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setName(key);
                                dataObjectType.setType(DataType.STDOUT);
                                outputArray.add(dataObjectType);
                            }else if (DataType.STDERR == actualParameter.getType()) {
                                String fileName = localStdErrFile.getName();
                                String localFile = outputDataDir + File.separator + fileName;
                                jobExecutionContext.addOutputFile(localFile);
                                actualParameter.setValue(localFile);
                                OutputDataObjectType dataObjectType = new OutputDataObjectType();
                                dataObjectType.setValue(localFile);
                                dataObjectType.setName(key);
                                dataObjectType.setType(DataType.STDERR);
                                outputArray.add(dataObjectType);
                            }
                        }
                        break;
                    } else if (outputList.size() == 1) {//FIXME: Ultrascan case
                        String valueList = outputList.get(0);
                        cluster.scpFrom(jobExecutionContext.getOutputDir() + File.separator + valueList, outputDataDir);
                        String outputPath = outputDataDir + File.separator + valueList;
                        jobExecutionContext.addOutputFile(outputPath);
                        actualParameter.setValue(outputPath);
                        OutputDataObjectType dataObjectType = new OutputDataObjectType();
                        dataObjectType.setValue(outputPath);
                        dataObjectType.setName(paramName);
                        dataObjectType.setType(DataType.URI);
                        outputArray.add(dataObjectType);
                    }
                } else {
                    OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr, outputArray);
                }
            }
            if (outputArray == null || outputArray.isEmpty()) {
                log.error("Empty Output returned from the Application, Double check the application and ApplicationDescriptor output Parameter Names");
                if (jobExecutionContext.getTaskData().getAdvancedOutputDataHandling() == null) {
                    throw new GFacHandlerException(
                            "Empty Output returned from the Application, Double check the application"
                                    + "and ApplicationDescriptor output Parameter Names");
                }
            }
            jobExecutionContext.setStandardError(localStdErrFile.getAbsolutePath());
            jobExecutionContext.setStandardOutput(localStdOutFile.getAbsolutePath());
            jobExecutionContext.setOutputDir(outputDataDir);
            status.setTransferState(TransferState.DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription(outputDataDir);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
            registry.add(ChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());

        } catch (Exception e) {
            try {
                status.setTransferState(TransferState.FAILED);
                detail.setTransferStatus(status);
                registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                GFacUtils.saveErrorDetails(jobExecutionContext,  errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.FILE_SYSTEM_FAILURE);
            } catch (Exception e1) {
                throw new GFacHandlerException("Error persisting status", e1, e1.getLocalizedMessage());
            }
            throw new GFacHandlerException("Error in retrieving results", e);
        }

    }

    @Override
    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        // TODO: Auto generated method body.
    }

    public void initProperties(Properties properties) throws GFacHandlerException {

    }
}

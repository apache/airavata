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
package org.apache.airavata.gfac.gsissh.handler;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.core.utils.OutputUtils;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.gsissh.util.GFACGSISSHUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GSISSHOutputHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(GSISSHOutputHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        if(jobExecutionContext.getApplicationContext().getHostDescription().getType() instanceof GsisshHostType) { // this is because we don't have the right jobexecution context
            // so attempting to get it from the registry
            if (Constants.PUSH.equals(((GsisshHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType()).getMonitorMode())) {
                log.warn("During the out handler chain jobExecution context came null, so trying to handler");
                ApplicationDescription applicationDeploymentDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
                TaskDetails taskData = null;
                try {
                    taskData = (TaskDetails) jobExecutionContext.getRegistry().get(RegistryModelType.TASK_DETAIL, jobExecutionContext.getTaskData().getTaskID());
                } catch (RegistryException e) {
                    log.error("Error retrieving job details from Registry");
                    throw new GFacHandlerException("Error retrieving job details from Registry", e);
                }
                JobDetails jobDetails = taskData.getJobDetailsList().get(0);
                String jobDescription = jobDetails.getJobDescription();
                if (jobDescription != null) {
                    JobDescriptor jobDescriptor = null;
                    try {
                        jobDescriptor = JobDescriptor.fromXML(jobDescription);
                    } catch (XmlException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    applicationDeploymentDescription.getType().setScratchWorkingDirectory(
                            jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getWorkingDirectory());
                    applicationDeploymentDescription.getType().setInputDataDirectory(jobDescriptor.getInputDirectory());
                    applicationDeploymentDescription.getType().setOutputDataDirectory(jobDescriptor.getOutputDirectory());
                    applicationDeploymentDescription.getType().setStandardError(jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getStandardErrorFile());
                    applicationDeploymentDescription.getType().setStandardOutput(jobDescriptor.getJobDescriptorDocument().getJobDescriptor().getStandardOutFile());
                }
            }
        }
        try {
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) == null) {

                GFACGSISSHUtils.addSecurityContext(jobExecutionContext);
            }
        } catch (ApplicationSettingsException e) {
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        } catch (GFacException e) {
            log.error(e.getMessage());
            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
        }
        super.invoke(jobExecutionContext);
        DataTransferDetails detail = new DataTransferDetails();
        TransferStatus status = new TransferStatus();

        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext()
                .getApplicationDeploymentDescription().getType();
        try {
            Cluster cluster = null;
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) != null) {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            } else {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            }
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }

            // Get the Stdouts and StdErrs
            String timeStampedServiceName = GFacUtils.createUniqueNameForService(jobExecutionContext.getServiceName());

            TaskDetails taskData = jobExecutionContext.getTaskData();
            String outputDataDir = null;
            File localStdOutFile;
            File localStdErrFile;

            if (taskData.getAdvancedOutputDataHandling() != null) {
                outputDataDir = taskData.getAdvancedOutputDataHandling().getOutputDataDir();
            }
            if (outputDataDir == null) {
                outputDataDir = File.separator + "tmp";
            }
            outputDataDir = outputDataDir + File.separator + jobExecutionContext.getExperimentID() + "-" + jobExecutionContext.getTaskData().getTaskID();
            (new File(outputDataDir)).mkdirs();


            localStdOutFile = new File(outputDataDir + File.separator + timeStampedServiceName + "stdout");
            localStdErrFile = new File(outputDataDir + File.separator + timeStampedServiceName + "stderr");
//            cluster.makeDirectory(outputDataDir);
            cluster.scpFrom(app.getStandardOutput(), localStdOutFile.getAbsolutePath());
            Thread.sleep(1000);
            cluster.scpFrom(app.getStandardError(), localStdErrFile.getAbsolutePath());
            Thread.sleep(1000);

            String stdOutStr = GFacUtils.readFileToString(localStdOutFile.getAbsolutePath());
            String stdErrStr = GFacUtils.readFileToString(localStdErrFile.getAbsolutePath());
            status.setTransferState(TransferState.COMPLETE);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDOUT:" + stdOutStr);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());

            status.setTransferState(TransferState.COMPLETE);
            detail.setTransferStatus(status);
            detail.setTransferDescription("STDERR:" + stdErrStr);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());


            List<DataObjectType> outputArray = new ArrayList<DataObjectType>();
            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            Set<String> keys = output.keySet();
            for (String paramName : keys) {
                ActualParameter actualParameter = (ActualParameter) output.get(paramName);
                if ("URI".equals(actualParameter.getType().getType().toString())) {

                    List<String> outputList = cluster.listDirectory(app.getOutputDataDirectory());
                    if (outputList.size() == 0 || outputList.get(0).isEmpty()) {
                        OutputUtils.fillOutputFromStdout1(output, stdOutStr, stdErrStr, outputArray);
                        break;
                    } else {
                        String valueList = outputList.get(0);
                        cluster.scpFrom(app.getOutputDataDirectory() + File.separator + valueList, outputDataDir);
                        jobExecutionContext.addOutputFile(outputDataDir + File.separator + valueList);
                        DataObjectType dataObjectType = new DataObjectType();
                        dataObjectType.setValue(valueList);
                        dataObjectType.setKey(paramName);
                        dataObjectType.setType(DataType.URI);
                        outputArray.add(dataObjectType);
                    }
                } else {
                    OutputUtils.fillOutputFromStdout1(output, stdOutStr,stdErrStr, outputArray);
                    break;
                }
            }
            if (outputArray == null || outputArray.isEmpty()) {
                throw new GFacHandlerException(
                        "Empty Output returned from the Application, Double check the application"
                                + "and ApplicationDescriptor output Parameter Names");
            }
            app.setStandardError(localStdErrFile.getAbsolutePath());
            app.setStandardOutput(localStdOutFile.getAbsolutePath());
            app.setOutputDataDirectory(outputDataDir);
            status.setTransferState(TransferState.DOWNLOAD);
            detail.setTransferStatus(status);
            detail.setTransferDescription(outputDataDir);
            registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
            registry.add(ChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());
        } catch (XmlException e) {
            throw new GFacHandlerException("Cannot read output:" + e.getMessage(), e);
        } catch (ConnectionException e) {
            throw new GFacHandlerException(e.getMessage(), e);
        } catch (TransportException e) {
            throw new GFacHandlerException(e.getMessage(), e);
        } catch (IOException e) {
            throw new GFacHandlerException(e.getMessage(), e);
        } catch (Exception e) {
            try {
                status.setTransferState(TransferState.FAILED);
                detail.setTransferStatus(status);
                detail.setTransferDescription(e.getLocalizedMessage());
                registry.add(ChildDataType.DATA_TRANSFER_DETAIL, detail, jobExecutionContext.getTaskData().getTaskID());
                GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.FILE_SYSTEM_FAILURE);
            } catch (Exception e1) {
                throw new GFacHandlerException("Error persisting status", e1, e1.getLocalizedMessage());
            }
            throw new GFacHandlerException("Error in retrieving results", e);
        }

    }

    public void initProperties(Properties properties) throws GFacHandlerException {

    }
}

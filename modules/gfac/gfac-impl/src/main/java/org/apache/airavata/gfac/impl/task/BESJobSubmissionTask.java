///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//
//package org.apache.airavata.gfac.impl.task;
//
//import de.fzj.unicore.bes.client.FactoryClient;
//import de.fzj.unicore.uas.client.StorageClient;
//import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
//import eu.unicore.util.httpclient.DefaultClientConfiguration;
//import org.apache.airavata.gfac.core.GFacUtils;
//import org.apache.airavata.gfac.core.context.ProcessContext;
//import org.apache.airavata.gfac.core.context.TaskContext;
//import org.apache.airavata.gfac.core.task.JobSubmissionTask;
//import org.apache.airavata.gfac.core.task.TaskException;
//import org.apache.airavata.gfac.impl.task.utils.bes.DataTransferrer;
//import org.apache.airavata.gfac.impl.task.utils.bes.JSDLGenerator;
//import org.apache.airavata.gfac.impl.task.utils.bes.StorageCreator;
//import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
//import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
//import org.apache.airavata.model.job.JobModel;
//import org.apache.airavata.model.status.JobState;
//import org.apache.airavata.model.status.JobStatus;
//import org.apache.airavata.model.status.TaskStatus;
//import org.apache.airavata.model.task.TaskTypes;
//import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityDocument;
//import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseDocument;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3.x2005.x08.addressing.EndpointReferenceType;
//
//import java.util.Calendar;
//import java.util.Map;
//
//public class BESJobSubmissionTask implements JobSubmissionTask {
//    private static final Logger log = LoggerFactory.getLogger(BESJobSubmissionTask.class);
//    private DefaultClientConfiguration secProperties;
//
//    private String jobId;
//    @Override
//    public JobStatus cancel(TaskContext taskcontext) throws TaskException {
//        return null;
//    }
//
//    @Override
//    public void init(Map<String, String> propertyMap) throws TaskException {
//    }
//
//    @Override
//    public TaskStatus execute(TaskContext taskContext) {
//        StorageClient sc = null;
//        try {
//            ProcessContext processContext = taskContext.getParentProcessContext();
//            JobSubmissionProtocol protocol = processContext.getJobSubmissionProtocol();
//            String interfaceId = processContext.getApplicationInterfaceDescription().getApplicationInterfaceId();
//            String factoryUrl = null;
//            if (protocol.equals(JobSubmissionProtocol.UNICORE)) {
//                UnicoreJobSubmission unicoreJobSubmission = GFacUtils.getUnicoreJobSubmission(interfaceId);
//                factoryUrl = unicoreJobSubmission.getUnicoreEndPointURL();
//            }
//            EndpointReferenceType eprt = EndpointReferenceType.Factory.newInstance();
//            eprt.addNewAddress().setStringValue(factoryUrl);
//            String userDN = processContext.getProcessModel().getUserDn();
//
//            CreateActivityDocument cad = CreateActivityDocument.Factory.newInstance();
//
//            // create storage
//            StorageCreator storageCreator = new StorageCreator(secProperties, factoryUrl, 5, null);
//            sc = storageCreator.createStorage();
//
//            JobDefinitionType jobDefinition = JSDLGenerator.buildJSDLInstance(processContext, sc.getUrl()).getJobDefinition();
//            cad.addNewCreateActivity().addNewActivityDocument().setJobDefinition(jobDefinition);
//
//            log.info("Submitted JSDL: " + jobDefinition.getJobDescription());
//
//            // upload files if any
//            DataTransferrer dt = new DataTransferrer(processContext, sc);
//            dt.uploadLocalFiles();
//
//            JobModel jobDetails = new JobModel();
//            FactoryClient factory = new FactoryClient(eprt, secProperties);
//
//            log.info(String.format("Activity Submitting to %s ... \n",
//                    factoryUrl));
//            monitorPublisher.publish(new StartExecutionEvent());
//            CreateActivityResponseDocument response = factory.createActivity(cad);
//            log.info(String.format("Activity Submitted to %s \n", factoryUrl));
//
//            EndpointReferenceType activityEpr = response.getCreateActivityResponse().getActivityIdentifier();
//
//            log.info("Activity : " + activityEpr.getAddress().getStringValue() + " Submitted.");
//
//            // factory.waitWhileActivityIsDone(activityEpr, 1000);
//            jobId = WSUtilities.extractResourceID(activityEpr);
//            if (jobId == null) {
//                jobId = new Long(Calendar.getInstance().getTimeInMillis())
//                        .toString();
//            }
//            log.info("JobID: " + jobId);
//            jobDetails.setJobId(jobId);
//            jobDetails.setJobDescription(activityEpr.toString());
//            jobDetails.setJobStatus(new JobStatus(JobState.SUBMITTED));
//            processContext.setJobModel(jobDetails);
//            GFacUtils.saveJobStatus(processContext, jobDetails);
//            log.info(formatStatusMessage(activityEpr.getAddress()
//                    .getStringValue(), factory.getActivityStatus(activityEpr)
//                    .toString()));
//
//            waitUntilDone(eprt, activityEpr, jobDetails, secProperties);
//
//            ActivityStatusType activityStatus = null;
//            activityStatus = getStatus(factory, activityEpr);
//            log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(), activityStatus.getState().toString()));
//            ActivityClient activityClient;
//            activityClient = new ActivityClient(activityEpr, secProperties);
//            // now use the activity working directory property
//            dt.setStorageClient(activityClient.getUspaceClient());
//
//            if ((activityStatus.getState() == ActivityStateEnumeration.FAILED)) {
//                String error = activityStatus.getFault().getFaultcode()
//                        .getLocalPart()
//                        + "\n"
//                        + activityStatus.getFault().getFaultstring()
//                        + "\n EXITCODE: " + activityStatus.getExitCode();
//                log.info(error);
//
//                JobState applicationJobStatus = JobState.FAILED;
//                sendNotification(jobExecutionContext,applicationJobStatus);
//                GFacUtils.updateJobStatus(jobExecutionContext, jobDetails, applicationJobStatus);
//                try {Thread.sleep(5000);} catch (InterruptedException e) {}
//
//                //What if job is failed before execution and there are not stdouts generated yet?
//                log.debug("Downloading any standard output and error files, if they were produced.");
//                dt.downloadStdOuts();
//
//            } else if (activityStatus.getState() == ActivityStateEnumeration.CANCELLED) {
//                JobState applicationJobStatus = JobState.CANCELED;
//                sendNotification(jobExecutionContext,applicationJobStatus);
//                GFacUtils.updateJobStatus(jobExecutionContext, jobDetails, applicationJobStatus);
//                throw new GFacProviderException(
//                        jobExecutionContext.getExperimentID() + "Job Canceled");
//            } else if (activityStatus.getState() == ActivityStateEnumeration.FINISHED) {
//                try {
//                    Thread.sleep(5000);
//                    JobState applicationJobStatus = JobState.COMPLETE;
//                    sendNotification(jobExecutionContext,applicationJobStatus);
//
//                } catch (InterruptedException e) {
//                }
//                if (activityStatus.getExitCode() == 0) {
//                    dt.downloadRemoteFiles();
//                } else {
//                    dt.downloadStdOuts();
//                }
//            }
//
//            dt.publishFinalOutputs();
//        } catch (AppCatalogException e) {
//            log.error("Error while retrieving UNICORE job submission..");
//            throw new GFacProviderException("Error while retrieving UNICORE job submission..", e);
//        } catch (Exception e) {
//            log.error("Cannot create storage..");
//            throw new GFacProviderException("Cannot create storage..", e);
//        }
//    }
//
//    @Override
//    public TaskStatus recover(TaskContext taskContext) {
//        return null;
//    }
//
//    @Override
//    public TaskTypes getType() {
//        return null;
//    }
//}

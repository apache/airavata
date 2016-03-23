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

package org.apache.airavata.gfac.impl.task;

import de.fzj.unicore.bes.client.ActivityClient;
import de.fzj.unicore.bes.client.FactoryClient;
import de.fzj.unicore.bes.faults.UnknownActivityIdentifierFault;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.util.httpclient.DefaultClientConfiguration;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.task.utils.bes.*;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.core.experiment.catalog.model.UserConfigurationData;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.xmlbeans.XmlCursor;
import org.ggf.schemas.bes.x2006.x08.besFactory.*;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.util.Calendar;
import java.util.Map;

public class BESJobSubmissionTask implements JobSubmissionTask {
    private static final Logger log = LoggerFactory.getLogger(BESJobSubmissionTask.class);
    private DefaultClientConfiguration secProperties;

    private String jobId;
    @Override
    public JobStatus cancel(TaskContext taskcontext) throws TaskException {
        return null;
    }

    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {
    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        StorageClient sc = null;

        //TODO - initialize securityContext secProperties
        try {
            if (secProperties == null) {
                secProperties = getSecurityConfig(taskContext.getParentProcessContext());
            }  // try secProperties = secProperties.clone() if we can't use already initialized ClientConfigurations.
        } catch (GFacException e) {
            String msg = "Unicorn security context initialization error";
            log.error(msg, e);
            taskStatus.setState(TaskState.FAILED);
            taskStatus.setReason(msg);
            return taskStatus;
        }

        try {
            ProcessContext processContext = taskContext.getParentProcessContext();
            JobSubmissionProtocol protocol = processContext.getJobSubmissionProtocol();
            JobSubmissionInterface jobSubmissionInterface = GFacUtils.getPreferredJobSubmissionInterface(processContext);
            String factoryUrl = null;
            if (protocol.equals(JobSubmissionProtocol.UNICORE)) {
                UnicoreJobSubmission unicoreJobSubmission = GFacUtils.getUnicoreJobSubmission(
                        jobSubmissionInterface.getJobSubmissionInterfaceId());
                factoryUrl = unicoreJobSubmission.getUnicoreEndPointURL();
            }
            EndpointReferenceType eprt = EndpointReferenceType.Factory.newInstance();
            eprt.addNewAddress().setStringValue(factoryUrl);
            String userDN = processContext.getProcessModel().getUserDn();

            CreateActivityDocument cad = CreateActivityDocument.Factory.newInstance();

            // create storage
            StorageCreator storageCreator = new StorageCreator(secProperties, factoryUrl, 5, null);
            sc = storageCreator.createStorage();

            JobDefinitionType jobDefinition = JSDLGenerator.buildJSDLInstance(processContext, sc.getUrl()).getJobDefinition();
            cad.addNewCreateActivity().addNewActivityDocument().setJobDefinition(jobDefinition);

            log.info("Submitted JSDL: " + jobDefinition.getJobDescription());

            // upload files if any
            DataTransferrer dt = new DataTransferrer(processContext, sc);
            dt.uploadLocalFiles();

            JobModel jobDetails = new JobModel();
            FactoryClient factory = new FactoryClient(eprt, secProperties);

            log.info(String.format("Activity Submitting to %s ... \n",
                    factoryUrl));
            CreateActivityResponseDocument response = factory.createActivity(cad);
            log.info(String.format("Activity Submitted to %s \n", factoryUrl));

            EndpointReferenceType activityEpr = response.getCreateActivityResponse().getActivityIdentifier();

            log.info("Activity : " + activityEpr.getAddress().getStringValue() + " Submitted.");

            // factory.waitWhileActivityIsDone(activityEpr, 1000);
            jobId = WSUtilities.extractResourceID(activityEpr);
            if (jobId == null) {
                jobId = new Long(Calendar.getInstance().getTimeInMillis())
                        .toString();
            }
            log.info("JobID: " + jobId);
            jobDetails.setJobId(jobId);
            jobDetails.setJobDescription(activityEpr.toString());
            jobDetails.setJobStatus(new JobStatus(JobState.SUBMITTED));
            processContext.setJobModel(jobDetails);
            GFacUtils.saveJobStatus(processContext, jobDetails);
            log.info(formatStatusMessage(activityEpr.getAddress()
                    .getStringValue(), factory.getActivityStatus(activityEpr)
                    .toString()));

            waitUntilDone(eprt, activityEpr, processContext, secProperties);

            ActivityStatusType activityStatus = null;
            activityStatus = getStatus(factory, activityEpr);
            log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(), activityStatus.getState().toString()));
            ActivityClient activityClient;
            activityClient = new ActivityClient(activityEpr, secProperties);
            // now use the activity working directory property
            dt.setStorageClient(activityClient.getUspaceClient());

            if ((activityStatus.getState() == ActivityStateEnumeration.FAILED)) {
                String error = activityStatus.getFault().getFaultcode()
                        .getLocalPart()
                        + "\n"
                        + activityStatus.getFault().getFaultstring()
                        + "\n EXITCODE: " + activityStatus.getExitCode();
                log.info(error);

                JobState applicationJobStatus = JobState.FAILED;
                jobDetails.setJobStatus(new JobStatus(applicationJobStatus));
                sendNotification(processContext, jobDetails);
                try {Thread.sleep(5000);} catch (InterruptedException e) {}

                //What if job is failed before execution and there are not stdouts generated yet?
                log.debug("Downloading any standard output and error files, if they were produced.");
                dt.downloadStdOuts();

            } else if (activityStatus.getState() == ActivityStateEnumeration.CANCELLED) {
                JobState applicationJobStatus = JobState.CANCELED;
                jobDetails.setJobStatus(new JobStatus(applicationJobStatus));
                GFacUtils.saveJobStatus(processContext, jobDetails);
                throw new GFacException(
                        processContext.getExperimentId() + "Job Canceled");
            } else if (activityStatus.getState() == ActivityStateEnumeration.FINISHED) {
                try {
                    Thread.sleep(5000);
                    JobState applicationJobStatus = JobState.COMPLETE;
                    jobDetails.setJobStatus(new JobStatus(applicationJobStatus));
                    GFacUtils.saveJobStatus(processContext, jobDetails);

                } catch (InterruptedException e) {
                }
                if (activityStatus.getExitCode() == 0) {
                    dt.downloadRemoteFiles();
                } else {
                    dt.downloadStdOuts();
                }
            }

            dt.publishFinalOutputs();
            taskStatus.setState(TaskState.COMPLETED);
        } catch (AppCatalogException e) {
            log.error("Error while retrieving UNICORE job submission..");
            taskStatus.setState(TaskState.FAILED);
        } catch (Exception e) {
            log.error("Cannot create storage..", e);
            taskStatus.setState(TaskState.FAILED);
        }

        return taskStatus;
    }

    private DefaultClientConfiguration getSecurityConfig(ProcessContext pc) throws GFacException {
        DefaultClientConfiguration clientConfig = null;
        try {
            UNICORESecurityContext unicoreSecurityContext = SecurityUtils.getSecurityContext(pc);
            UserConfigurationDataModel userConfigDataModel = (UserConfigurationDataModel) pc.getExperimentCatalog().
                    get(ExperimentCatalogModelType.USER_CONFIGURATION_DATA, pc.getExperimentId());
            // FIXME - remove following setter lines, and use original value comes with user configuration data model.
            userConfigDataModel.setGenerateCert(true);
            userConfigDataModel.setUserDN("CN=swus3, O=Ultrascan Gateway, C=DE");
            if (userConfigDataModel.isGenerateCert()) {
                clientConfig = unicoreSecurityContext.getDefaultConfiguration(false, userConfigDataModel);
            } else {
                clientConfig = unicoreSecurityContext.getDefaultConfiguration(false);
            }
        } catch (RegistryException e) {
            throw new GFacException("Error! reading user configuration data from registry", e);
        } catch (ApplicationSettingsException e) {
            throw new GFacException("Error! retrieving default client configurations", e);
        }

        return clientConfig;
    }

    protected String formatStatusMessage(String activityUrl, String status) {
        return String.format("Activity %s is %s.\n", activityUrl, status);
    }

    protected void waitUntilDone(EndpointReferenceType factoryEpr, EndpointReferenceType activityEpr, ProcessContext processContext, DefaultClientConfiguration secProperties) throws Exception {

        try {
            FactoryClient factoryClient = new FactoryClient(factoryEpr, secProperties);
            JobState applicationJobStatus = null;

            while ((factoryClient.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED)
                    && (factoryClient.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)
                    && (factoryClient.getActivityStatus(activityEpr) != ActivityStateEnumeration.CANCELLED)
                    && (applicationJobStatus != JobState.COMPLETE)) {

                ActivityStatusType activityStatus = getStatus(factoryClient, activityEpr);
                applicationJobStatus = getApplicationJobStatus(activityStatus);

                sendNotification(processContext,processContext.getJobModel());

                // GFacUtils.updateApplicationJobStatus(jobExecutionContext,jobId,
                // applicationJobStatus);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
                continue;
            }
        } catch(Exception e) {
            log.error("Error monitoring job status..");
            throw e;
        }
    }

    private void sendNotification(ProcessContext processContext,  JobModel jobModel) throws GFacException {
        GFacUtils.saveJobStatus(processContext, jobModel);
    }

    @Override
    public TaskStatus recover(TaskContext taskContext) {
        return execute(taskContext);
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.JOB_SUBMISSION;
    }

    protected ActivityStatusType getStatus(FactoryClient fc, EndpointReferenceType activityEpr)
            throws UnknownActivityIdentifierFault {

        GetActivityStatusesDocument stats = GetActivityStatusesDocument.Factory
                .newInstance();

        stats.addNewGetActivityStatuses().setActivityIdentifierArray(
                new EndpointReferenceType[] { activityEpr });

        GetActivityStatusesResponseDocument resDoc = fc
                .getActivityStatuses(stats);

        ActivityStatusType activityStatus = resDoc
                .getGetActivityStatusesResponse().getResponseArray()[0]
                .getActivityStatus();
        return activityStatus;
    }

    private JobState getApplicationJobStatus(ActivityStatusType activityStatus) {
        if (activityStatus == null) {
            return JobState.UNKNOWN;
        }
        ActivityStateEnumeration.Enum state = activityStatus.getState();
        String status = null;
        XmlCursor acursor = activityStatus.newCursor();
        try {
            if (acursor.toFirstChild()) {
                if (acursor.getName().getNamespaceURI()
                        .equals("http://schemas.ogf.org/hpcp/2007/01/fs")) {
                    status = acursor.getName().getLocalPart();
                }
            }
            if (status != null) {
                if (status.equalsIgnoreCase("Queued")
                        || status.equalsIgnoreCase("Starting")
                        || status.equalsIgnoreCase("Ready")) {
                    return JobState.QUEUED;
                } else if (status.equalsIgnoreCase("Staging-In")) {
                    return JobState.SUBMITTED;
                } else if (status.equalsIgnoreCase("FINISHED")) {
                    return JobState.COMPLETE;
                }else if(status.equalsIgnoreCase("Staging-Out")){
                    return JobState.ACTIVE;
                }
                else if (status.equalsIgnoreCase("Executing")) {
                    return JobState.ACTIVE;
                } else if (status.equalsIgnoreCase("FAILED")) {
                    return JobState.FAILED;
                } else if (status.equalsIgnoreCase("CANCELLED")) {
                    return JobState.CANCELED;
                }
            } else {
                if (ActivityStateEnumeration.CANCELLED.equals(state)) {
                    return JobState.CANCELED;
                } else if (ActivityStateEnumeration.FAILED.equals(state)) {
                    return JobState.FAILED;
                } else if (ActivityStateEnumeration.FINISHED.equals(state)) {
                    return JobState.COMPLETE;
                } else if (ActivityStateEnumeration.RUNNING.equals(state)) {
                    return JobState.ACTIVE;
                }
            }
        } finally {
            if (acursor != null)
                acursor.dispose();
        }
        return JobState.UNKNOWN;
    }

    /**
     * EndpointReference need to be saved to make cancel work.
     *
     * @param processContext
     * @throws GFacException
     */
    public boolean cancelJob(ProcessContext processContext) throws GFacException {
        try {
            String activityEpr = processContext.getJobModel().getJobDescription();
            // initSecurityProperties(processContext);
            EndpointReferenceType eprt = EndpointReferenceType.Factory
                    .parse(activityEpr);
            JobSubmissionProtocol protocol = processContext.getJobSubmissionProtocol();
            String interfaceId = processContext.getApplicationInterfaceDescription().getApplicationInterfaceId();
            String factoryUrl = null;
            if (protocol.equals(JobSubmissionProtocol.UNICORE)) {
                UnicoreJobSubmission unicoreJobSubmission = GFacUtils.getUnicoreJobSubmission(interfaceId);
                factoryUrl = unicoreJobSubmission.getUnicoreEndPointURL();
            }
            EndpointReferenceType epr = EndpointReferenceType.Factory
                    .newInstance();
            epr.addNewAddress().setStringValue(factoryUrl);

            FactoryClient factory = new FactoryClient(epr, secProperties);
            factory.terminateActivity(eprt);
            return true;
        } catch (Exception e) {
            throw new GFacException(e.getLocalizedMessage(), e);
        }

    }
}

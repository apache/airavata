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
package org.apache.airavata.gfac.bes.provider.impl;

import java.util.Calendar;
import java.util.Map;

import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.bes.security.UNICORESecurityContext;
import org.apache.airavata.gfac.bes.security.X509SecurityContext;
import org.apache.airavata.gfac.bes.utils.BESConstants;
import org.apache.airavata.gfac.bes.utils.DataTransferrer;
import org.apache.airavata.gfac.bes.utils.JSDLGenerator;
import org.apache.airavata.gfac.bes.utils.SecurityUtils;
import org.apache.airavata.gfac.bes.utils.StorageCreator;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.AbstractProvider;
import org.apache.airavata.gfac.core.provider.GFacProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeRequestEvent;
import org.apache.airavata.model.experiment.JobDetails;
import org.apache.airavata.model.experiment.JobState;
import org.apache.xmlbeans.XmlCursor;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration.Enum;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.bes.client.ActivityClient;
import de.fzj.unicore.bes.client.FactoryClient;
import de.fzj.unicore.bes.faults.UnknownActivityIdentifierFault;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.util.httpclient.DefaultClientConfiguration;

public class BESProvider extends AbstractProvider implements GFacProvider,
		BESConstants {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private DefaultClientConfiguration secProperties;

	private String jobId;

	public void initialize(JobExecutionContext jobExecutionContext)
			throws GFacProviderException, GFacException {
		log.info("Initializing UNICORE Provider..");
		super.initialize(jobExecutionContext);
		secProperties = (DefaultClientConfiguration) jobExecutionContext.getProperty(PROP_CLIENT_CONF);
		if (secProperties != null) {
			secProperties = secProperties.clone();
			return;
		}
		SecurityUtils.addSecurityContext(jobExecutionContext);
		UNICORESecurityContext unicoreContext = (UNICORESecurityContext) jobExecutionContext.getSecurityContext(X509SecurityContext.X509_SECURITY_CONTEXT);
		try{
			if (jobExecutionContext.getExperiment()
					.getUserConfigurationData().isGenerateCert())  {
				secProperties = unicoreContext
						.getDefaultConfiguration(false, jobExecutionContext
								.getExperiment().getUserConfigurationData());
			}else {
				secProperties = unicoreContext.getDefaultConfiguration(false);
			}
				
		} catch (ApplicationSettingsException e) {
			throw new GFacProviderException("Error initializing security of Unicore provider", e);
		}
		if (log.isDebugEnabled()) {
			log.debug("Security properties initialized.");
		}
	}

	public void execute(JobExecutionContext jobExecutionContext)
			throws GFacProviderException, GFacException {

        StorageClient sc = null;
        try {
            JobSubmissionInterface preferredJobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
            JobSubmissionProtocol protocol = preferredJobSubmissionInterface.getJobSubmissionProtocol();
            String interfaceId = preferredJobSubmissionInterface.getJobSubmissionInterfaceId();
            String factoryUrl = null;
            if (protocol.equals(JobSubmissionProtocol.UNICORE)) {
                UnicoreJobSubmission unicoreJobSubmission = GFacUtils.getUnicoreJobSubmission(interfaceId);
                factoryUrl = unicoreJobSubmission.getUnicoreEndPointURL();
            }
            EndpointReferenceType eprt = EndpointReferenceType.Factory.newInstance();
            eprt.addNewAddress().setStringValue(factoryUrl);
            String userDN = getUserName(jobExecutionContext);

            // TODO: to be removed
            if (userDN == null || userDN.equalsIgnoreCase("admin")) {
                userDN = "CN=zdv575, O=Ultrascan Gateway, C=DE";
            }
            CreateActivityDocument cad = CreateActivityDocument.Factory.newInstance();
            JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory.newInstance();

            // create storage
            StorageCreator storageCreator = new StorageCreator(secProperties, factoryUrl, 5, null);
            sc = storageCreator.createStorage();

            JobDefinitionType jobDefinition = JSDLGenerator.buildJSDLInstance(jobExecutionContext, sc.getUrl()).getJobDefinition();
            cad.addNewCreateActivity().addNewActivityDocument().setJobDefinition(jobDefinition);
            log.info("JSDL" + jobDefDoc.toString());

            // upload files if any
            DataTransferrer dt = new DataTransferrer(jobExecutionContext, sc);
            dt.uploadLocalFiles();

            JobDetails jobDetails = new JobDetails();
            FactoryClient factory = new FactoryClient(eprt, secProperties);

            log.info("Activity Submitting to {} ... \n", factoryUrl));
            CreateActivityResponseDocument response = factory.createActivity(cad);
            log.info("Activity Submitted to {} \n", factoryUrl);

            EndpointReferenceType activityEpr = response.getCreateActivityResponse().getActivityIdentifier();

            log.info("Activity : " + activityEpr.getAddress().getStringValue() + " Submitted.");

            // factory.waitWhileActivityIsDone(activityEpr, 1000);
            jobId = WSUtilities.extractResourceID(activityEpr);
            if (jobId == null) {
                jobId = new Long(Calendar.getInstance().getTimeInMillis())
                        .toString();
            }
            log.info("JobID: " + jobId);
            jobDetails.setJobID(jobId);
            jobDetails.setJobDescription(activityEpr.toString());

            jobExecutionContext.setJobDetails(jobDetails);
            GFacUtils.saveJobStatus(jobExecutionContext, jobDetails, JobState.SUBMITTED);
            log.info(formatStatusMessage(activityEpr.getAddress()
                    .getStringValue(), factory.getActivityStatus(activityEpr)
                    .toString()));
            
            waitUntilDone(eprt, activityEpr, jobDetails, secProperties);

            ActivityStatusType activityStatus = null;
            activityStatus = getStatus(factory, activityEpr);
            log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(), activityStatus.getState().toString()));
            ActivityClient activityClient;
            activityClient = new ActivityClient(activityEpr, secProperties);
            dt.setStorageClient(activityClient.getUspaceClient());

            if ((activityStatus.getState() == ActivityStateEnumeration.FAILED)) {
                String error = activityStatus.getFault().getFaultcode()
                        .getLocalPart()
                        + "\n"
                        + activityStatus.getFault().getFaultstring()
                        + "\n EXITCODE: " + activityStatus.getExitCode();
                log.info(error);
  
                JobState applicationJobStatus = JobState.FAILED;
                sendNotification(jobExecutionContext,applicationJobStatus);
                GFacUtils.updateJobStatus(jobExecutionContext, jobDetails, applicationJobStatus);
                try {Thread.sleep(5000);} catch (InterruptedException e) {}
                
                //What if job is failed before execution and there are not stdouts generated yet?
                log.debug("Downloading any standard output and error files, if they were produced.");
                dt.downloadStdOuts();
                
            } else if (activityStatus.getState() == ActivityStateEnumeration.CANCELLED) {
                JobState applicationJobStatus = JobState.CANCELED;
                sendNotification(jobExecutionContext,applicationJobStatus);
                GFacUtils.updateJobStatus(jobExecutionContext, jobDetails, applicationJobStatus);
                throw new GFacProviderException(
                        jobExecutionContext.getExperimentID() + "Job Canceled");
            } else if (activityStatus.getState() == ActivityStateEnumeration.FINISHED) {
                try {
                    Thread.sleep(5000);
                    JobState applicationJobStatus = JobState.COMPLETE;
                    sendNotification(jobExecutionContext,applicationJobStatus);
                    
                } catch (InterruptedException e) {
                }
                if (activityStatus.getExitCode() == 0) {
                    dt.downloadRemoteFiles();
                } else {
                    dt.downloadStdOuts();
                }
            }
        } catch (AppCatalogException e) {
            log.error("Error while retrieving UNICORE job submission..");
            throw new GFacProviderException("Error while retrieving UNICORE job submission..", e);
        } catch (Exception e) {
            log.error("Cannot create storage..");
            throw new GFacProviderException("Cannot create storage..", e);
        } finally {
            // destroy sms instance
            try {
                if (sc != null) {
                    sc.destroy();
                }
            } catch (Exception e) {
                log.warn(
                        "Cannot destroy temporary SMS instance:" + sc.getUrl(),
                        e);
            }
        }

    }
	

	private JobState getApplicationJobStatus(ActivityStatusType activityStatus) {
		if (activityStatus == null) {
			return JobState.UNKNOWN;
		}
		Enum state = activityStatus.getState();
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
	 * @param activityEpr
	 * @param jobExecutionContext
	 * @throws GFacProviderException
	 */
	public boolean cancelJob(
			JobExecutionContext jobExecutionContext)
			throws GFacProviderException {
		try {
			String activityEpr = jobExecutionContext.getJobDetails().getJobDescription();
			// initSecurityProperties(jobExecutionContext);
			EndpointReferenceType eprt = EndpointReferenceType.Factory
					.parse(activityEpr);
            JobSubmissionInterface preferredJobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
            JobSubmissionProtocol protocol = preferredJobSubmissionInterface.getJobSubmissionProtocol();
            String interfaceId = preferredJobSubmissionInterface.getJobSubmissionInterfaceId();
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
			throw new GFacProviderException(e.getLocalizedMessage(), e);
		}

	}

	// FIXME: Get user details
	private String getUserName(JobExecutionContext context) {
		// if (context.getConfigurationData()!= null) {
		// return
		// context.getConfigurationData().getBasicMetadata().getUserName();
		// } else {
		return "";
		// }
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

	protected String formatStatusMessage(String activityUrl, String status) {
		return String.format("Activity %s is %s.\n", activityUrl, status);
	}

	protected String subStatusAsString(ActivityStatusType statusType) {

		StringBuffer sb = new StringBuffer();

		sb.append(statusType.getState().toString());

		XmlCursor acursor = statusType.newCursor();
		if (acursor.toFirstChild()) {
			do {
				if (acursor.getName().getNamespaceURI()
						.equals("http://schemas.ogf.org/hpcp/2007/01/fs")) {
					sb.append(":");
					sb.append(acursor.getName().getLocalPart());
				}
			} while (acursor.toNextSibling());
			acursor.dispose();
			return sb.toString();
		} else {
			acursor.dispose();
			return sb.toString();
		}

	}

	private String getCNFromUserDN(String userDN) {
		return X500NameUtils.getAttributeValues(userDN, BCStyle.CN)[0];

	}

	@Override
	public void initProperties(Map<String, String> properties)
			throws GFacProviderException, GFacException {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose(JobExecutionContext jobExecutionContext)
			throws GFacProviderException, GFacException {
		secProperties = null;

	}

    @Override
    public void recover(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        // TODO: Auto generated method body.
    }

    @Override
    public void monitor(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        // TODO: Auto generated method body.
    }

    protected void waitUntilDone(EndpointReferenceType factoryEpr, EndpointReferenceType activityEpr, JobDetails jobDetails, DefaultClientConfiguration secProperties) throws Exception {
		
		try {
			FactoryClient factoryClient = new FactoryClient(factoryEpr, secProperties);
			JobState applicationJobStatus = null;
			
			while ((factoryClient.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED)
	                && (factoryClient.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)
	                && (factoryClient.getActivityStatus(activityEpr) != ActivityStateEnumeration.CANCELLED) 
	                && (applicationJobStatus != JobState.COMPLETE)) {
	
	            ActivityStatusType activityStatus = getStatus(factoryClient, activityEpr);
	            applicationJobStatus = getApplicationJobStatus(activityStatus);
	         
	            sendNotification(jobExecutionContext,applicationJobStatus);
	
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
    private void sendNotification(JobExecutionContext jobExecutionContext,  JobState status) {
        JobStatusChangeRequestEvent jobStatus = new JobStatusChangeRequestEvent();
        JobIdentifier jobIdentity = new JobIdentifier(jobExecutionContext.getJobDetails().getJobID(),
        		jobExecutionContext.getTaskData().getTaskID(),
        		jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
        		jobExecutionContext.getExperimentID(),
        		jobExecutionContext.getGatewayID());
        jobStatus.setJobIdentity(jobIdentity);
        jobStatus.setState(status);
        log.debug(jobStatus.getJobIdentity().getJobId(), "Published job status change request, " +
                "experiment {} , task {}", jobStatus.getJobIdentity().getExperimentId(),
        jobStatus.getJobIdentity().getTaskId());
        jobExecutionContext.getLocalEventPublisher().publish(jobStatus);
    }
}
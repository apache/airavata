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
package org.apache.airavata.gfac.bes.provider.impl;

import java.util.Calendar;
import java.util.Map;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.bes.security.UNICORESecurityContext;
import org.apache.airavata.gfac.bes.security.X509SecurityContext;
import org.apache.airavata.gfac.bes.utils.BESConstants;
import org.apache.airavata.gfac.bes.utils.DataTransferrer;
import org.apache.airavata.gfac.bes.utils.JSDLGenerator;
import org.apache.airavata.gfac.bes.utils.SecurityUtils;
import org.apache.airavata.gfac.bes.utils.StorageCreator;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.core.notification.events.StatusChangeEvent;
import org.apache.airavata.gfac.core.notification.events.UnicoreJobIDEvent;
import org.apache.airavata.gfac.core.provider.AbstractProvider;
import org.apache.airavata.gfac.core.provider.GFacProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
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
		if (log.isDebugEnabled()) {
			log.debug("Generating default configuration.");
		}
		// TODO: check what credential mode should be used
		try {
			secProperties = unicoreContext.getDefaultConfiguration();
		} catch (ApplicationSettingsException e) {
			throw new GFacProviderException(e.getMessage(), e);
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
            EndpointReferenceType eprt = EndpointReferenceType.Factory
                    .newInstance();
            eprt.addNewAddress().setStringValue(factoryUrl);
            String userDN = getUserName(jobExecutionContext);

            // TODO: to be removed
            if (userDN == null || userDN.equalsIgnoreCase("admin")) {
                userDN = "CN=zdv575, O=Ultrascan Gateway, C=DE";
            }
            CreateActivityDocument cad = CreateActivityDocument.Factory
                    .newInstance();
            JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
                    .newInstance();

            // create storage
            StorageCreator storageCreator = new StorageCreator(secProperties,
                    factoryUrl, 5, null);
            sc = storageCreator.createStorage();

            JobDefinitionType jobDefinition = JSDLGenerator.buildJSDLInstance(
                    jobExecutionContext, sc.getUrl()).getJobDefinition();
            cad.addNewCreateActivity().addNewActivityDocument()
                    .setJobDefinition(jobDefinition);
            log.info("JSDL" + jobDefDoc.toString());

            // upload files if any
            DataTransferrer dt = new DataTransferrer(jobExecutionContext, sc);
            dt.uploadLocalFiles();

            JobDetails jobDetails = new JobDetails();
            FactoryClient factory = new FactoryClient(eprt, secProperties);

            log.info(String.format("Activity Submitting to %s ... \n",
                    factoryUrl));
            jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
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
            jobDetails.setJobID(activityEpr.toString());
            jobDetails.setJobDescription(activityEpr.toString());

            jobExecutionContext.setJobDetails(jobDetails);
            log.info(formatStatusMessage(activityEpr.getAddress()
                    .getStringValue(), factory.getActivityStatus(activityEpr)
                    .toString()));

            jobExecutionContext.getNotifier().publish(new UnicoreJobIDEvent(jobId));
            GFacUtils.saveJobStatus(jobExecutionContext, details, JobState.SUBMITTED);

            factory.getActivityStatus(activityEpr);
            log.info(formatStatusMessage(activityEpr.getAddress()
                    .getStringValue(), factory.getActivityStatus(activityEpr)
                    .toString()));

            // TODO publish the status messages to the message bus
            while ((factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED)
                    && (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)
                    && (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.CANCELLED)) {

                ActivityStatusType activityStatus = getStatus(factory, activityEpr);
                JobState applicationJobStatus = getApplicationJobStatus(activityStatus);
                String jobStatusMessage = "Status of job " + jobId + "is "
                        + applicationJobStatus;
                GFacUtils.updateJobStatus(jobExecutionContext, jobDetails,
                        applicationJobStatus);

                jobExecutionContext.getNotifier().publish(
                        new StatusChangeEvent(jobStatusMessage));

                // GFacUtils.updateApplicationJobStatus(jobExecutionContext,jobId,
                // applicationJobStatus);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                continue;
            }

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
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                dt.downloadStdOuts();
            } else if (activityStatus.getState() == ActivityStateEnumeration.CANCELLED) {
                JobState applicationJobStatus = JobState.CANCELED;
                String jobStatusMessage = "Status of job " + jobId + "is "
                        + applicationJobStatus;
                jobExecutionContext.getNotifier().publish(
                        new StatusChangeEvent(jobStatusMessage));
                GFacUtils.updateJobStatus(jobExecutionContext, jobDetails,
                        applicationJobStatus);
                throw new GFacProviderException(
                        jobExecutionContext.getExperimentID() + "Job Canceled");
            } else if (activityStatus.getState() == ActivityStateEnumeration.FINISHED) {
                try {
                    Thread.sleep(5000);
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

		UnicoreHostType host = (UnicoreHostType) jobExecutionContext
				.getApplicationContext().getHostDescription().getType();
		
		String factoryUrl = host.getUnicoreBESEndPointArray()[0];

		EndpointReferenceType eprt = EndpointReferenceType.Factory
				.newInstance();
		eprt.addNewAddress().setStringValue(factoryUrl);

		// WSUtilities.addServerIdentity(eprt, serverDN);

		String userDN = getUserName(jobExecutionContext);

		// TODO: to be removed
		if (userDN == null || userDN.equalsIgnoreCase("admin")) {
			userDN = "CN=zdv575, O=Ultrascan Gateway, C=DE";
		}

		StorageClient sc = null;

		try {

			CreateActivityDocument cad = CreateActivityDocument.Factory
					.newInstance();
			JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
					.newInstance();

//			String xlogin = getCNFromUserDN(userDN);

			// create storage
			StorageCreator storageCreator = new StorageCreator(secProperties,
					factoryUrl, 5, null);

			try {
				sc = storageCreator.createStorage();
			} catch (Exception e2) {
				log.error("Cannot create storage..");
				throw new GFacProviderException("Cannot create storage..", e2);
			}

			JobDefinitionType jobDefinition = jobDefDoc.addNewJobDefinition();
			try {
				jobDefinition = JSDLGenerator.buildJSDLInstance(
						jobExecutionContext, sc.getUrl()).getJobDefinition();
				cad.addNewCreateActivity().addNewActivityDocument()
						.setJobDefinition(jobDefinition);
				log.info("JSDL" + jobDefDoc.toString());
			} catch (Exception e1) {
				throw new GFacProviderException(
						"Cannot generate JSDL instance from the JobExecutionContext.",
						e1);
			}

			// upload files if any
			DataTransferrer dt = new DataTransferrer(jobExecutionContext, sc);
			dt.uploadLocalFiles();

			FactoryClient factory = null;
			JobDetails jobDetails = new JobDetails();

			try {
				factory = new FactoryClient(eprt, secProperties);
			} catch (Exception e) {
				throw new GFacProviderException(e.getLocalizedMessage(), e);
			}
			CreateActivityResponseDocument response = null;
			try {
				log.info(String.format("Activity Submitting to %s ... \n",
						factoryUrl));
				jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
				response = factory.createActivity(cad);
				log.info(String.format("Activity Submitted to %s \n", factoryUrl));
			} catch (Exception e) {
				throw new GFacProviderException("Cannot create activity.", e);
			}
			EndpointReferenceType activityEpr = response.getCreateActivityResponse().getActivityIdentifier();

			log.info("Activity : " + activityEpr.getAddress().getStringValue()	+ " Submitted.");

			// factory.waitWhileActivityIsDone(activityEpr, 1000);
			jobId = WSUtilities.extractResourceID(activityEpr);
			if (jobId == null) {
				jobId = new Long(Calendar.getInstance().getTimeInMillis())
						.toString();
			}
			log.info("JobID: " + jobId);
			jobDetails.setJobID(jobId);
			jobDetails.setJobDescription(jobId);

			jobExecutionContext.setJobDetails(jobDetails);
			try {
			log.info(formatStatusMessage(activityEpr.getAddress()
					.getStringValue(), factory.getActivityStatus(activityEpr)
					.toString()));
			
			jobExecutionContext.getNotifier().publish(new UnicoreJobIDEvent(jobId));
//			GFacUtils.saveJobStatus(jobExecutionContext, details,JobState.SUBMITTED);

			factory.getActivityStatus(activityEpr);
			log.info(formatStatusMessage(activityEpr.getAddress()
					.getStringValue(), factory.getActivityStatus(activityEpr)
					.toString()));

			// TODO publish the status messages to the message bus
			while ((factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED)
					&& (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)
					&& (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.CANCELLED)) {

				ActivityStatusType activityStatus = null;
				try {
					activityStatus = getStatus(factory, activityEpr);
					JobState applicationJobStatus = getApplicationJobStatus(activityStatus);
					String jobStatusMessage = "Status of job " + jobId + "is "
							+ applicationJobStatus;
					//TODO: properly use GFacUtils..
//					GFacUtils.updateJobStatus(jobExecutionContext, jobDetails,	applicationJobStatus);

					jobExecutionContext.getNotifier().publish(
							new StatusChangeEvent(jobStatusMessage));

					// GFacUtils.updateApplicationJobStatus(jobExecutionContext,jobId,
					// applicationJobStatus);
				} catch (UnknownActivityIdentifierFault e) {
					throw new GFacProviderException(e.getMessage(),
							e.getCause());
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				continue;
			}
			}catch(Exception e) {
				throw new GFacProviderException(e.getMessage(),
						e.getCause());
				
			}
			
			ActivityStatusType activityStatus = null;
			try {
				activityStatus = getStatus(factory, activityEpr);
				log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(), activityStatus.getState().toString()));
				ActivityClient activityClient;
				activityClient = new ActivityClient(activityEpr,secProperties);
				dt.setStorageClient(activityClient.getUspaceClient());
			} catch (Exception e1) {
				throw new GFacProviderException(e1.getMessage(),
						e1.getCause());
			}

			

			if ((activityStatus.getState() == ActivityStateEnumeration.FAILED)) {
				String error = activityStatus.getFault().getFaultcode()
						.getLocalPart()
						+ "\n"
						+ activityStatus.getFault().getFaultstring()
						+ "\n EXITCODE: " + activityStatus.getExitCode();
				log.info(error);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				dt.downloadStdOuts();
			} else if (activityStatus.getState() == ActivityStateEnumeration.CANCELLED) {
				JobState applicationJobStatus = JobState.CANCELED;
				String jobStatusMessage = "Status of job " + jobId + "is "
						+ applicationJobStatus;
				jobExecutionContext.getNotifier().publish(
						new StatusChangeEvent(jobStatusMessage));
				//TODO: properly use GFacUtils..
//				GFacUtils.updateJobStatus(jobExecutionContext, jobDetails, applicationJobStatus);
				throw new GFacProviderException(
						jobExecutionContext.getExperimentID() + "Job Canceled");
			}

			else if (activityStatus.getState() == ActivityStateEnumeration.FINISHED) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				if (activityStatus.getExitCode() == 0) {
					dt.downloadRemoteFiles();
				} else {
					dt.downloadStdOuts();
				}
			}

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
				} else if (status.equalsIgnoreCase("Staging-Out")
						|| status.equalsIgnoreCase("FINISHED")) {
					return JobState.COMPLETE;
				} else if (status.equalsIgnoreCase("Executing")) {
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
	public void cancelJob(String activityEpr,
			JobExecutionContext jobExecutionContext)
			throws GFacProviderException {
		try {
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

	protected ActivityStatusType getStatus(FactoryClient fc,
			EndpointReferenceType activityEpr)
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
	public void cancelJob(JobExecutionContext jobExecutionContext)
			throws GFacProviderException, GFacException {
		// TODO Auto-generated method stub

	}
}
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
package org.apache.airavata.gfac.provider.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.notification.events.StatusChangeEvent;
import org.apache.airavata.gfac.notification.events.UnicoreJobIDEvent;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.provider.utils.DataTransferrer;
import org.apache.airavata.gfac.provider.utils.JSDLGenerator;
import org.apache.airavata.gfac.provider.utils.StorageCreator;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.registry.api.workflow.ApplicationJob;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.apache.xmlbeans.XmlCursor;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
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

import de.fzj.unicore.bes.client.FactoryClient;
import de.fzj.unicore.bes.faults.UnknownActivityIdentifierFault;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.emi.security.authn.x509.helpers.CertificateHelpers;
import eu.emi.security.authn.x509.helpers.proxy.X509v3CertificateBuilder;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.DirectoryCertChainValidator;
import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.util.httpclient.DefaultClientConfiguration;



public class BESProvider extends AbstractProvider implements GFacProvider{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private DefaultClientConfiguration secProperties;

    private String jobId;
    
    
        
	public void initialize(JobExecutionContext jobExecutionContext)
			throws GFacProviderException, GFacException {
		log.info("Initializing UNICORE Provider");
		super.initialize(jobExecutionContext);
    	initSecurityProperties(jobExecutionContext);
    	log.debug("initialized security properties");
    }


	public void execute(JobExecutionContext jobExecutionContext)
			throws GFacProviderException {
        UnicoreHostType host = (UnicoreHostType) jobExecutionContext.getApplicationContext().getHostDescription()
                .getType();

        String factoryUrl = host.getUnicoreBESEndPointArray()[0];

        EndpointReferenceType eprt = EndpointReferenceType.Factory.newInstance();
        eprt.addNewAddress().setStringValue(factoryUrl);

        String userDN = getUserName(jobExecutionContext);

        if (userDN == null || userDN.equalsIgnoreCase("admin")) {
            userDN = "CN=zdv575, O=Ultrascan Gateway, C=DE";
        }

        String xlogin = getCNFromUserDN(userDN);
        // create storage
        StorageCreator storageCreator = new StorageCreator(secProperties, factoryUrl, 5, xlogin);

        StorageClient sc = null;
        try {
            try {
                sc = storageCreator.createStorage();
            } catch (Exception e2) {
                log.error("Cannot create storage..");
                throw new GFacProviderException("Cannot create storage..", e2);
            }

            CreateActivityDocument cad = CreateActivityDocument.Factory.newInstance();
            JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory.newInstance();

            JobDefinitionType jobDefinition = jobDefDoc.addNewJobDefinition();
            try {
                jobDefinition = JSDLGenerator.buildJSDLInstance(jobExecutionContext, sc.getUrl()).getJobDefinition();
                cad.addNewCreateActivity().addNewActivityDocument().setJobDefinition(jobDefinition);

                log.info("JSDL" + jobDefDoc.toString());
            } catch (Exception e1) {
                throw new GFacProviderException("Cannot generate JSDL instance from the JobExecutionContext.", e1);
            }

            // upload files if any
            DataTransferrer dt = new DataTransferrer(jobExecutionContext, sc);
            dt.uploadLocalFiles();

            FactoryClient factory = null;
            try {
                factory = new FactoryClient(eprt, secProperties);
            } catch (Exception e) {
                throw new GFacProviderException(e.getLocalizedMessage(), e);
            }

            CreateActivityResponseDocument response = null;
            try {
                log.info(String.format("Activity Submitting to %s ... \n", factoryUrl));
                response = factory.createActivity(cad);
                log.info(String.format("Activity Submitted to %s \n", factoryUrl));
            } catch (Exception e) {
                throw new GFacProviderException("Cannot create activity.", e);
            }
            EndpointReferenceType activityEpr = response.getCreateActivityResponse().getActivityIdentifier();

            log.info("Activity : " + activityEpr.getAddress().getStringValue() + " Submitted.");

            // factory.waitWhileActivityIsDone(activityEpr, 1000);
            jobId = WSUtilities.extractResourceID(activityEpr);
            if (jobId == null) {
                jobId = new Long(Calendar.getInstance().getTimeInMillis()).toString();
            }
            log.info("JobID: " + jobId);
            jobExecutionContext.getNotifier().publish(new UnicoreJobIDEvent(jobId));
            saveApplicationJob(jobExecutionContext, jobDefinition, activityEpr.toString());

            factory.getActivityStatus(activityEpr);
            log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(),
                    factory.getActivityStatus(activityEpr).toString()));

            // TODO publish the status messages to the message bus
            while ((factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED)
                    && (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)
                    && (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.CANCELLED)) {

                ActivityStatusType activityStatus = null;
                try {
                    activityStatus = getStatus(factory, activityEpr);
                    JobState jobStatus = getApplicationJobStatus(activityStatus);
                    String jobStatusMessage = "Status of job " + jobId + "is " + jobStatus;
                    jobExecutionContext.getNotifier().publish(new StatusChangeEvent(jobStatusMessage));
                    details.setJobID(jobId);
                    GFacUtils.updateJobStatus(details, jobStatus);
                } catch (UnknownActivityIdentifierFault e) {
                    throw new GFacProviderException(e.getMessage(), e.getCause());
                }catch (GFacException e) {
                    throw new GFacProviderException(e.getMessage(), e.getCause());
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                continue;
            }

            ActivityStatusType activityStatus = null;
            try {
                activityStatus = getStatus(factory, activityEpr);
            } catch (UnknownActivityIdentifierFault e) {
                throw new GFacProviderException(e.getMessage(), e.getCause());
            }

            log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(), activityStatus.getState()
                    .toString()));

            if ((activityStatus.getState() == ActivityStateEnumeration.FAILED)) {
                String error = activityStatus.getFault().getFaultcode().getLocalPart() + "\n"
                        + activityStatus.getFault().getFaultstring() + "\n EXITCODE: " + activityStatus.getExitCode();
                log.info(error);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                dt.downloadStdOuts();
            } else if (activityStatus.getState() == ActivityStateEnumeration.CANCELLED) {
                String experimentID = (String) jobExecutionContext.getProperty(Constants.PROP_TOPIC);
                JobState jobStatus = JobState.CANCELED;
                String jobStatusMessage = "Status of job " + jobId + "is " + jobStatus;
                jobExecutionContext.getNotifier().publish(new StatusChangeEvent(jobStatusMessage));
                details.setJobID(jobId);
                try {
					GFacUtils.saveJobStatus(details, jobStatus, jobExecutionContext.getTaskData().getTaskID());
				} catch (GFacException e) {
					 throw new GFacProviderException(e.getLocalizedMessage(),e);
				}
                throw new GFacProviderException(experimentID + "Job Canceled");
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

        } catch (UnknownActivityIdentifierFault e1) {
            throw new GFacProviderException(e1.getLocalizedMessage(), e1);
        } finally {
            // destroy sms instance
            try {
                if (sc != null) {
                    sc.destroy();
                }
            } catch (Exception e) {
                log.warn("Cannot destroy temporary SMS instance:" + sc.getUrl(), e);
            }
        }
    }

	private JobState getApplicationJobStatus(ActivityStatusType activityStatus){
        if (activityStatus == null) {
            return JobState.UNKNOWN;
        }
        Enum state = activityStatus.getState();
        String status = null;
        XmlCursor acursor = activityStatus.newCursor();
        try {
            if (acursor.toFirstChild()) {
                if (acursor.getName().getNamespaceURI().equals("http://schemas.ogf.org/hpcp/2007/01/fs")) {
                    status = acursor.getName().getLocalPart();
                }
            }
            if (status != null) {
                if (status.equalsIgnoreCase("Queued") || status.equalsIgnoreCase("Starting")
                        || status.equalsIgnoreCase("Ready")) {
                    return JobState.QUEUED;
                } else if (status.equalsIgnoreCase("Staging-In")) {
                    return JobState.SUBMITTED;
                } else if (status.equalsIgnoreCase("Staging-Out") || status.equalsIgnoreCase("FINISHED")) {
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

    private void saveApplicationJob(JobExecutionContext jobExecutionContext, JobDefinitionType jobDefinition,
                                    String metadata) {
        ApplicationJob appJob = GFacUtils.createApplicationJob(jobExecutionContext);
        appJob.setJobId(jobId);
        appJob.setJobData(jobDefinition.toString());
        appJob.setSubmittedTime(Calendar.getInstance().getTime());
        appJob.setStatus(ApplicationJobStatus.SUBMITTED);
        appJob.setStatusUpdateTime(appJob.getSubmittedTime());
        appJob.setMetadata(metadata);
        GFacUtils.recordApplicationJob(jobExecutionContext, appJob);
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        secProperties = null;
    }

    /**
     * EndpointReference need to be saved to make cancel work.
     *
     * @param activityEpr
     * @param jobExecutionContext
     * @throws GFacProviderException
     */
    public void cancelJob(String activityEpr, JobExecutionContext jobExecutionContext) throws GFacProviderException {
        try {
            initSecurityProperties(jobExecutionContext);
            EndpointReferenceType eprt = EndpointReferenceType.Factory.parse(activityEpr);
            UnicoreHostType host = (UnicoreHostType) jobExecutionContext.getApplicationContext().getHostDescription()
                    .getType();

            String factoryUrl = host.getUnicoreBESEndPointArray()[0];
            EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
            epr.addNewAddress().setStringValue(factoryUrl);

            FactoryClient factory = new FactoryClient(epr, secProperties);
            factory.terminateActivity(eprt);
        } catch (Exception e) {
            throw new GFacProviderException(e.getLocalizedMessage(),e);
        }

    }

    protected void downloadOffline(String smsEpr, JobExecutionContext jobExecutionContext) throws GFacProviderException {
        try {
            initSecurityProperties(jobExecutionContext);
            EndpointReferenceType eprt = EndpointReferenceType.Factory.parse(smsEpr);
            StorageClient sms = new StorageClient(eprt, secProperties);
            DataTransferrer dt = new DataTransferrer(jobExecutionContext, sms);
            // there must be output files there
            // this is also possible if client is re-connected, the jobs are
            // still
            // running and no output is produced
            dt.downloadRemoteFiles();

            // may be use the below method before downloading for checking
            // the number of entries
            // sms.listDirectory(".");

        } catch (Exception e) {
            throw new GFacProviderException(e.getLocalizedMessage(), e);
        }
    }

    protected void initSecurityProperties(JobExecutionContext jobExecutionContext) throws GFacProviderException,
            GFacException {

        if (secProperties != null)
            return;

        GSISecurityContext gssContext = (GSISecurityContext) jobExecutionContext
                .getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT);

        try {
            String certLocation = gssContext.getTrustedCertificatePath();
            List<String> trustedCert = new ArrayList<String>();
            trustedCert.add(certLocation + "/*.0");
            trustedCert.add(certLocation + "/*.pem");

            DirectoryCertChainValidator dcValidator = new DirectoryCertChainValidator(trustedCert, Encoding.PEM, -1,
                    60000, null);

            String userID = getUserName(jobExecutionContext);

            if ( userID == null || "".equals(userID) || userID.equalsIgnoreCase("admin") ) {
                userID = "CN=zdv575, O=Ultrascan Gateway, C=DE";
            }

            String userDN = userID.replaceAll("^\"|\"$", "");

            // TODO: should be changed to default airavata server locations
            KeyAndCertCredential cred = generateShortLivedCertificate(userDN, certLocation
                    + "/cacert.pem", certLocation
                    + "/cakey.pem", "ultrascan3");
            secProperties = new DefaultClientConfiguration(dcValidator, cred);

            // secProperties.doSSLAuthn();
            secProperties.getETDSettings().setExtendTrustDelegation(true);

            secProperties.setDoSignMessage(true);

            String[] outHandlers = secProperties.getOutHandlerClassNames();

            Set<String> outHandlerLst = null;

            // timeout in milliseconds
            Properties p = secProperties.getExtraSettings();
            p.setProperty("http.connection.timeout", "300000");
            p.setProperty("http.socket.timeout", "300000");

            if (outHandlers == null) {
                outHandlerLst = new HashSet<String>();
            } else {
                outHandlerLst = new HashSet<String>(Arrays.asList(outHandlers));
            }

            outHandlerLst.add("de.fzj.unicore.uas.security.ProxyCertOutHandler");

            secProperties.setOutHandlerClassNames(outHandlerLst.toArray(new String[outHandlerLst.size()]));

        } catch (Exception e) {
            throw new GFacProviderException(e.getMessage(), e);
        }
    }

    //FIXME: Get user details
    private String getUserName(JobExecutionContext context) {
//        if (context.getConfigurationData()!= null) {
//            return context.getConfigurationData().getBasicMetadata().getUserName();
//        } else {
           return "";
//        }
    }

    protected ActivityStatusType getStatus(FactoryClient fc, EndpointReferenceType activityEpr)
            throws UnknownActivityIdentifierFault {

        GetActivityStatusesDocument stats = GetActivityStatusesDocument.Factory.newInstance();

        stats.addNewGetActivityStatuses().setActivityIdentifierArray(new EndpointReferenceType[] { activityEpr });

        GetActivityStatusesResponseDocument resDoc = fc.getActivityStatuses(stats);

        ActivityStatusType activityStatus = resDoc.getGetActivityStatusesResponse().getResponseArray()[0]
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
                if (acursor.getName().getNamespaceURI().equals("http://schemas.ogf.org/hpcp/2007/01/fs")) {
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

    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }

    protected KeyAndCertCredential generateShortLivedCertificate(String userDN, String caCertPath, String caKeyPath,
                                                                 String caPwd) throws Exception {
        final long CredentialGoodFromOffset = 1000L * 60L * 15L; // 15 minutes
        // ago

        final long startTime = System.currentTimeMillis() - CredentialGoodFromOffset;
        final long endTime = startTime + 30 * 3600 * 1000;

        String keyLengthProp = "1024";
        int keyLength = Integer.parseInt(keyLengthProp);
        String signatureAlgorithm = "SHA1withRSA";

        KeyAndCertCredential caCred = getCACredential(caCertPath, caKeyPath, caPwd);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(caCred.getKey().getAlgorithm());
        kpg.initialize(keyLength);
        KeyPair pair = kpg.generateKeyPair();

        X500Principal subjectDN = new X500Principal(userDN);
        Random rand = new Random();

        SubjectPublicKeyInfo publicKeyInfo;
        try {
            publicKeyInfo = SubjectPublicKeyInfo.getInstance(new ASN1InputStream(pair.getPublic().getEncoded())
                    .readObject());
        } catch (IOException e) {
            throw new InvalidKeyException("Can not parse the public key"
                    + "being included in the short lived certificate", e);
        }

        X500Name issuerX500Name = CertificateHelpers.toX500Name(caCred.getCertificate().getSubjectX500Principal());

        X500Name subjectX500Name = CertificateHelpers.toX500Name(subjectDN);

        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(issuerX500Name, new BigInteger(20, rand),
                new Date(startTime), new Date(endTime), subjectX500Name, publicKeyInfo);

        AlgorithmIdentifier sigAlgId = X509v3CertificateBuilder.extractAlgorithmId(caCred.getCertificate());

        X509Certificate certificate = certBuilder.build(caCred.getKey(), sigAlgId, signatureAlgorithm, null, null);

        certificate.checkValidity(new Date());
        certificate.verify(caCred.getCertificate().getPublicKey());
        KeyAndCertCredential result = new KeyAndCertCredential(pair.getPrivate(), new X509Certificate[] { certificate,
                caCred.getCertificate() });

        return result;
    }

    private KeyAndCertCredential getCACredential(String caCertPath, String caKeyPath, String password) throws Exception {
        InputStream isKey = new FileInputStream(caKeyPath);
        PrivateKey pk = CertificateUtils.loadPrivateKey(isKey, Encoding.PEM, password.toCharArray());

        InputStream isCert = new FileInputStream(caCertPath);
        X509Certificate caCert = CertificateUtils.loadCertificate(isCert, Encoding.PEM);

        if (isKey != null)
            isKey.close();
        if (isCert != null)
            isCert.close();

        return new KeyAndCertCredential(pk, new X509Certificate[] { caCert });
    }

    private String getCNFromUserDN(String userDN) {
        return X500NameUtils.getAttributeValues(userDN, BCStyle.CN)[0];

    }
}

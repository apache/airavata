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
package org.apache.airavata.core.gfac.provider.impl;

import de.fzj.unicore.bes.client.FactoryClient;
import de.fzj.unicore.bes.faults.InvalidRequestMessageFault;
import de.fzj.unicore.bes.faults.NotAcceptingNewActivitiesFault;
import de.fzj.unicore.bes.faults.UnsupportedFeatureFault;
import de.fzj.unicore.uas.security.ClientProperties;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.security.util.client.IClientProperties;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.*;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.provider.utils.BESJob;
import org.apache.airavata.core.gfac.provider.utils.JobSubmissionListener;
import org.apache.airavata.core.gfac.provider.utils.JSDLGenerator;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

public class JSDLProvider {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";
    private IClientProperties securityProperties;
    private String unicoreHost;
    private JobSubmissionListener listener;
    private BESJob job;
    private String jobId;
    private GSISecurityContext gssContext;

    public void makeDirectory(InvocationContext invocationContext) throws ProviderException {
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();

        GridFtp ftp = new GridFtp();

        try {
            gssContext = (GSISecurityContext)invocationContext.getSecurityContext(MYPROXY_SECURITY_CONTEXT);
            GSSCredential gssCred = gssContext.getGssCredentails();
            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[] { host.getHostAddress() };
            }

            boolean success = false;
            ProviderException pe = null;// = new ProviderException("");

            for (String endpoint : host.getGridFTPEndPointArray()) {
                try {

                    URI tmpdirURI = GfacUtils.createGsiftpURI(endpoint, app.getScratchWorkingDirectory());
                    URI workingDirURI = GfacUtils.createGsiftpURI(endpoint, app.getStaticWorkingDirectory());
                    URI inputURI = GfacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
                    URI outputURI = GfacUtils.createGsiftpURI(endpoint, app.getOutputDataDirectory());

                    log.debug("Host FTP = " + hostgridFTP);
                    log.debug("temp directory = " + tmpdirURI);
                    log.debug("Working directory = " + workingDirURI);
                    log.debug("Input directory = " + inputURI);
                    log.debug("Output directory = " + outputURI);

                    ftp.makeDir(tmpdirURI, gssCred);
                    ftp.makeDir(workingDirURI, gssCred);
                    ftp.makeDir(inputURI, gssCred);
                    ftp.makeDir(outputURI, gssCred);

                    success = true;
                    break;
                } catch (URISyntaxException e) {
                    pe = new ProviderException("URI is malformatted:" + e.getMessage(), e,invocationContext);

                } catch (ToolsException e) {
                    pe = new ProviderException(e.getMessage(), e,invocationContext);
                }
            }
            if (success == false) {
                throw pe;
            }
        } catch (org.apache.airavata.core.gfac.exception.SecurityException e) {
            throw new ProviderException(e.getMessage(), e,invocationContext);
        }
    }

    public void setupEnvironment(InvocationContext invocationContext) throws ProviderException {
        UnicoreHostType host = (UnicoreHostType) invocationContext.getExecutionDescription().getHost().getType();

        log.debug("Searching for Gate Keeper");


        String tmp[] = host.getUnicoreHostAddressArray();
        if (tmp == null || tmp.length == 0) {
            unicoreHost = host.getHostAddress();
        }else{
            /*
             * TODO: algorithm for correct gatekeeper selection
             */
            unicoreHost = tmp[0];
        }
        log.debug("Using Globus GateKeeper " + unicoreHost);

        try {
            JobDefinitionDocument jsdl = JSDLGenerator.configureRemoteJob(invocationContext);
            log.debug("JSDL = " + jsdl.toString());

            job = new BESJob();
            job.setJobDoc(jsdl);
            job.setFactory(unicoreHost);
//            listener = new JobSubmissionListener(job, invocationContext);
//            job.addListener(listener);

        } catch (ToolsException te) {
            throw new ProviderException(te.getMessage(), te, invocationContext);
        }

    }

    public void executeApplication(InvocationContext invocationContext) throws ProviderException {
        GlobusHostType host = (GlobusHostType) invocationContext.getExecutionDescription().getHost().getType();
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();
        StringBuffer buf = new StringBuffer();
            /*
             * Set Security
             */
            securityProperties = initSecurityProperties();
            String factoryUrl = job.getFactoryUrl();
            EndpointReferenceType eprt = EndpointReferenceType.Factory
                    .newInstance();
            eprt.addNewAddress().setStringValue(factoryUrl);
            System.out.println("========================================");
            System.out.println(String.format("Job Submitted to %s.\n", factoryUrl));
            FactoryClient factory = null;
            try {
                factory = new FactoryClient(eprt, securityProperties);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            CreateActivityDocument cad = CreateActivityDocument.Factory
                    .newInstance();
            cad.addNewCreateActivity().addNewActivityDocument()
                    .setJobDefinition(job.getJobDoc().getJobDefinition());
            CreateActivityResponseDocument response = null;
            try {
                response = factory.createActivity(cad);
            } catch (NotAcceptingNewActivitiesFault notAcceptingNewActivitiesFault) {
                notAcceptingNewActivitiesFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidRequestMessageFault invalidRequestMessageFault) {
                invalidRequestMessageFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnsupportedFeatureFault unsupportedFeatureFault) {
                unsupportedFeatureFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            EndpointReferenceType activityEpr = response
                    .getCreateActivityResponse().getActivityIdentifier();
            //factory.waitWhileActivityIsDone(activityEpr, 1000);
            jobId = WSUtilities.extractResourceID(activityEpr);
            if (jobId == null) {
                jobId = new Long(Calendar.getInstance().getTimeInMillis())
                        .toString();
            }
            ActivityStateEnumeration.Enum state = factory.getActivityStatus(activityEpr);

            String status;

            status = String.format("Job %s is %s.\n", activityEpr.getAddress()
                    .getStringValue(), factory.getActivityStatus(activityEpr)
                    .toString()).toString();


            while ((factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED) &&
                    (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)){
                status = String.format("Job %s is %s.\n", activityEpr.getAddress()
                        .getStringValue(), factory.getActivityStatus(activityEpr)
                        .toString()).toString();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                continue;
            }

            status = String.format("Job %s is %s.\n", activityEpr.getAddress()
                    .getStringValue(), factory.getActivityStatus(activityEpr)
                    .toString()).toString();

            log.debug("Request to contact:" + unicoreHost);

            buf.append("Finished launching job, Host = ").append(host.getHostAddress()).append(" JSDL = ")
                    .append(job.getJobDoc().toString()).append(" working directory = ").append(app.getStaticWorkingDirectory())
                    .append(" temp directory = ").append(app.getScratchWorkingDirectory())
                    .append(" Unicore Endpoint = ").append(unicoreHost);
            invocationContext.getExecutionContext().getNotifier().info(invocationContext, buf.toString());
            invocationContext.getExecutionContext().getNotifier().info(invocationContext, "JobID=" + jobId);
            log.debug(buf.toString());
    }

    protected ClientProperties initSecurityProperties() {
        //todo provide a proper way of specified credentials
		ClientProperties sp = new ClientProperties();
		sp.setSslEnabled(true);
		sp.setSignMessage(true);
		sp.setKeystore("src/test/resources/demo-keystore.jks");
		sp.setKeystorePassword("654321");
		sp.setKeystoreAlias("demouser-new");
		sp.setKeystoreType("JKS");
		return sp;
	}

}

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.airavata.gfac.context.GSISecurityContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.provider.utils.JSDLGenerator;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseDocument;
import org.globus.gsi.GlobusCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.bes.client.FactoryClient;
import de.fzj.unicore.bes.faults.InvalidRequestMessageFault;
import de.fzj.unicore.bes.faults.NotAcceptingNewActivitiesFault;
import de.fzj.unicore.bes.faults.UnsupportedFeatureFault;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.DirectoryCertChainValidator;
import eu.emi.security.authn.x509.impl.PEMCredential;
import eu.unicore.util.httpclient.DefaultClientConfiguration;



public class BESProvider implements GFacProvider {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private DefaultClientConfiguration secProperties;

    private String jobId;



    @Override
	public void initialize(JobExecutionContext jobExecutionContext)
			throws GFacProviderException {

    	log.info("Initializing GFAC's <<< UNICORE Provider >>>");
    	initSecurityProperties(jobExecutionContext);
    	log.debug("initialized security properties");

	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext)
			throws GFacProviderException {
		UnicoreHostType host = (UnicoreHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();

        String factoryUrl = host.getUnicoreHostAddressArray()[0];

        EndpointReferenceType eprt = EndpointReferenceType.Factory.newInstance();
        eprt.addNewAddress().setStringValue(factoryUrl);
        log.info("========================================");
        log.info(String.format("Job Submitted to %s.\n", factoryUrl));

        FactoryClient factory = null;
        try {
            factory = new FactoryClient(eprt, secProperties);
        } catch (Exception e) {
            throw new GFacProviderException("");
        }
        CreateActivityDocument cad = CreateActivityDocument.Factory
                .newInstance();


        try {
			cad.addNewCreateActivity().addNewActivityDocument()
			        .setJobDefinition(JSDLGenerator.buildJSDLInstance(jobExecutionContext).getJobDefinition());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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

        log.debug("Job EPR: "+activityEpr);

        log.info("Job: "+activityEpr.getAddress().getStringValue()+  " Submitted.");

        //factory.waitWhileActivityIsDone(activityEpr, 1000);
        jobId = WSUtilities.extractResourceID(activityEpr);
        if (jobId == null) {
            jobId = new Long(Calendar.getInstance().getTimeInMillis())
                    .toString();
        }


        String status = String.format("Job %s is %s.\n", activityEpr.getAddress()
                .getStringValue(), factory.getActivityStatus(activityEpr)
                .toString()).toString();


        log.info(status);


        while ((factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED) &&
                (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)){
            status = String.format("Job %s is %s.\n", activityEpr.getAddress()
                    .getStringValue(), factory.getActivityStatus(activityEpr)
                    .toString()).toString();
            try {
            	log.info(status);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            continue;
        }

        status = String.format("Job %s is %s.\n", activityEpr.getAddress()
                .getStringValue(), factory.getActivityStatus(activityEpr)
                .toString()).toString();

        log.info(status);

	}

	@Override
	public void dispose(JobExecutionContext jobExecutionContext)
			throws GFacProviderException {

		secProperties = null;

	}


	protected void initSecurityProperties(JobExecutionContext jobExecutionContext) throws GFacProviderException{

		if (secProperties != null) return;

		GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
		GlobusCredential credentials = gssContext.getGlobusCredential();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		BufferedOutputStream bufos = new BufferedOutputStream(bos);

		ByteArrayInputStream bis = null;
		BufferedInputStream bufis = null;
		try{
			credentials.save(bufos);
			bufos.flush();

			//TODO: to be supported by airavata gsscredential class
			List<String> trustedCert = new ArrayList<String>();
			trustedCert.add(gssContext.getTrustedCertLoc()+"/*.0");
			trustedCert.add(gssContext.getTrustedCertLoc()+"/*.pem");

			char[] c = null;

			DirectoryCertChainValidator dcValidator = new DirectoryCertChainValidator(trustedCert, Encoding.PEM, -1, 60000, null);
			bis = new ByteArrayInputStream(bos.toByteArray());
			bufis = new BufferedInputStream(bis);
			PEMCredential pem = new PEMCredential(bufis, c);

			secProperties = new DefaultClientConfiguration(dcValidator, pem);
			secProperties.doSSLAuthn();

			String[] outHandlers = secProperties.getOutHandlerClassNames();

			Set<String> outHandlerLst = null;

	        if(outHandlers == null) {
	        	outHandlerLst = new HashSet<String>();
	        }
	        else  {
	        	outHandlerLst = new HashSet<String>(Arrays.asList(outHandlers));
	        }

	        outHandlerLst.add("de.fzj.unicore.uas.security.ProxyCertOutHandler");

	        secProperties.setOutHandlerClassNames(outHandlerLst.toArray(new String[outHandlerLst.size()]));

		}
		catch (Exception e) {
			throw new GFacProviderException(e.getMessage(), e);
		}
		finally{
			try {
				if(bos!=null)bos.close();
				if(bufos!=null)bufos.close();
				if(bis!=null)bis.close();
				if(bufis!=null)bufis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}






}

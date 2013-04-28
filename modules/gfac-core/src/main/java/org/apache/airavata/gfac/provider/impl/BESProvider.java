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
import java.util.*;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.provider.utils.JSDLGenerator;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.apache.xmlbeans.XmlCursor;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesDocument;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseDocument;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.bes.client.FactoryClient;
import de.fzj.unicore.bes.faults.UnknownActivityIdentifierFault;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.DirectoryCertChainValidator;
import eu.emi.security.authn.x509.impl.PEMCredential;
import eu.unicore.util.httpclient.DefaultClientConfiguration;



public class BESProvider implements GFacProvider {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private DefaultClientConfiguration secProperties;

    private String jobId;
    
    
        
	public void initialize(JobExecutionContext jobExecutionContext)
			throws GFacProviderException, GFacException {
		
    	log.info("Initializing UNICORE Provider");
    	initSecurityProperties(jobExecutionContext);
    	log.debug("initialized security properties");

	}


	public void execute(JobExecutionContext jobExecutionContext)
			throws GFacProviderException {
		UnicoreHostType host = (UnicoreHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        
        String factoryUrl = host.getUnicoreBESEndPointArray()[0];

        EndpointReferenceType eprt = EndpointReferenceType.Factory.newInstance();
        eprt.addNewAddress().setStringValue(factoryUrl);

        CreateActivityDocument cad = CreateActivityDocument.Factory
                .newInstance();
        
        try {
            //FIXME: Replace by a native client
//			cad.addNewCreateActivity().addNewActivityDocument()
//			        .setJobDefinition(JSDLGenerator.buildJSDLInstance(jobExecutionContext).getJobDefinition());
			System.out.println("REMOVE ME");
		} catch (Exception e1) {
			throw new GFacProviderException("Cannot generate JSDL instance from the JobExecutionContext.",e1);
		}
        
        FactoryClient factory = null;
        try {
            factory = new FactoryClient(eprt, secProperties);
        } catch (Exception e) {
            throw new GFacProviderException("");
        }
        CreateActivityResponseDocument response = null;
        try {
            log.info(String.format("Activity Submitting to %s ... \n", factoryUrl));
            response = factory.createActivity(cad);
            log.info(String.format("Activity Submitted to %s \n", factoryUrl));
        } catch (Exception e) {
        	e.printStackTrace();
            throw new GFacProviderException("Cannot create activity.", e);
        } 
        EndpointReferenceType activityEpr = response
                .getCreateActivityResponse().getActivityIdentifier();
        
        log.debug("Activity EPR: "+activityEpr);
        
        log.info("Activity: "+activityEpr.getAddress().getStringValue()+  " Submitted.");
        
        //factory.waitWhileActivityIsDone(activityEpr, 1000);
        jobId = WSUtilities.extractResourceID(activityEpr);
        if (jobId == null) {
            jobId = new Long(Calendar.getInstance().getTimeInMillis())
                    .toString();
        }
        
        log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(), factory.getActivityStatus(activityEpr)
                .toString()));
        
        
        //TODO publish the status messages to the message bus
        while ((factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FINISHED) &&
                (factory.getActivityStatus(activityEpr) != ActivityStateEnumeration.FAILED)){
        	
            ActivityStatusType activityStatus = null;
    		try {
    			activityStatus = getStatus(factory, activityEpr);
    			log.info (subStatusAsString(activityStatus));
    		} catch (UnknownActivityIdentifierFault e) {
    			throw new GFacProviderException(e.getMessage(), e.getCause());
    		}
	
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            continue;
        }
        
        ActivityStatusType activityStatus = null;
		try {
			activityStatus = getStatus(factory, activityEpr);
		} catch (UnknownActivityIdentifierFault e) {
			throw new GFacProviderException(e.getMessage(), e.getCause());
		}
        
        log.info(formatStatusMessage(activityEpr.getAddress().getStringValue(), activityStatus.getState().toString()));
        
		if ((activityStatus.getState() == ActivityStateEnumeration.FAILED)) {
				log.info(activityStatus.getFault().getFaultcode().getLocalPart()
						+ "\n" + activityStatus.getFault().getFaultstring());
				log.info("EXITCODE: "+activityStatus.getExitCode());
		} 
	}

	public void dispose(JobExecutionContext jobExecutionContext)
			throws GFacProviderException {
		secProperties = null;
	}
	
	
	protected void initSecurityProperties(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException{
		
		if (secProperties != null) return;
		

		
		GSISecurityContext gssContext = (GSISecurityContext)jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT);
//		GlobusCredential credentials = gssContext.getGlobusCredential();
		
		GlobusGSSCredentialImpl gss = (GlobusGSSCredentialImpl) gssContext.getGssCredentails();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		BufferedOutputStream bufos = new BufferedOutputStream(bos);
		
		ByteArrayInputStream bis = null;
		BufferedInputStream bufis = null;
		try{
			gss.getGlobusCredential().save(bufos);
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
	
	protected ActivityStatusType getStatus(FactoryClient fc, EndpointReferenceType activityEpr) throws UnknownActivityIdentifierFault{
		
    	GetActivityStatusesDocument stats = GetActivityStatusesDocument.Factory.newInstance();
    	
    	stats.addNewGetActivityStatuses().setActivityIdentifierArray(new EndpointReferenceType[] {activityEpr});
    	
		GetActivityStatusesResponseDocument resDoc = fc.getActivityStatuses(stats);
		
		ActivityStatusType activityStatus = resDoc
				.getGetActivityStatusesResponse()
				.getResponseArray()[0].getActivityStatus();
		return activityStatus;
	}
	
	
	protected String formatStatusMessage(String activityUrl, String status){
		return String.format("Activity %s is %s.\n", activityUrl, status);
	}
	
	protected String subStatusAsString(ActivityStatusType statusType) {
		
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(statusType.getState().toString());
		
		XmlCursor acursor = statusType.newCursor();
		if (acursor.toFirstChild()) {
			do {
				if(acursor.getName().getNamespaceURI().equals("http://schemas.ogf.org/hpcp/2007/01/fs")) {
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

}

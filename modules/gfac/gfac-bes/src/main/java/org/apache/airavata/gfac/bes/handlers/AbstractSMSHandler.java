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

package org.apache.airavata.gfac.bes.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.bes.security.UNICORESecurityContext;
import org.apache.airavata.gfac.bes.security.X509SecurityContext;
import org.apache.airavata.gfac.bes.utils.BESConstants;
import org.apache.airavata.gfac.bes.utils.DataTransferrer;
import org.apache.airavata.gfac.bes.utils.SecurityUtils;
import org.apache.airavata.gfac.bes.utils.StorageCreator;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.experiment.CorrectiveAction;
import org.apache.airavata.model.experiment.ErrorCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.StorageClient;
import eu.unicore.util.httpclient.DefaultClientConfiguration;

public abstract class AbstractSMSHandler implements BESConstants, GFacHandler{
    
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
    protected DefaultClientConfiguration secProperties;
	
	protected StorageClient storageClient; 
	
	protected DataTransferrer dataTransferrer;
	
	@Override
	public void initProperties(Properties properties)
			throws GFacHandlerException {
		
	}

	@Override
	public void invoke(JobExecutionContext jobExecutionContext)
			throws GFacHandlerException {
		try {
            initSecurityProperties(jobExecutionContext);
            JobSubmissionInterface preferredJobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
            JobSubmissionProtocol protocol = preferredJobSubmissionInterface.getJobSubmissionProtocol();
            String interfaceId = preferredJobSubmissionInterface.getJobSubmissionInterfaceId();
            String factoryUrl = null;
            if (protocol.equals(JobSubmissionProtocol.UNICORE)) {
                    UnicoreJobSubmission unicoreJobSubmission = GFacUtils.getUnicoreJobSubmission(interfaceId);
                    factoryUrl = unicoreJobSubmission.getUnicoreEndPointURL();
            }
            storageClient = null;

            if (!isSMSInstanceExisting(jobExecutionContext)) {
                EndpointReferenceType eprt = EndpointReferenceType.Factory.newInstance();
                eprt.addNewAddress().setStringValue(factoryUrl);
                StorageCreator storageCreator = new StorageCreator(secProperties, factoryUrl, 5, null);
                try {
                    storageClient = storageCreator.createStorage();
                } catch (Exception e2) {
                    log.error("Cannot create storage..");
                    throw new GFacHandlerException("Cannot create storage..", e2);
                }
                jobExecutionContext.setProperty(PROP_SMS_EPR, storageClient.getEPR());
            } else {
                EndpointReferenceType eprt = (EndpointReferenceType) jobExecutionContext.getProperty(PROP_SMS_EPR);
                try {
                    storageClient = new StorageClient(eprt, secProperties);
                } catch (Exception e) {
                    throw new GFacHandlerException("Cannot create storage..", e);
                }
            }
            dataTransferrer = new DataTransferrer(jobExecutionContext, storageClient);
        } catch (AppCatalogException e) {
            throw new GFacHandlerException("Error occurred while retrieving unicore job submission interface..", e);
        }
    }
	
	protected void initSecurityProperties(JobExecutionContext jobExecutionContext) throws GFacHandlerException{
		log.debug("Initializing SMSInHandler security properties ..");
        if (secProperties != null) {
            secProperties = secProperties.clone();
            return;
        }
        UNICORESecurityContext unicoreContext;
        try {
	        if (jobExecutionContext.getSecurityContext(X509SecurityContext.X509_SECURITY_CONTEXT) == null ) {
				SecurityUtils.addSecurityContext(jobExecutionContext);
				log.info("Successfully added the UNICORE Security Context");
	        }
	    }catch (Exception e) {
        	log.error(e.getMessage());
            try {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
				GFacUtils.saveErrorDetails(jobExecutionContext, errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
            } catch (GFacException e1) {
				 log.error(e1.getLocalizedMessage());
            }  
            throw new GFacHandlerException("Error while creating UNICORESecurityContext", e, e.getLocalizedMessage());
        }
        	
		try {
			unicoreContext = (UNICORESecurityContext) jobExecutionContext.getSecurityContext(X509SecurityContext.X509_SECURITY_CONTEXT);
			log.info("Successfully retrieved the UNICORE Security Context");
		} catch (GFacException e) {
			throw new GFacHandlerException(e);
		}
        if(log.isDebugEnabled()) {
        	log.debug("Generating client's default security configuration..");
        }
        //TODO: check what kind of credential (server signed or myproxy) should be used
        try {
			secProperties = unicoreContext.getDefaultConfiguration(false);
		} catch (Exception e) {
			throw new GFacHandlerException(e);
		} 
        if(log.isDebugEnabled()) {
        	log.debug("Security properties are initialized.");
        }
        jobExecutionContext.setProperty(PROP_CLIENT_CONF, secProperties);
	}
	
	protected boolean isSMSInstanceExisting(JobExecutionContext jec){
		boolean hasSMS = true;
        if((null == jec.getProperty(PROP_SMS_EPR))) {
        	hasSMS = false;
        }
        return hasSMS;
	}

	/**
	 * It checks whether the SMSByteIO protocol is used during the creation 
	 * of the job execution context.
	 * */
	protected boolean isSMSEnabled(JobExecutionContext jobExecutionContext){
//		if(((UnicoreHostType)jobExecutionContext.getApplicationContext().getHostDescription().getType()).getJobDirectoryMode() == JobDirectoryMode.SMS_BYTE_IO) {
//			return true;
//		}
		return false;
	}
	

}
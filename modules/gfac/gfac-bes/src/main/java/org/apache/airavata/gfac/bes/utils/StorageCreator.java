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
package org.apache.airavata.gfac.bes.utils;

import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import org.oasisOpen.docs.wsrf.sg2.EntryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unigrids.services.atomic.types.PropertyType;
import org.unigrids.x2006.x04.services.smf.CreateSMSDocument;
import org.unigrids.x2006.x04.services.smf.StorageBackendParametersDocument.StorageBackendParameters;
import org.unigrids.x2006.x04.services.smf.StorageDescriptionType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.StorageFactory;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.StorageFactoryClient;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;
import de.fzj.unicore.wsrflite.xmlbeans.sg.Registry;


import eu.unicore.util.httpclient.DefaultClientConfiguration;

public class StorageCreator {
	 protected final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * the initial lifetime (in days) for newly created SMSs
	 */
	private int initialLifeTime;

	/**
	 * factory URL to use
	 */
	private String factoryUrl;

	/**
	 * site where to create the storage
	 */
	private String siteName;

	/**
	 * storage type to create
	 */
	private String storageType;

	private DefaultClientConfiguration secProps;
	
	private String userName;
	
	public StorageCreator(DefaultClientConfiguration secProps, String besUrl, int initialLifetime, String storageType, String userName) {
		this.secProps = secProps; 
		this.factoryUrl = getStorageFactoryUrl(besUrl);
		this.storageType = storageType;
		this.initialLifeTime = initialLifetime;
		this.userName = userName;
	}
	
	
	public StorageCreator(DefaultClientConfiguration secProps, String besUrl, int initialLifetime, String userName) {
		this.secProps = secProps; 
		this.factoryUrl = getStorageFactoryUrl(besUrl);
		this.initialLifeTime = initialLifetime;
		this.userName = userName;
	}

	
	// The target site must have storage factory deployed with bes factory
	public StorageClient createStorage() throws Exception{
		
		if(factoryUrl == null) {
			throw new Exception("Cannot create Storage Factory Url");
		}
		
		EndpointReferenceType sfEpr= WSUtilities.makeServiceEPR(factoryUrl, StorageFactory.SMF_PORT);
		
		String dn = findServerName(factoryUrl, sfEpr);
		
		WSUtilities.addServerIdentity(sfEpr, dn);
		
		secProps.getETDSettings().setReceiver(new X500Principal(dn));
		secProps.getETDSettings().setIssuerCertificateChain(secProps.getCredential().getCertificateChain());
		
		// TODO: remove it afterwards
		if(userName != null) {
			secProps.getETDSettings().getRequestedUserAttributes2().put("xlogin", new String[]{userName});
		}
		
		StorageFactoryClient sfc = new StorageFactoryClient(sfEpr, secProps);
		
		if (log.isDebugEnabled()){
			log.debug("Using storage factory at <"+sfc.getUrl()+">");
		}
		
		StorageClient sc = null;
		try{
			sc=sfc.createSMS(getCreateSMSDocument());
			
			String addr=sc.getEPR().getAddress().getStringValue();
			log.info(addr);
			
		}catch(Exception ex){
			log.error("Could not create storage",ex);
			throw new Exception(ex);
		}

		return sc;
	}
	
	protected String findServerName(String besUrl, EndpointReferenceType smsEpr)throws Exception{
		
		int besIndex = besUrl.indexOf("StorageFactory?res");
		String ss = besUrl.substring(0, besIndex);
		ss = ss + "Registry";
		
		EndpointReferenceType eprt = WSUtilities.makeServiceEPR(ss, "default_registry", Registry.REGISTRY_PORT);
		
		RegistryClient registry = new RegistryClient(eprt, secProps);
		
		//first, check if server name is already in the EPR...
		String dn=WSUtilities.extractServerIDFromEPR(smsEpr);
		if(dn!=null){
			return dn;
		}
		//otherwise find a matching service in the registry
		String url=smsEpr.getAddress().getStringValue();
		if(url.contains("/services/"))url=url.substring(0,url.indexOf("/services"));
		if(log.isDebugEnabled()) log.debug("Checking for services at "+url);
		for(EntryType entry:registry.listEntries()){
			if(entry.getMemberServiceEPR().getAddress().getStringValue().startsWith(url)){
				dn=WSUtilities.extractServerIDFromEPR(entry.getMemberServiceEPR());
				if(dn!=null){
					return dn;
				}
			}
		}
		return null;
	}

	
	public static String getStorageFactoryUrl(String besUrl){
		int besIndex = besUrl.indexOf("BESFactory?res");
		String ss = besUrl.substring(0, besIndex);
		ss = ss + "StorageFactory?res=default_storage_factory";
		return ss;
	}
	
	/**
	 * prepare request
	 * */
	protected CreateSMSDocument getCreateSMSDocument(String ...keyValueParams){
		CreateSMSDocument in=CreateSMSDocument.Factory.newInstance();
		in.addNewCreateSMS();
		if(initialLifeTime>0){
			in.getCreateSMS().addNewTerminationTime().setCalendarValue(getTermTime());
		}
		if(storageType!=null){
			if(log.isDebugEnabled()) {
				log.debug("Will create storage of type : "+storageType);
			}
			StorageDescriptionType desc=in.getCreateSMS().addNewStorageDescription();
			desc.setStorageBackendType(storageType);
			if(keyValueParams.length>1){
				//other parameters from the cmdline as key=value
				StorageBackendParameters params=desc.addNewStorageBackendParameters();
				for(int i=1;i<keyValueParams.length;i++){
					String arg=keyValueParams[i];
					String[]sp=arg.split("=",2);
					PropertyType prop=params.addNewProperty();
					prop.setName(sp[0]);
					prop.setValue(sp[1]);
					if(log.isDebugEnabled()) {
						log.debug("Have parameter : "+arg);
					}
				}
			}
		}
		return in;
	}

	protected Calendar getTermTime(){
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, initialLifeTime);
		return c;
	}


}

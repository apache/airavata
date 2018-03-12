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

package org.apache.airavata.gfac.impl.task.utils.bes;

import de.fzj.unicore.uas.security.ProxyCertOutHandler;
import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.util.httpclient.DefaultClientConfiguration;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.RequestData;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class UNICORESecurityContext extends X509SecurityContext {

	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(UNICORESecurityContext.class);
	private DefaultClientConfiguration secProperties;
	
	
	public UNICORESecurityContext(CredentialReader credentialReader, RequestData requestData) {
		super(credentialReader, requestData);
	}
	
	

	public DefaultClientConfiguration getDefaultConfiguration(Boolean enableMessageLogging) throws GFacException, ApplicationSettingsException {
		try{
			X509Credential cred = getX509Credentials();
			secProperties = new DefaultClientConfiguration(dcValidator, cred);
			setExtraSettings();
		}
		catch (Exception e) {
			throw new GFacException(e.getMessage(), e); 
		} 
		if(enableMessageLogging) secProperties.setMessageLogging(true);
		
		return secProperties;
	}

	public DefaultClientConfiguration getDefaultConfiguration(Boolean enableMessageLogging,
															  UserConfigurationDataModel userDataModel)
			throws GFacException, ApplicationSettingsException {

		X509Credential cred = null;
		try{
			boolean genCert = userDataModel.isGenerateCert();
				if(genCert) {
					String userDN = userDataModel.getUserDN();
					if (userDN == null || "".equals(userDN)){
						log.warn("Cannot generate cert, falling back to GFAC configured MyProxy credentials");
						return getDefaultConfiguration(enableMessageLogging);
					}
					else {
						log.info("Generating X.509 certificate for: "+userDN);
						try {
							String caCertPath = ServerSettings.getSetting(BESConstants.PROP_CA_CERT_PATH, "");
							String caKeyPath = ServerSettings.getSetting(BESConstants.PROP_CA_KEY_PATH, "");
							String caKeyPass = ServerSettings.getSetting(BESConstants.PROP_CA_KEY_PASS, "");
							
							if(caCertPath.equals("") || caKeyPath.equals("")) {
								throw new Exception("CA certificate or key file path missing in the properties file. "
										+ "Please make sure " + BESConstants.PROP_CA_CERT_PATH + " or "
										+ BESConstants.PROP_CA_KEY_PATH + " are not empty.");
							}
							
							if("".equals(caKeyPass)) {
								log.warn("Caution: CA key has no password. For security reasons it is highly recommended to set a CA key password");
							}
							cred = generateShortLivedCredential(userDN, caCertPath, caKeyPath, caKeyPass);
						}catch (Exception e){
							throw new GFacException("Error occured while generating a short lived credential for user:"+userDN, e);
						}
						
					}
				}else  {
					return getDefaultConfiguration(enableMessageLogging);
				}
				
			secProperties = new DefaultClientConfiguration(dcValidator, cred);
			setExtraSettings();
		}
		catch (Exception e) {
			throw new GFacException(e.getMessage(), e); 
		} 
		secProperties.getETDSettings().setExtendTrustDelegation(true);
		if(enableMessageLogging) secProperties.setMessageLogging(true);
//		secProperties.setDoSignMessage(true);
		secProperties.getETDSettings()
				.setIssuerCertificateChain(secProperties.getCredential().getCertificateChain());
		
		return secProperties;
	}

	
	/**
	 * Get server signed credentials. Each time it is invoked new certificate 
	 * is returned.
	 * 
	 * @param userID
	 * @param userDN
	 * @param caCertPath
	 * @param caKeyPath
	 * @param caKeyPwd
	 * @return
	 * @throws GFacException
	 */
	public DefaultClientConfiguration getServerSignedConfiguration(String userID, String userDN, String caCertPath, String caKeyPath, String caKeyPwd) throws GFacException {
		try {
			KeyAndCertCredential cred = SecurityUtils.generateShortLivedCertificate(userDN,caCertPath,caKeyPath,caKeyPwd);
			secProperties = new DefaultClientConfiguration(dcValidator, cred);
			setExtraSettings();
		} catch (Exception e) {
			throw new GFacException(e.getMessage(), e);
		}

		return secProperties;
	}

	
	

	
	private void setExtraSettings(){
		secProperties.getETDSettings().setExtendTrustDelegation(true);

		secProperties.setDoSignMessage(true);

		String[] outHandlers = secProperties.getOutHandlerClassNames();
		
		Set<String> outHandlerLst = null;

		// timeout in milliseconds
		Properties p = secProperties.getExtraSettings();
		
		if(p == null) {
			p = new Properties();
		}
		
		p.setProperty("http.connection.timeout", "5000");
		p.setProperty("http.socket.timeout", "5000");
		
		if (outHandlers == null) {
			outHandlerLst = new HashSet<String>();
		} else {
			outHandlerLst = new HashSet<String>(Arrays.asList(outHandlers));
		}

		outHandlerLst.add(ProxyCertOutHandler.class.getName());
		
		secProperties.setOutHandlerClassNames(outHandlerLst
				.toArray(new String[outHandlerLst.size()]));
		
		secProperties.getETDSettings().setExtendTrustDelegation(true);

	}


    private String getCNFromUserDN(String userDN) {
        return X500NameUtils.getAttributeValues(userDN, BCStyle.CN)[0];

    }


}
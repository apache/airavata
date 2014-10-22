package org.apache.airavata.gfac.bes.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.bes.utils.SecurityUtils;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.unicore.util.httpclient.DefaultClientConfiguration;

public class UNICORESecurityContext extends X509SecurityContext {

	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(UNICORESecurityContext.class);
	private DefaultClientConfiguration secProperties;
	
	
	public UNICORESecurityContext(CredentialReader credentialReader, RequestData requestData) {
		super(credentialReader, requestData);
	}
	
	
	/**
	 * Get client configuration from MyProxy credentials. 
	 * 
	 * @return an instance of the default client configuration
	 * @throws GFacException
	 * @throws ApplicationSettingsException 
	 * @throws GFacProviderException
	 */
	public DefaultClientConfiguration getDefaultConfiguration() throws GFacException, ApplicationSettingsException {
		try{
			X509Credential cred = getX509Credentials();
			secProperties = new DefaultClientConfiguration(dcValidator, cred);
//			setExtraSettings();
		}
		catch (Exception e) {
			throw new GFacException(e.getMessage(), e); 
		} 
		secProperties.getETDSettings().setExtendTrustDelegation(true);
//		secProperties.setMessageLogging(true);
//		secProperties.setDoSignMessage(true);
//		secProperties.getETDSettings().setIssuerCertificateChain(secProperties.getCredential().getCertificateChain());
		
		
		
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
	 * @throws GFacProviderException
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
		
		p.setProperty("http.connection.timeout", "300000");
		p.setProperty("http.socket.timeout", "300000");
		
		secProperties.setExtraSettings(p);

		if (outHandlers == null) {
			outHandlerLst = new HashSet<String>();
		} else {
			outHandlerLst = new HashSet<String>(Arrays.asList(outHandlers));
		}

		outHandlerLst.add("de.fzj.unicore.uas.security.ProxyCertOutHandler");

		secProperties.setOutHandlerClassNames(outHandlerLst
				.toArray(new String[outHandlerLst.size()]));
	}


    private String getCNFromUserDN(String userDN) {
        return X500NameUtils.getAttributeValues(userDN, BCStyle.CN)[0];

    }


}
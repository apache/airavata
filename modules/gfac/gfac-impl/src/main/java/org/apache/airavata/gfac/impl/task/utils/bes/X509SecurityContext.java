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
package org.apache.airavata.gfac.impl.task.utils.bes;


import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.helpers.CertificateHelpers;
import eu.emi.security.authn.x509.helpers.proxy.X509v3CertificateBuilder;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.DirectoryCertChainValidator;
import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.RequestData;
import org.apache.airavata.gfac.core.context.AbstractSecurityContext;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.File;
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
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Handles X509 Certificate based security.
 */
public class X509SecurityContext extends AbstractSecurityContext {

	private static final long serialVersionUID = 1L;

	protected static final Logger log = LoggerFactory.getLogger(X509SecurityContext.class);
    
    /*
     * context name
     */
    public static final String X509_SECURITY_CONTEXT = "x509.security.context";
    
    public static final int CREDENTIAL_RENEWING_THRESH_HOLD = 10 * 90;
    
    protected static DirectoryCertChainValidator dcValidator;
    
    private X509Credential x509Credentials= null;

    static {
        try {
			setUpTrustedCertificatePath();
			// set up directory based trust validator
			dcValidator = getTrustedCerts();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
    }
    

    public static void setUpTrustedCertificatePath(String trustedCertificatePath) {

        File file = new File(trustedCertificatePath);

        if (!file.exists() || !file.canRead()) {
            File f = new File(".");
            log.info("Current directory " + f.getAbsolutePath());
            throw new RuntimeException("Cannot read trusted certificate path " + trustedCertificatePath);
        } else {
            System.setProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY, file.getAbsolutePath());
        }
    }

    private static void setUpTrustedCertificatePath() throws ApplicationSettingsException {

        String trustedCertificatePath  = ServerSettings.getSetting(Constants.TRUSTED_CERT_LOCATION);

        setUpTrustedCertificatePath(trustedCertificatePath);
    }

    /**
     * Gets the trusted certificate path. Trusted certificate path is stored in "X509_CERT_DIR"
     * system property.
     * @return The trusted certificate path as a string.
     */
    public static String getTrustedCertificatePath() {
        return System.getProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY);
    }


    public X509SecurityContext(CredentialReader credentialReader, RequestData requestData) {
        super(credentialReader, requestData);
    }


    /**
     * Gets X509Credentials. The process is as follows;
     * If credentials were queried for the first time create credentials.
     *   1. Try creating credentials using certificates stored in the credential store
     *   2. If 1 fails use user name and password to create credentials
     * @return x509credentials (from CANL security API)
     * @throws org.apache.airavata.common.exception.ApplicationSettingsException
     */
    public X509Credential getX509Credentials() throws GFacException, ApplicationSettingsException {
    	
    	if(getCredentialReader() == null) {
    		return getDefaultCredentials();
    	}
    	
        if (x509Credentials == null) {

            try {
                x509Credentials = getCredentialsFromStore();
            } catch (Exception e) {
                log.error("An exception occurred while retrieving credentials from the credential store. " +
                        "Will continue with my proxy user name and password.", e);
            }

            // If store does not have credentials try to get from user name and password
            if (x509Credentials == null) {
                x509Credentials = getDefaultCredentials();
            }

            // if still null, throw an exception
            if (x509Credentials == null) {
                throw new GFacException("Unable to retrieve my proxy credentials to continue operation.");
            }
        } else {
            try {
            	
            	final long remainingTime = x509Credentials.getCertificate().getNotAfter().getTime() - new Date().getTime();
            	
                if (remainingTime < CREDENTIAL_RENEWING_THRESH_HOLD) {
                	//                    return renewCredentials();
                	log.warn("Do not support credentials renewal");
                }
                
                log.info("Fall back to get new default credentials");
                
                try {
                	x509Credentials.getCertificate().checkValidity();
                }catch(Exception e){
                	x509Credentials = getDefaultCredentials();
                }
                
            } catch (Exception e) {
                throw new GFacException("Unable to retrieve remaining life time from credentials.", e);
            }
        }

        return x509Credentials;
    }

        /**
     * Reads the credentials from credential store.
     * @return If token is found in the credential store, will return a valid credential. Else returns null.
     * @throws Exception If an error occurred while retrieving credentials.
     */
    public  X509Credential getCredentialsFromStore() throws Exception {

        if (getCredentialReader() == null) {
            return null;
        }

        Credential credential = getCredentialReader().getCredential(getRequestData().getGatewayId(),
                getRequestData().getTokenId());

        if (credential != null) {
            if (credential instanceof CertificateCredential) {

                log.info("Successfully found credentials for token id - " + getRequestData().getTokenId() +
                        " gateway id - " + getRequestData().getGatewayId());

                CertificateCredential certificateCredential = (CertificateCredential) credential;

                X509Certificate[] certificates = certificateCredential.getCertificates();

                KeyAndCertCredential keyAndCert = new KeyAndCertCredential(certificateCredential.getPrivateKey(), certificates); 
                
                return keyAndCert;
                //return new GlobusGSSCredentialImpl(newCredential,
                //        GSSCredential.INITIATE_AND_ACCEPT);
            } else {
                log.info("Credential type is not CertificateCredential. Cannot create mapping globus credentials. " +
                        "Credential type - " + credential.getClass().getName());
            }
        } else {
            log.info("Could not find credentials for token - " + getRequestData().getTokenId() + " and "
                    + "gateway id - " + getRequestData().getGatewayId());
        }

        return null;
    }

    /**
     * Gets the default proxy certificate.
     * @return Default my proxy credentials.
     * @throws org.apache.airavata.common.exception.ApplicationSettingsException
     */
    public X509Credential getDefaultCredentials() throws GFacException, ApplicationSettingsException {
    	MyProxyLogon logon = new MyProxyLogon();
    	logon.setValidator(dcValidator);
    	logon.setHost(getRequestData().getMyProxyServerUrl());
    	logon.setPort(getRequestData().getMyProxyPort());
    	logon.setUsername(getRequestData().getMyProxyUserName());
    	logon.setPassphrase(getRequestData().getMyProxyPassword().toCharArray());
    	logon.setLifetime(getRequestData().getMyProxyLifeTime());
    	
        	try {
				logon.connect();
				logon.logon();
				logon.getCredentials();
				logon.disconnect();
				PrivateKey pk=logon.getPrivateKey();
				return new KeyAndCertCredential(pk, new X509Certificate[]{logon.getCertificate()});
			}  catch (Exception e) {
				throw new GFacException("An error occurred while retrieving default security credentials.", e);
			}
    }

	private static  DirectoryCertChainValidator getTrustedCerts() throws Exception{
		String certLocation = getTrustedCertificatePath();
		List<String> trustedCert = new ArrayList<String>();
		trustedCert.add(certLocation + "/*.0");
		trustedCert.add(certLocation + "/*.pem");
		DirectoryCertChainValidator dcValidator = new DirectoryCertChainValidator(trustedCert, Encoding.PEM, -1, 60000, null);
		return dcValidator;
	}
	
	private String getCNFromUserDN(String userDN) {
		return X500NameUtils.getAttributeValues(userDN, BCStyle.CN)[0];
	}
	
	public KeyAndCertCredential generateShortLivedCredential(String userDN, String caCertPath, String caKeyPath,
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
		InputStream isKey, isCert;
		isKey = isCert = null;
		try {
		isKey = new FileInputStream(caKeyPath);
		PrivateKey pk = CertificateUtils.loadPrivateKey(isKey, Encoding.PEM, password.toCharArray());

		isCert = new FileInputStream(caCertPath);
		X509Certificate caCert = CertificateUtils.loadCertificate(isCert, Encoding.PEM);
		return new KeyAndCertCredential(pk, new X509Certificate[] { caCert });
		} finally {
			if (isKey != null){
				isKey.close();
			}
			if (isCert != null) {
				isCert.close();
			}
		}
	}
	 
    
}

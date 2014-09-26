package org.apache.airavata.gfac.bes.security;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.emi.security.authn.x509.helpers.CertificateHelpers;
import eu.emi.security.authn.x509.helpers.proxy.X509v3CertificateBuilder;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.DirectoryCertChainValidator;
import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
import eu.emi.security.authn.x509.impl.X500NameUtils;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.PEMCredential;
import eu.unicore.util.httpclient.DefaultClientConfiguration;

public class UNICORESecurityContext extends GSISecurityContext {

	protected static final Logger log = LoggerFactory.getLogger(UNICORESecurityContext.class);
	private DefaultClientConfiguration secProperties;
	protected static DirectoryCertChainValidator dcValidator;
	
	public static final String UNICORE_SECURITY_CONTEXT = "unicore";
	
	public UNICORESecurityContext(CredentialReader credentialReader, RequestData requestData) {
		super(credentialReader, requestData);
	}
	 static {
		 try {
			dcValidator = getTrustedCerts();
		} catch (Exception e) {
			log.error("Cannot construct trust validator.", e);
		}
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
		GlobusGSSCredentialImpl gss = (GlobusGSSCredentialImpl) getGssCredentials();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedOutputStream bufos = new BufferedOutputStream(bos);
		ByteArrayInputStream bis = null;
		BufferedInputStream bufis = null;
		try{
			gss.getX509Credential().save(bufos);
			bufos.flush();
			char[] c = null;
			bis = new ByteArrayInputStream(bos.toByteArray());
			bufis = new BufferedInputStream(bis);
			PEMCredential cred = new PEMCredential(bufis, c);
			secProperties = new DefaultClientConfiguration(dcValidator, cred);
//			setExtraSettings();
		}
		catch (Exception e) {
			throw new GFacException(e.getMessage(), e); 
		} 
		finally{
			try {
				if(bos!=null)bos.close();
				if(bufos!=null)bufos.close();
				if(bis!=null)bis.close();
				if(bufis!=null)bufis.close();
			} catch (IOException e) {
				log.error("Error closing IO streams.", e);
			}
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
			KeyAndCertCredential cred = generateShortLivedCertificate(userDN,caCertPath,caKeyPath,caKeyPwd);
			secProperties = new DefaultClientConfiguration(dcValidator, cred);
			setExtraSettings();
		} catch (Exception e) {
			throw new GFacException(e.getMessage(), e);
		}

		return secProperties;
	}

	
	private KeyAndCertCredential generateShortLivedCertificate(String userDN,
			String caCertPath, String caKeyPath, String caPwd) throws Exception {
		final long CredentialGoodFromOffset = 1000L * 60L * 15L; // 15 minutes
		// ago

		final long startTime = System.currentTimeMillis()
				- CredentialGoodFromOffset;
		final long endTime = startTime + 30 * 3600 * 1000;

		String keyLengthProp = "1024";
		int keyLength = Integer.parseInt(keyLengthProp);
		String signatureAlgorithm = "SHA1withRSA";

		KeyAndCertCredential caCred = getCACredential(caCertPath, caKeyPath,
				caPwd);

		KeyPairGenerator kpg = KeyPairGenerator.getInstance(caCred.getKey()
				.getAlgorithm());
		kpg.initialize(keyLength);
		KeyPair pair = kpg.generateKeyPair();

		X500Principal subjectDN = new X500Principal(userDN);
		Random rand = new Random();

		SubjectPublicKeyInfo publicKeyInfo;
		try {
			publicKeyInfo = SubjectPublicKeyInfo
					.getInstance(new ASN1InputStream(pair.getPublic()
							.getEncoded()).readObject());
		} catch (IOException e) {
			throw new InvalidKeyException("Can not parse the public key"
					+ "being included in the short lived certificate", e);
		}

		X500Name issuerX500Name = CertificateHelpers.toX500Name(caCred
				.getCertificate().getSubjectX500Principal());

		X500Name subjectX500Name = CertificateHelpers.toX500Name(subjectDN);

		X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
				issuerX500Name, new BigInteger(20, rand), new Date(startTime),
				new Date(endTime), subjectX500Name, publicKeyInfo);

		AlgorithmIdentifier sigAlgId = X509v3CertificateBuilder
				.extractAlgorithmId(caCred.getCertificate());

		X509Certificate certificate = certBuilder.build(caCred.getKey(),
				sigAlgId, signatureAlgorithm, null, null);

		certificate.checkValidity(new Date());
		certificate.verify(caCred.getCertificate().getPublicKey());
		KeyAndCertCredential result = new KeyAndCertCredential(
				pair.getPrivate(), new X509Certificate[] { certificate,
						caCred.getCertificate() });

		return result;
	}

	private KeyAndCertCredential getCACredential(String caCertPath,
			String caKeyPath, String password) throws Exception {
		InputStream isKey = new FileInputStream(caKeyPath);
		PrivateKey pk = CertificateUtils.loadPrivateKey(isKey, Encoding.PEM,
				password.toCharArray());

		InputStream isCert = new FileInputStream(caCertPath);
		X509Certificate caCert = CertificateUtils.loadCertificate(isCert,
				Encoding.PEM);

		if (isKey != null)
			isKey.close();
		if (isCert != null)
			isCert.close();

		return new KeyAndCertCredential(pk, new X509Certificate[] { caCert });
	}
	
	private static  DirectoryCertChainValidator getTrustedCerts() throws Exception{
		String certLocation = getTrustedCertificatePath();
		List<String> trustedCert = new ArrayList<String>();
		trustedCert.add(certLocation + "/*.0");
		trustedCert.add(certLocation + "/*.pem");
		DirectoryCertChainValidator dcValidator = new DirectoryCertChainValidator(trustedCert, Encoding.PEM, -1, 60000, null);
		return dcValidator;
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
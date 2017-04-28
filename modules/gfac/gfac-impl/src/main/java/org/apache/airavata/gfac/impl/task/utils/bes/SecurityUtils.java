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

import eu.emi.security.authn.x509.helpers.CertificateHelpers;
import eu.emi.security.authn.x509.helpers.proxy.X509v3CertificateBuilder;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.RequestData;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;

public class SecurityUtils {
	
	private final static Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	
	public static UNICORESecurityContext getSecurityContext(ProcessContext processContext) throws GFacException {

		if (processContext.getJobSubmissionProtocol().equals(JobSubmissionProtocol.UNICORE)) {
			String credentialStoreToken = processContext.getTokenId(); // set by the framework
			RequestData requestData;
			requestData = new RequestData(processContext.getProcessModel().getUserDn());
			requestData.setTokenId(credentialStoreToken);
			CredentialReader credentialReader = null;
			try {
				credentialReader = GFacUtils.getCredentialReader();
				if (credentialReader == null) {
					throw new GFacException("Credential reader returns null");
				}
			} catch (Exception e) {
				throw new GFacException("Error while initializing credential reader");
			}
			return new UNICORESecurityContext(credentialReader, requestData);
		} else {
			throw new GFacException("Only support UNICORE job submissions, invalid job submission protocol " +
					processContext.getJobSubmissionProtocol().name());
		}
	}
	
	public static final KeyAndCertCredential generateShortLivedCertificate(String userDN,
			String caCertPath, String caKeyPath, String caPwd) throws Exception {
		final long CredentialGoodFromOffset = 1000L * 60L * 15L; // 15 minutes
		// ago

		final long startTime = System.currentTimeMillis() - CredentialGoodFromOffset;
		final long endTime = startTime + 30 * 3600 * 1000;

		final String keyLengthProp = "1024";
		int keyLength = Integer.parseInt(keyLengthProp);
		final String signatureAlgorithm = "SHA1withRSA";

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
	
	public static KeyAndCertCredential getCACredential(String caCertPath,
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

	
	
}

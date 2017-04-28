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
package org.apache.airavata.credential.store.credential.impl.ssh;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.SSHCredentialWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

/**
 * A class which generates an SSH credential
 */
public class SSHCredentialGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(SSHCredentialWriter.class);
	
	/**
	 * 
	 * @return a SSH Credential generated and encrypted using a randomly generated password
	 * @throws CredentialStoreException 
	 */
	public SSHCredential generateCredential(String tokenId) throws CredentialStoreException {
        JSch jsch=new JSch();
        try {
            KeyPair kpair=KeyPair.genKeyPair(jsch, KeyPair.RSA);
            File file;
			
				file = File.createTempFile("id_rsa", "");
			
            String fileName = file.getAbsolutePath();

            String password = generateRandomString();
            // We are encrypting the private key with the hash of (tokenId+password). 
            // Any client which wants to use this private key will also generate a hash and then use it to decrypt the key.  
            kpair.writePrivateKey(fileName,password.getBytes());
            kpair.writePublicKey(fileName + ".pub"  , "");
            kpair.dispose();
            byte[] priKey = FileUtils.readFileToByteArray(new File(fileName));
            byte[] pubKey = FileUtils.readFileToByteArray(new File(fileName + ".pub"));
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setPrivateKey(priKey);
            sshCredential.setPublicKey(pubKey);
            sshCredential.setPassphrase(password);
            return sshCredential;
		} catch (IOException e) {
			logger.error("IO Exception when creating SSH credential ",e);
			throw new CredentialStoreException("Unable to generate SSH Credential", e);
		} catch (JSchException e) {
			logger.error("JSch SSH credential creation exception ",e);
			throw new CredentialStoreException("Unable to generate SSH Credential. JSch exception ", e);
		}
	}
	
	private String generateHash(String tokenId, String password) {
        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = password.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new String( md.digest(bytesOfMessage));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        return  null;
    }

	// Generate a random alphanumberic string of 16 characters length
	private String generateRandomString() {
		return RandomStringUtils.randomAlphanumeric(16);
	}
}

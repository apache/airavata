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

package org.apache.airavata.gfac.provider.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.sshtools.j2ssh.util.Base64;
import org.bouncycastle.openssl.PEMWriter;

import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/*This class holds the utility methods used for the EC2Provider*/
public class EC2ProviderUtil {

    /**
     * Builds a key pair with the given AmazonEC2Client and the generated key will have
     * the name keyPairName.
     *
     * @param ec2 ec2client
     * @param keyPairName name for the generated key pair
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeySpecException InvalidKeySpecException
     * @throws AmazonServiceException AmazonServiceException
     * @throws AmazonClientException AmazonClientException
     * @throws IOException IOException
     */
    public static void buildKeyPair(AmazonEC2Client ec2, String keyPairName)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            AmazonServiceException, AmazonClientException, IOException {
        boolean newKey = false;

        String privateKeyFilePath = System.getProperty("user.home") + "/.ssh/" + keyPairName;
        File privateKeyFile = new File(privateKeyFilePath);
        File publicKeyFile = new File(privateKeyFilePath + ".pub");

        /* Check if Key-pair already created on the server */
        if (!privateKeyFile.exists()) {

            // check folder and create if it does not exist
            File sshDir = new File(System.getProperty("user.home") + "/.ssh/");
            if (!sshDir.exists())
                sshDir.mkdir();

            // Generate a 1024-bit RSA key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keypair = keyGen.genKeyPair();

            FileOutputStream fos = null;

            // Store Public Key.
            try {
                fos = new FileOutputStream(privateKeyFilePath + ".pub");
                fos.write(Base64.encodeBytes(keypair.getPublic().getEncoded(), true).getBytes());
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }
            }

            // Store Private Key.
            try {
                fos = new FileOutputStream(privateKeyFilePath);
                StringWriter stringWriter = new StringWriter();

                /* Write in PEM format (openssl support) */
                PEMWriter pemFormatWriter = new PEMWriter(stringWriter);
                pemFormatWriter.writeObject(keypair.getPrivate());
                pemFormatWriter.close();
                fos.write(stringWriter.toString().getBytes());
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }
            }

            privateKeyFile.setWritable(false, false);
            privateKeyFile.setExecutable(false, false);
            privateKeyFile.setReadable(false, false);
            privateKeyFile.setReadable(true);
            privateKeyFile.setWritable(true);

            // set that this key is just created
            newKey = true;
        }

        /* Read Public Key */
        String encodedPublicKey = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(publicKeyFile));
            encodedPublicKey = br.readLine();
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException ioe) {
                    throw ioe;
                }
            }
        }

        /* Generate key pair in Amazon if necessary */
        try {
            /* Get current key pair in Amazon */
            DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();
            ec2.describeKeyPairs(describeKeyPairsRequest.withKeyNames(keyPairName));

            /* If key exists and new key is created, delete old key and replace
             * with new one. Else, do nothing */
            if (newKey) {
                DeleteKeyPairRequest deleteKeyPairRequest = new DeleteKeyPairRequest(keyPairName);
                ec2.deleteKeyPair(deleteKeyPairRequest);
                ImportKeyPairRequest importKeyPairRequest = new ImportKeyPairRequest(keyPairName, encodedPublicKey);
                ec2.importKeyPair(importKeyPairRequest);
            }

        } catch (AmazonServiceException ase) {
            /* Key doesn't exists, import new key. */
            if (ase.getErrorCode().equals("InvalidKeyPair.NotFound")) {
                ImportKeyPairRequest importKeyPairRequest = new ImportKeyPairRequest(keyPairName, encodedPublicKey);
                ec2.importKeyPair(importKeyPairRequest);
            } else {
                throw ase;
            }
        }
    }
}
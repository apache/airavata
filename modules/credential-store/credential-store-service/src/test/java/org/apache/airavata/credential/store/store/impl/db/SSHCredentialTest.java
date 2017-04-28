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
package org.apache.airavata.credential.store.store.impl.db;


import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.SSHCredentialWriter;
import org.apache.airavata.credential.store.util.TokenGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SSHCredentialTest {

    public static void main(String[] args) {
        String jdbcURL = "jdbc:mysql://gw85.iu.xsede.org:3306/airavata_gw119";
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String userName = "gtaDevUser";
        String password = "gtaDevPWD";
        String gatewayId = "phasta";
        String privateKeyPath = "/Users/chathuri/Desktop/ssh_gw111/id_rsa";
        String pubKeyPath = "/Users/chathuri/Desktop/ssh_gw111/id_rsa.pub";

        try {
            DBUtil dbUtil = new DBUtil(jdbcURL, userName, password, jdbcDriver);
            SSHCredentialWriter writer = new SSHCredentialWriter(dbUtil);
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setGateway(gatewayId);
            String token = TokenGenerator.generateToken(gatewayId, null);
            sshCredential.setToken(token);
            sshCredential.setPortalUserName("phasta");
            sshCredential.setDescription("dummy creds for testing");
            FileInputStream privateKeyStream = new FileInputStream(privateKeyPath);
            File filePri = new File(privateKeyPath);
            byte[] bFilePri = new byte[(int) filePri.length()];
            privateKeyStream.read(bFilePri);
            FileInputStream pubKeyStream = new FileInputStream(pubKeyPath);
            File filePub = new File(pubKeyPath);
            byte[] bFilePub = new byte[(int) filePub.length()];
            pubKeyStream.read(bFilePub);
            privateKeyStream.close();
            pubKeyStream.close();
            sshCredential.setPrivateKey(bFilePri);
            sshCredential.setPublicKey(bFilePub);
            sshCredential.setPassphrase("ultrascan");
            writer.writeCredentials(sshCredential);
            System.out.println(token);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        } catch (CredentialStoreException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}

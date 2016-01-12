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
package org.apache.airavata.data.manager.core.remote.client.scp;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

@Ignore
public class SCPStorageClientTest {
    private final static Logger logger = LoggerFactory.getLogger(SCPStorageClientTest.class);

    @Test
    public void testSCPStorageClient() throws Exception {
        File privateKey = new File("/Users/supun/.ssh/id_rsa");
        byte[] privateKeyBytes = IOUtils.toByteArray(new FileInputStream(privateKey));

        File publicKey = new File("/Users/supun/.ssh/id_rsa.pub");
        byte[] publicKeyBytes = IOUtils.toByteArray(new FileInputStream(publicKey));

        String passPhrase = "";
        byte[] passPhraseBytes = passPhrase.getBytes();

        SCPStorageClient scpStorageClient = new SCPStorageClient("gw75.iu.xsede.org", 22, "pga", privateKeyBytes,
                publicKeyBytes, passPhraseBytes);
        File file = scpStorageClient.readFile("/var/www/portals/gateway-user-data/testdrive/test.txt");
        System.out.println("File exists ? " + file.exists());
        scpStorageClient.writeFile(file, "/var/www/portals/gateway-user-data/testdrive/test2.txt");
        file.delete();
        System.out.println("File exists ? " + file.exists());
    }
}
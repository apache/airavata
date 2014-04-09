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
package org.apache.airavata.gsi.ssh.config;

import java.io.File;

import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.gsi.ssh.util.SSHUtils;
import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SCPFromTestWithMyProxyAuth {
    private String myProxyUserName;
    private String myProxyPassword;
    private String certificateLocation;
    private String lFilePath;
    private String rFilePath;

    @BeforeTest
    public void setUp() throws Exception {
        System.out.println("Test case name " + this.getClass().getName());
//        System.setProperty("myproxy.user", "ogce");
//        System.setProperty("myproxy.password", "");
//        System.setProperty("gsi.certificate.path", "/Users/lahirugunathilake/Downloads/certificates");
        myProxyUserName = System.getProperty("myproxy.user");
        myProxyPassword = System.getProperty("myproxy.password");
        certificateLocation = System.getProperty("gsi.certificate.path");

        System.out.println(myProxyPassword + "," + myProxyUserName + "," + certificateLocation);
        if ((myProxyUserName == null) || (myProxyPassword == null) || (certificateLocation == null)) {
            System.out.println(">>>>>> Please run tests with my proxy user name and password. " +
                    "E.g :- mvn clean install -Dmyproxy.user=xxx -Dmyproxy.password=xxx -Dgsi.working.directory=/path<<<<<<<" +
                    "gsi.certificate.path=/cert/path");
            throw new Exception("Need my proxy user name password to run tests.");
        }
    }


    @Test
    public void testExecuteCommand() throws Exception {
        // Create authentication
        File test = new File("test");
        if(!test.exists()){
            test.createNewFile();
        }
        lFilePath = test.getAbsolutePath();
        System.out.println(lFilePath);
        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
                7512, 17280000, certificateLocation);
        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");
        SSHUtils SSHUtils = new SSHUtils(serverInfo, authenticationInfo, this.certificateLocation, new ConfigReader());
        SSHUtils.scpTo("/tmp", lFilePath);
        Thread.sleep(1000);
        SSHUtils.scpFrom(File.separator + "tmp" + File.separator + "test", lFilePath);
        boolean delete = test.delete();
        Assert.assertTrue(delete);
    }
    /* add more test cases for each ssh operation */

}

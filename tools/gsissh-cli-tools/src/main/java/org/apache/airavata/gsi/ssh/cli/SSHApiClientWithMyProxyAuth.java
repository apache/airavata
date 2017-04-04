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
package org.apache.airavata.gfac.ssh.cli;

import org.apache.airavata.gfac.ssh.api.*;
import org.apache.airavata.gfac.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gfac.ssh.api.job.JobDescriptor;
import org.apache.airavata.gfac.ssh.config.ConfigReader;
import org.apache.airavata.gfac.ssh.impl.PBSCluster;
import org.apache.airavata.gfac.ssh.impl.RawCommandInfo;
import org.apache.airavata.gfac.ssh.impl.SystemCommandOutput;
import org.apache.airavata.gfac.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.gfac.ssh.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHApiClientWithMyProxyAuth {

    public static void main(String[]ars){
		  String myproxyServer = System.getProperty("myproxy.server");
		  String myproxyUserName = System.getProperty("myproxy.username");
		  String myproxyPassword = System.getProperty("myproxy.password");
		  String certificateLocation = System.getProperty("myproxy.cert.location");
		  String remoteCmd = System.getProperty("remote.cmd");
		  String remoteHost = System.getProperty("remote.host");
		  String remoteHostPort = System.getProperty("remote.host.port");

		  int myproxyPort=7512;
		  int myproxyTimeout=17280000;

		  if (myproxyServer == null || myproxyUserName == null || myproxyPassword == null || certificateLocation == null || remoteCmd == null || remoteHost == null || remoteHostPort == null) {
				System.out.println("Incorrect arguments provided");
				System.exit(1);
		  }

        GSIAuthenticationInfo authenticationInfo
                = new MyProxyAuthenticationInfo(myproxyUserName, myproxyPassword, myproxyServer,
                myproxyPort, myproxyTimeout, certificateLocation);

        // Create command
        CommandInfo commandInfo = new RawCommandInfo(remoteCmd);

        // Server info
        ServerInfo serverInfo = new ServerInfo(myproxyUserName, remoteHost, Integer.parseInt(remoteHostPort));

        // Output
        CommandOutput commandOutput = new SystemCommandOutput();

        // Execute command
        try {
            CommandExecutor.executeCommand(commandInfo, serverInfo, authenticationInfo, commandOutput, new ConfigReader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

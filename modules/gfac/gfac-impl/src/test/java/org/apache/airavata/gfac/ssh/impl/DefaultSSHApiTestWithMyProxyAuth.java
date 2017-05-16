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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.gfac.ssh.impl;
//
//import org.apache.airavata.gfac.core.SSHApiException;
//import org.apache.airavata.gfac.core.cluster.CommandInfo;
//import org.apache.airavata.gfac.core.cluster.CommandOutput;
//import org.apache.airavata.gfac.core.cluster.RawCommandInfo;
//import org.apache.airavata.gfac.core.cluster.ServerInfo;
//import org.apache.airavata.gfac.gsi.ssh.api.CommandExecutor;
//import org.apache.airavata.gfac.gsi.ssh.config.ConfigReader;
//import org.apache.airavata.gfac.impl.HPCRemoteCluster;
//import org.apache.airavata.gfac.gsi.ssh.impl.SystemCommandOutput;
//import org.apache.airavata.gfac.gsi.ssh.impl.authentication.DefaultPublicKeyAuthentication;
//import org.apache.commons.io.IOUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//
//public class DefaultSSHApiTestWithMyProxyAuth {
//    private static final Logger log = LoggerFactory.getLogger(HPCRemoteCluster.class);
//
//
//
//    public void tearDown() throws Exception {
//    }
//
//
//    public static void main(String[]ars) throws IOException {
//         String myProxyUserName = "lg11w";
//
////        DefaultPasswordAuthenticationInfo authenticationInfo
////                = new DefaultPasswordAuthenticationInfo("");
//        byte[] privateKey = IOUtils.toByteArray(new BufferedInputStream(new FileInputStream("/Users/lginnali/.ssh/id_dsa")));
//        byte[] publicKey = IOUtils.toByteArray(new BufferedInputStream(new FileInputStream("/Users/lginnali/.ssh/id_dsa.pub")));
//        DefaultPublicKeyAuthentication authenticationInfo = new DefaultPublicKeyAuthentication(privateKey,publicKey,"");
//
//        // Create command
//        CommandInfo commandInfo = new RawCommandInfo("source /etc/bashrc; bsub </home/lg11w/mywork/sshEchoExperiment_9d267072-ca65-4ca8-847a-cd3d130f6050/366787899.lsf");
//
//        // Server info
//        //Stampede
////        ServerInfo serverInfo = new ServerInfo(myProxyUserName, "stampede.tacc.utexas.edu", 2222);
//        //Trestles
////        ServerInfo serverInfo = new ServerInfo(myProxyUserName, "trestles.sdsc.xsede.org", 22);
//
//        //Lonestar
//         ServerInfo serverInfo = new ServerInfo(myProxyUserName, "ghpcc06.umassrc.org", 22);
//        // Output
//        CommandOutput commandOutput = new SystemCommandOutput();
//
//        // Execute command
//        try {
//            CommandExecutor.executeCommand(commandInfo, serverInfo, authenticationInfo, commandOutput, new ConfigReader());
//        } catch (SSHApiException e) {
//            log.error(e.getMessage(), e);
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//
//
//
//}

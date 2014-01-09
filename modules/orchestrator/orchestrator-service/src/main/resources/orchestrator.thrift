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

namespace java org.apache.airavata.orchestrator


struct HPCSettings {
    1:optional string jobManager;
    2:optional i32 cpuCount;
    3:optional i32 nodeCount;
    4:optional string queueName;
    5:optional i32 maxWalltime;
}

struct HostSchedulingSettings {
    1:optional string hostID;
    2:optional bool isWSGramPreferred;
    3:optional string gatekeeperEPR;
}

struct NameValuePairType {
    1:optional string name;
    2:optional string value;
    3:optional string description;
}


struct WorkflowSchedulingSettings {
    1:optional list<NodeSettings> nodeSettingsList;
}

struct OutputDataSettings {
    1:optional string nodeID;
    2:optional string outputdataDir;
    3:optional string dataRegURL;
    4:optional bool isdataPersisted;
}

struct WorkflowOutputDataSettings{
    1:optional list<OutputDataSettings> outputDataSettingsList;
}

struct SSHAuthenticationSettings {
    1:optional string accessKeyID;
    2:optional string secretAccessKey;
}

struct MyProxyRepositorySettings {
    1:optional string userName;
    2:optional string password;
    3:optional string myproxyServer;
    4:optional i32 lifetime;
}

struct CredentialStoreSecuritySettings {
    1:optional string tokenId;
    2:optional string portalUser;
    3:optional string gatewayID;
}

struct AmazonWebServicesSettings {
    1:optional string accessKey;
    2:optional string amiID;
    3:optional string instanceID;
    4:optional string instanceType;
    5:optional string secretAccessKey;
    6:optional string username;
}

struct SecuritySettings {
    1:optional AmazonWebServicesSettings amazonWSSettings;
    2:optional CredentialStoreSecuritySettings credentialStoreSettings;
    3:optional MyProxyRepositorySettings myproxySettings;
    4:optional SSHAuthenticationSettings sshAuthSettings;
}

struct ExperimentAdvanceOptions {
    1:optional string executionUser;
    2:optional string metadata;
    3:optional string experimentName;
    4:optional string customExperimentId;
    5:optional WorkflowSchedulingSettings workflowSchedulingSettings;
    6:optional WorkflowOutputDataSettings workflowOutputDataSettings;
    7:optional SecuritySettings securitySettings;
}

service InterpreterService {
    string createExperiment(1:string executionUser,2:string applicationName),
    string submitExperiment(1:map<string,string> applicationInputs, 2: ExperimentAdvanceOptions experimentAdOptions)
}
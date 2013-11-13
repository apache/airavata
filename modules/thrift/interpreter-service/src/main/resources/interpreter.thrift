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

namespace java org.apache.airavata.experiment.execution

typedef string ExperimentName
typedef map<string,string> workflowInputs

struct HPCSettings {
    1:string jobManager;
    2:i32 cpuCount;
    3:i32 nodeCount;
    4:string queueName;
    5:i32 maxWalltime;
}

struct HostSchedulingSettings {
    1:string hostID;
    2:bool isWSGramPreferred;
    3:string gatekeeperEPR;
}

struct NameValuePairType {
    1:string name;
    2:string value;
    3:string description;
}

struct NodeSettings {
    1:string nodeId;
    2:string serviceId;
    3:HostSchedulingSettings hostSchedulingSettings;
    4:HPCSettings hpcSettings;
    5:list<NameValuePairType> nameValuePairList;
}

struct WorkflowSchedulingSettings {
    1:list<NodeSettings> nodeSettingsList;
}

struct OutputDataSettings {
    1:string nodeID;
    2:string outputdataDir;
    3:string dataRegURL;
    4:bool isdataPersisted;
}

struct WorkflowOutputDataSettings{
    1:list<OutputDataSettings> outputDataSettingsList;
}

struct SSHAuthenticationSettings {
    1:string accessKeyID;
    2:string secretAccessKey;
}

struct MyProxyRepositorySettings {
    1:string userName;
    2:string password;
    3:string myproxyServer;
    4:i32 lifetime;
}

struct CredentialStoreSecuritySettings {
    1:string tokenId;
    2:string portalUser;
    3:string gatewayID;
}

struct AmazonWebServicesSettings {
    1:string accessKey;
    2:string amiID;
    3:string instanceID;
    4:string instanceType;
    5:string secretAccessKey;
    6:string username;
}

struct SecuritySettings {
    1:AmazonWebServicesSettings amazonWSSettings;
    2:CredentialStoreSecuritySettings credentialStoreSettings;
    3:MyProxyRepositorySettings myproxySettings;
    4:SSHAuthenticationSettings sshAuthSettings;
}

struct ExperimentAdvanceOptions {
    1:string executionUser;
    2:string metadata;
    3:string experimentName;
    4:string customExperimentId;
    5:WorkflowSchedulingSettings workflowSchedulingSettings;
    6:WorkflowOutputDataSettings workflowOutputDataSettings;
    7:SecuritySettings securitySettings;
}

service InterpreterService {
    string runExperiment(1:string workflowTemplateName, 2:map<string,string> workflowInputs, ExperimentAdvanceOptions experimentAdOptions),
    void cancelExperiment(1:string experimentID),
    void suspendExperiment(1:string experimentID),
    void resumeExperiment(1:string experimentID)
}
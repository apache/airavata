/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <glib.h>
#include <iostream>
#include <stdint.h>
#include <sys/time.h>

#define _WIN32_WINNT 0x501

#include <thrift/transport/TTransport.h>
#include <thrift/transport/TBufferTransports.cpp>
#include <thrift/transport/TSocket.cpp>
#include <thrift/protocol/TProtocol.h>
#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/protocol/TBinaryProtocol.tcc>
#include <thrift/TApplicationException.cpp>
#include <thrift/transport/TTransportException.cpp>
#include <thrift/protocol/TProtocolException.h>
#include "../lib/airavata/Airavata.h"
#include "../lib/airavata/Airavata.cpp"
#include "../lib/airavata/airavataDataModel_types.h"
#include "../lib/airavata/airavataDataModel_types.cpp"
#include "../lib/airavata/airavataErrors_types.h"
#include "../lib/airavata/airavataErrors_types.cpp"
#include "../lib.airavata.registry.core.experiment.odel_types.h"
#include "../lib.airavata.registry.core.experiment.odel_types.cpp"
#include "../lib/airavata/workspaceModel_types.h"
#include "../lib/airavata/workspaceModel_types.cpp"
#include "../lib/airavata/airavataAPI_types.h"
#include "../lib/airavata/airavataAPI_types.cpp"
#include "../lib/airavata/applicationDeploymentModel_types.h"
#include "../lib/airavata/applicationDeploymentModel_types.cpp"
#include "../lib/airavata/applicationInterfaceModel_types.h"
#include "../lib/airavata/applicationInterfaceModel_types.cpp"
#include "../lib/airavata/gatewayResourceProfileModel_types.h"
#include "../lib/airavata/gatewayResourceProfileModel_types.cpp"
#include "../lib/airavata/computeResourceModel_types.h"
#include "../lib/airavata/computeResourceModel_types.cpp"


typedef struct {
        gchar *airavata_server;
        gint airavata_port, airavata_timeout;
} Settings;

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace apache::airavata::api;
using namespace apache::airavata::model::workspace::experiment;

void readConfigFile(char* cfgfile, string& airavata_server, int& airavata_port, int& airavata_timeout) {

        Settings *conf;
        GKeyFile *keyfile;
        GKeyFileFlags flags;
        GError *error = NULL;        
        keyfile = g_key_file_new ();        				
        if (!g_key_file_load_from_file (keyfile, cfgfile, flags, &error)) {
                g_error (error->message);
        } else {                
                conf = g_slice_new (Settings);
                conf->airavata_server    = g_key_file_get_string(keyfile, "airavata", "AIRAVATA_SERVER", NULL);
                airavata_server = conf->airavata_server;
                conf->airavata_port      = g_key_file_get_integer(keyfile, "airavata", "AIRAVATA_PORT", NULL);
                airavata_port = conf->airavata_port;
                conf->airavata_timeout  = g_key_file_get_integer(keyfile, "airavata", "AIRAVATA_TIMEOUT", NULL);
                airavata_timeout = conf->airavata_timeout;                
        }				

}


int main(int argc, char **argv)
{
        
        int airavata_port, airavata_timeout;
        string airavata_server;
				char* cfgfile;
				cfgfile = "./airavata-client-properties.ini";
        readConfigFile(cfgfile, airavata_server, airavata_port, airavata_timeout);				
				airavata_server.erase(0,1);
				airavata_server.erase(airavata_server.length()-1,1);			
			  boost::shared_ptr<TSocket> socket(new TSocket(airavata_server, airavata_port));
				socket->setSendTimeout(airavata_timeout);
  			boost::shared_ptr<TTransport> transport(new TBufferedTransport(socket));	
  			boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
				AiravataClient airavataclient(protocol);
				transport->open();
				
				if(argc !=4){
					cout << "Usage: ./createExperiment <username> <experiment_name> <project_ID>";
					return 0;
				}
				/* ComputationalResourceScheduling data for Trestles*/
        ComputationalResourceScheduling cmRST;
        cmRST.__set_resourceHostId("trestles.sdsc.edu");
        cmRST.__set_computationalProjectAccount("sds128");
        cmRST.__set_totalCPUCount(1);
        cmRST.__set_nodeCount(1);
        cmRST.__set_numberOfThreads(0);
        cmRST.__set_queueName("normal");
        cmRST.__set_wallTimeLimit(15);
        cmRST.__set_jobStartTime(0);
        cmRST.__set_totalPhysicalMemory(0);


				UserConfigurationData userConfigurationData;
        userConfigurationData.__set_airavataAutoSchedule(0);
        userConfigurationData.__set_overrideManualScheduledParams(0);
        userConfigurationData.__set_computationalResourceScheduling(cmRST);
       
				
				/*Application ID for Trestles */
        char* appId = "SimpleEcho2";        

				 /* Experiment input and output data. */
        DataObjectType input;
        input.__set_key("echo_input");
        input.__set_value("echo_output=Hello World");
        input.__set_type(DataType::STRING);
				std::vector<DataObjectType> exInputs;
				exInputs.push_back(input);				
        DataObjectType output;
        output.__set_key("echo_output");
        output.__set_value("");
        output.__set_type(DataType::STRING);
				std::vector<DataObjectType> exOutputs;
				exOutputs.push_back(output);
        
        
				char* user = argv[1];
        char* exp_name = argv[2];
        char* proj = argv[3];

        Experiment experiment;
        experiment.__set_projectID(proj);
        experiment.__set_userName(user);
        experiment.__set_name(exp_name);
        experiment.__set_applicationId(appId);
        experiment.__set_userConfigurationData(userConfigurationData);
        experiment.__set_experimentInputs(exInputs);
        experiment.__set_experimentOutputs(exOutputs);
								
				string _return = "";
        airavataclient.createExperiment(_return, experiment);

        if (_return!="")
        {
            
            cout << "Experiment " << _return <<" created! \n    ";
        }
        else
        {
            cout << "Failed to create experiment. \n";
        }
				transport->close();
}

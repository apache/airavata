#include <glib.h>
#include <iostream>
#include <stdint.h>
#include <sys/time.h>
#include <fstream>

#define _WIN32_WINNT 0x501

#include "../lib/thrift/transport/TTransport.h"
#include "../lib/thrift/transport/TBufferTransports.cpp"
#include "../lib/thrift/transport/TSocket.h"
#include "../lib/thrift/protocol/TProtocol.h"
#include "../lib/thrift/protocol/TBinaryProtocol.h"
#include "../lib/thrift/protocol/TBinaryProtocol.tcc"
#include "../lib/thrift/TApplicationException.cpp"
#include "../lib/thrift/transport/TTransportException.cpp"
#include "../lib/thrift/protocol/TProtocolException.h"
#include "../lib/airavata/Airavata.h"
#include "../lib/airavata/Airavata.cpp"
#include "../lib/airavata/airavataDataModel_types.h"
#include "../lib/airavata/airavataDataModel_types.cpp"
#include "../lib/airavata/airavataErrors_types.h"
#include "../lib/airavata/airavataErrors_types.cpp"
#include "../lib/airavata/experimentModel_types.h"
#include "../lib/airavata/experimentModel_types.cpp"
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
        gchar *airavata_server, *app_catalog_server;
        gint airavata_port, app_catalog_port, airavata_timeout;
} Settings;

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace apache::airavata::api;
using namespace apache::airavata::model::workspace::experiment;

void readConfigFile(char* cfgfile, string& airavata_server, int& airavata_port, int& airavata_timeout) {

        // Settings *conf;
        // GKeyFile *keyfile;
        // GKeyFileFlags flags;
        // GError *error = NULL;        
        // keyfile = g_key_file_new ();        				
        // if (!g_key_file_load_from_file (keyfile, cfgfile, flags, &error)) {
        //         g_error (error->message);
        // } else {                
        //         conf = g_slice_new (Settings);
        //         conf->airavata_server    = g_key_file_get_string(keyfile, "airavata", "AIRAVATA_SERVER", NULL);
        //         airavata_server = conf->airavata_server;
        //         conf->airavata_port      = g_key_file_get_integer(keyfile, "airavata", "AIRAVATA_PORT", NULL);
        //         airavata_port = conf->airavata_port;
        //         conf->airavata_timeout  = g_key_file_get_integer(keyfile, "airavata", "AIRAVATA_TIMEOUT", NULL);
        //         airavata_timeout = conf->airavata_timeout;                
        // }				

        airavata_server="'localhost'";
        airavata_port= 8930;
        airavata_timeout=500000;
}

string createProject(char* projectOwner,char* projectName){

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
				
				apache::airavata::model::workspace::Project project;
				// if(argc !=3){
				// 	cout << "Usage: ./createProject <owner> <projectName>";
				// 	return 0;
				// }
				
				project.owner=projectOwner;
				project.name=projectName;
				string _return;
				airavataclient.createProject(_return,project);
				cout << _return << "\n";
				transport->close();
				return _return;
}

string createExperiment(char* userName,char* expName,char* projId,char* execId,char* inp){

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
				
				if(userName==NULL||expName==NULL||projId==NULL){
					cout << "not enough parameters-enter user,exp_name and proj";
					return 0;
				}
				/* ComputationalResourceScheduling data for Trestles*/
        ComputationalResourceScheduling cmRST;
        cmRST.__set_resourceHostId("localhost");
        cmRST.__set_computationalProjectAccount("psp");
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
        char* appId = execId;
        if(appId==NULL)
        appId = "SimpleEcho2";        
        // qDebug()<<appId;
				 /* Experiment input and output data. */
        DataObjectType input;
        input.__set_key("Input_JSON");
        input.__set_value(inp);
        input.__set_type(DataType::STRING);
				std::vector<DataObjectType> exInputs;
				exInputs.push_back(input);				
        DataObjectType output;
        output.__set_key("JSON_output");
        output.__set_value("");
        output.__set_type(DataType::STRING);
				std::vector<DataObjectType> exOutputs;
				exOutputs.push_back(output);
        
        
				

        Experiment experiment;
        experiment.__set_projectID(projId);
        experiment.__set_userName(userName);
        experiment.__set_name(expName);
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
	    return _return;
}

int getExperimentStatus(char* expId){
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
				
				
				
				ExperimentStatus _return;		
   			airavataclient.getExperimentStatus(_return, expId);
   			// qDebug() << _return.experimentState ;
   			cout << _return.experimentState <<"\n";
				transport->close();
				return _return.experimentState;

}

string getExperimentOutput(char* expId){
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
				
				
				// if(argc !=2){
				// 	cout << "Usage: ./getExperimentOutputs <experimentID>";
				// 	return 0;
				// }
				// char* expId = argv[1];			
				std::vector<DataObjectType> _return;
   			airavataclient.getExperimentOutputs(_return, expId);
				int i;
				for(i=0; i<_return.size();i++){
					cout << _return[i].value <<"\n";
				}
				transport->close();
				return _return[0].value;
}

void launchExperiment(char* expId){
	int airavata_port, airavata_timeout;
        string airavata_server;
				char* cfgfile;
				cfgfile = "./airavata-client-properties.ini";
	    // qDebug() << "launched experiment";
        readConfigFile(cfgfile, airavata_server, airavata_port, airavata_timeout);				
	    // qDebug() << "read config file";
				airavata_server.erase(0,1);
	// qDebug() << "erase file";
				airavata_server.erase(airavata_server.length()-1,1);			
			  boost::shared_ptr<TSocket> socket(new TSocket(airavata_server, airavata_port));
	// qDebug() << "socket TSocket";
				socket->setSendTimeout(airavata_timeout);
	// qDebug() << "setSendTimeout";
  			boost::shared_ptr<TTransport> transport(new TBufferedTransport(socket));	
	// qDebug() << "transport";
  			boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
	// qDebug() << "protocol";
				AiravataClient airavataclient(protocol);
	// qDebug() << "airavataclient protocol";
				transport->open();
	// qDebug() << "transport open";
				
				
				// if(argc !=2){
				// 	cout << "Usage: ./launchExperiment <experimentID>";
				// 	return 0;
				// }
				// char* expId = argv[1];		
			// expId="Align_101417ba-d75c-43cb-bc68-74661e028ff6";			
   			airavataclient.launchExperiment(expId, "airavataToken");
	// qDebug() << "launched client experiment";
   			cout << "Experiment " << expId << " is launched.\n";
				transport->close();
}
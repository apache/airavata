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
				
				
				if(argc !=2){
					cout << "Usage: ./launchExperiment <experimentID>";
					return 0;
				}
				char* expId = argv[1];					
   			airavataclient.launchExperiment(expId, "airavataToken");
   			cout << "Experiment " << expId << " is launched.\n";
				transport->close();
				
}

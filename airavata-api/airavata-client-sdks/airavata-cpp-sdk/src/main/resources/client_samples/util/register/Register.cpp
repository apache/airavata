#include "Register.h"
#include "../lib/jsoncons/json.hpp"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace apache::airavata::api;
using namespace apache::airavata::model::workspace;
using namespace apache::airavata::model::workspace::experiment;
using namespace apache::airavata::model::appcatalog::computeresource;
using namespace apache::airavata::model::appcatalog::appinterface;
using namespace apache::airavata::model::appcatalog::appdeployment;
using namespace apache::airavata::model::appcatalog::gatewayprofile;
using namespace apache::airavata::api::error;

using namespace std;
using jsoncons::json;
using jsoncons::output_format;
using std::string;

const string Register::THRIFT_SERVER_HOST = "127.0.0.1" ;
const string Register::DEFAULT_GATEWAY = "Sample" ;

Register::Register(){}
Register::~Register(){}

struct module {
    string id;
    string name;
    string moduleId;
};
vector<module> genapp_modules;
void Register::init()
{
    this->moduleDir="";
    this->moduleId="";
    this->localhostId="";
    
	//string directivesHome = getenv("DIRECTIVES_HOME");
    string directivesHome = "/home/priyanshu-sekhar/gsoc/new_airavata/new_genapp/psptest";
    this->moduleDir = "/home/priyanshu-sekhar/gsoc/new_airavata/new_genapp/psptest";
	if(directivesHome.empty())
	{
        moduleDir = "/bin";
	}
	else
	{
		this->directivesFile = directivesHome + "/directives.json";
        this->modulesFile = directivesHome + "/menu.json";
	}
	cout << "Directives directory: "+this->directivesFile<< endl;
    //directives
       json directives_json = json::parse_file(this->directivesFile);
       json executable_path = directives_json["executable_path"];
       this->moduleDir = executable_path["qt4"].as<std::string>();

    //modules
    
    json modules = json::parse_file(this->modulesFile);
    json module_menus = modules["menu"].as<json::array>();
    for(size_t i=0;i<module_menus.size();i++)
    {
        json menu_modules = module_menus[i]["modules"].as<json::array>();
        for(size_t j=0;j<menu_modules.size();j++)
        {
            module new_module;
            new_module.id = menu_modules[j]["id"].as<std::string>();
            new_module.name = menu_modules[j]["label"].as<std::string>();
            cout << new_module.id << endl;
            genapp_modules.push_back(new_module);
        }
    }
    
}

void Register::registerGateway() 
{
    try
    {
        Gateway gateway;
        gateway.__set_gatewayName("Sample");
        gateway.__set_gatewayId("sample");
        airavataClient->addGateway(this->gatewayId,gateway);
    }
    catch(TException e)
    {
        cout << e.what() << endl;
    }
}

void Register::registerGatewayProfile()
{
    try
    {
        DataMovementProtocol::type dataMovementProtocol;
        string scratchlocation = this->moduleDir + "../";
        JobSubmissionProtocol::type jobSubmissionProtocol;
        string preferredBatchQueue;
        bool overridebyAiravata = false;
        string allocationProjectNumber = "Sample";
        string computeResourceId = localhostId;
        ComputeResourcePreference localhostResourcePreference;
        localhostResourcePreference.__set_computeResourceId(computeResourceId);
        localhostResourcePreference.__set_allocationProjectNumber(allocationProjectNumber);
        localhostResourcePreference.__set_overridebyAiravata(overridebyAiravata); 
        localhostResourcePreference.__set_preferredBatchQueue(preferredBatchQueue);
        localhostResourcePreference.__set_preferredJobSubmissionProtocol(jobSubmissionProtocol);
        localhostResourcePreference.__set_preferredDataMovementProtocol(dataMovementProtocol);
        localhostResourcePreference.__set_scratchLocation(scratchlocation);

        GatewayResourceProfile gatewayResourceProfile;
        gatewayResourceProfile.__set_gatewayID(this->gatewayId);
        std::vector<ComputeResourcePreference> crpVector;
        crpVector.push_back(localhostResourcePreference);
        gatewayResourceProfile.__set_computeResourcePreferences(crpVector);
        string _registerGatewayResourceProfile="";
        airavataClient->registerGatewayResourceProfile(_registerGatewayResourceProfile,gatewayResourceProfile);
        cout << "gateway profile rgistered:" << _registerGatewayResourceProfile << endl;
        
    }
    catch(TException e)
    {
        cout << e.what() << endl;
    }
}
void Register::readConfigFile(char* cfgfile, string& airavata_server, int& airavata_port, int& airavata_timeout) {

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

        airavata_server="'127.0.0.1'";
        airavata_port= 8930;
        airavata_timeout=50000;
}

void Register::registerAll()
{  
	
	    int airavata_port, airavata_timeout;
        string airavata_server="";
        char* cfgfile = "./airavata-client-properties.ini";;
		readConfigFile(cfgfile, airavata_server, airavata_port, airavata_timeout);				
	    airavata_server.erase(0,1);
		airavata_server.erase(airavata_server.length()-1,1);	
		boost::shared_ptr<TSocket> socket(new TSocket(airavata_server, airavata_port));
		socket->setSendTimeout(airavata_timeout);
  		boost::shared_ptr<TTransport> transport(new TBufferedTransport(socket));	
  		boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
		airavataClient = new AiravataClient(protocol);
        
        transport->open();
        registerGateway();
        registerGatewayProfile();
		registerLocalhost();
		registerApplicationModules();
		registerApplicationDeployments();
		registerApplicationInterfaces();
        transport->close();
}

void Register::registerLocalhost() 
{
	try
	{
		cout << "\n #### Registering Localhost Computational Resources" << endl;
        string hostname="localhost";
        string hostDesc="LocalHost";
        vector<string> ipAddresses;
        vector<string> hostAliases;

        ComputeResourceDescription host;
        host.__set_hostName(hostname);
        host.__set_resourceDescription(hostDesc);
        host.__set_ipAddresses(ipAddresses);
        host.__set_hostAliases(hostAliases);
        
        // int airavata_port, airavata_timeout;
        // string airavata_server="";
        // char* cfgfile = "./airavata-client-properties.ini";;
        // readConfigFile(cfgfile, airavata_server, airavata_port, airavata_timeout);              
        // airavata_server.erase(0,1);
        // airavata_server.erase(airavata_server.length()-1,1);    
        // boost::shared_ptr<TSocket> socket(new TSocket(airavata_server, airavata_port));
        // socket->setSendTimeout(airavata_timeout);
        // boost::shared_ptr<TTransport> transport(new TBufferedTransport(socket));    
        // boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
        // airavataClient = new AiravataClient(protocol);
        // transport->open();
        airavataClient->registerComputeResource(localhostId,host);
        // transport->close();

        cout << "localhostId:" << localhostId << endl;
        ResourceJobManager resourceJobManager;
        map<JobManagerCommand::type, std::string> commandmap;
        // JobManagerCommand::type jobManagerCommandType = JobManagerCommandType::SUBMISSION;
        // commandmap[JobManagerCommand::SUBMISSION]="addLocalSubmissionDetails";
        resourceJobManager.__set_resourceJobManagerType(ResourceJobManagerType::FORK);
        resourceJobManager.__set_pushMonitoringEndpoint("");
        resourceJobManager.__set_jobManagerBinPath("");
        resourceJobManager.__set_jobManagerCommands(commandmap);
    

        LOCALSubmission localSubmission;
        localSubmission.__set_resourceJobManager(resourceJobManager);

        string submission = "";
        
        // transport->open();
        airavataClient->addLocalSubmissionDetails(submission,localhostId,1,localSubmission);
        // transport->close();

        cout << "submission:" << submission << endl;
        cout << "Localhost Resource Id is " << localhostId << endl;     
	}
	catch(TException& e)
	{
		cout << e.what() << endl;
	}
}

void Register::registerApplicationModules() 
{
    try
    {   
        for(std::vector<module>::iterator it = genapp_modules.begin(); it != genapp_modules.end(); it++) 
        {
            string moduleName = it->name;
            string appModuleVersion = "1.0";
            string appModuleDescription = moduleName+" application description";
            ApplicationModule appModule;
            appModule.__set_appModuleName(moduleName);
            appModule.__set_appModuleVersion(appModuleVersion);
            appModule.__set_appModuleDescription(appModuleDescription);
            airavataClient->registerApplicationModule(it->moduleId,this->gatewayId,appModule);

        }
    }
    catch(TException& e)
    {
        cout << e.what() << endl;
    }
}

void Register::registerApplicationDeployments()
{
    try
    {
        for(std::vector<module>::iterator it = genapp_modules.begin(); it != genapp_modules.end(); it++) 
        {
            cout << "#### Registering Genapp Modules on Localhost ####" << endl;
            string moduleName = it->name;
            string moduleId = it->moduleId;
            string moduleDeployId="";
            string computeResourceId = localhostId;
            string executablePath = moduleDir + "/" +it->id;
            ApplicationDeploymentDescription applicationDeploymentDescription;
            applicationDeploymentDescription.__set_appModuleId(moduleId);
            applicationDeploymentDescription.__set_computeHostId(computeResourceId);
            applicationDeploymentDescription.__set_executablePath(executablePath);
            applicationDeploymentDescription.__set_parallelism(ApplicationParallelismType::SERIAL);
            applicationDeploymentDescription.__set_appDeploymentDescription(moduleName);
            airavataClient->registerApplicationDeployment(moduleDeployId,this->gatewayId,applicationDeploymentDescription);
            cout << "Successfully registered " << moduleName << " application on localhost. Id= " << moduleDeployId << endl;
        }
    }
    catch(TException& e)
    {
        cout << e.what() << endl;
    }
}

void Register::registerApplicationInterfaces()
{   
    for(std::vector<module>::iterator it = genapp_modules.begin(); it != genapp_modules.end(); it++) 
    {
	    string moduleName = it->name;
        string moduleId = it->moduleId;
        registerApplicationInterface(moduleName,moduleId);
    }
}



void Register::registerApplicationInterface(string moduleName,string moduleId)
{
	try
	{
		cout << "#### Registering " << moduleName << "Interface ####" << endl;

        vector<string> appModules;
        appModules.push_back(moduleId);

        InputDataObjectType input;
        string inputName = "Input_JSON";
        string inputValue = "{}";
        apache::airavata::model::appcatalog::appinterface::DataType::type type = apache::airavata::model::appcatalog::appinterface::DataType::STRING;
        string applicationArgument="";
        bool stdIn = false;
        string description = "JSON String";
        string metaData="";

        if(!inputName.empty())
            input.__set_name(inputName);
        if(!inputValue.empty())
            input.__set_value(inputValue);
        input.__set_type(type);
        if(!applicationArgument.empty())
            input.__set_applicationArgument(applicationArgument);
        input.__set_standardInput(stdIn);
        if(!description.empty())
            input.__set_userFriendlyDescription(description);
        if(!metaData.empty())
            input.__set_metaData(metaData);

        vector<InputDataObjectType> applicationInputs;
        applicationInputs.push_back(input);
        
        string outputName = "Output_JSON";
        string outputValue = "{}";
        
        OutputDataObjectType output;
        if(!outputName.empty())
        	output.__set_name(outputName);
        if(!outputValue.empty())
        	output.__set_value(outputValue);
        output.__set_type(type);

        std::vector<OutputDataObjectType> applicationOutputs;
        applicationOutputs.push_back(output);

        string ModuleInterfaceId = "";
        ApplicationInterfaceDescription applicationInterfaceDescription;
        applicationInterfaceDescription.__set_applicationName(moduleName);
        applicationInterfaceDescription.__set_applicationDescription(moduleName + " the inputs");
        applicationInterfaceDescription.__set_applicationModules(appModules);
        applicationInterfaceDescription.__set_applicationInputs(applicationInputs);
        applicationInterfaceDescription.__set_applicationOutputs(applicationOutputs);

        airavataClient->registerApplicationInterface(ModuleInterfaceId,this->gatewayId,applicationInterfaceDescription);
    	
        cout << moduleName << " Module Interface Id: " << ModuleInterfaceId  << endl;
	}
	catch(TException& e)
	{
		cout << e.what() << endl;
	}
}
int main() 
{
	try
	{
		Register registerGenapp;
        registerGenapp.init();
        registerGenapp.registerAll();
	}
	catch(AiravataClientException& e1)
	{
		std::cerr<<"Cannot connect to server-"<< e1.what() <<endl;
	}
	catch(TException& e2)
	{
		cerr << e2.what() << endl;
	}

    return 0;
}


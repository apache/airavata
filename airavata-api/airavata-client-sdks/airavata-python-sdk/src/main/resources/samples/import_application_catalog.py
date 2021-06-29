from apache.airavata.api import Airavata
from apache.airavata.api.ttypes import *

from apache.airavata.model.workspace.ttypes import *
from apache.airavata.model.security.ttypes import AuthzToken
from apache.airavata.model.experiment.ttypes import *
from apache.airavata.model.appcatalog.appdeployment.ttypes import *
from apache.airavata.model.application.io.ttypes import *
from apache.airavata.model.appcatalog.appinterface.ttypes import *

import configparser
import json
import copy

sys.path.append('../lib')

from thrift import Thrift
from thrift.transport import TSSLSocket
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

def get_transport(hostname, port):
    # Create a socket to the Airavata Server
    # TODO: validate server certificate
    transport = TSSLSocket.TSSLSocket(hostname, port, validate=False)

    # Use Buffered Protocol to speedup over raw sockets
    transport = TTransport.TBufferedTransport(transport)
    return transport


def get_airavata_client(transport):
    # Airavata currently uses Binary Protocol
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    # Create a Airavata client to use the protocol encoder
    airavataClient = Airavata.Client(protocol)
    return airavataClient


def get_authz_token(token,username):
    return AuthzToken(accessToken=token, claimsMap={'gatewayID': "shubhamtestbed", 'userName': username})

def get_all_projects(airavataClient, authzToken, username,gatewayId):
    projectLists = airavataClient.getUserProjects(authzToken, gatewayId, username, -1, 0)

    return projectLists

def create_app_deployment(airavataClient, authzToken,AppDeployObj,gatewayId):
    appDeployID = airavataClient.registerApplicationDeployment(authzToken,gatewayId,AppDeployObj)
    return appDeployID

def create_app_module(airavataClient, authzToken,AppModuleObj,gatewayId):
    appDeployID = airavataClient.registerApplicationModule(authzToken,gatewayId,AppModuleObj)
    return appDeployID

def get_all_app_modules(airavataClient, authzToken,gatewayId):
    appModules = airavataClient.getAllAppModules(authzToken,gatewayId)
    return appModules

def create_app_interface(airavataClient, authzToken, AppInterfaceObj,gatewayId):
    appDeployID = airavataClient.registerApplicationInterface(authzToken,gatewayId,AppInterfaceObj)
    return appDeployID

if __name__ == '__main__':
    config = configparser.ConfigParser()
    config.read('airavata.ini')
    token = config['credentials']['AccessToken']
    username=config['credentials']['Username']
    gatewayid = config['credentials']['GatewayId']

    authz_token = get_authz_token(token,username)

    hostname = config['credentials']['host']
    port = config['credentials']['port']
    transport = get_transport(hostname, port)
    transport.open()
    airavataClient = get_airavata_client(transport)
    print('Airavata client -> ' + str(airavataClient))

    projects = get_all_projects(airavataClient, authz_token, username,gatewayid)
    #transport.close()
    print(projects)

        
    #import modules
    with open('ModulesData.txt') as file:
      datastore= json.load(file)
    
    for i in range(len(datastore)):
         AppModuleObj = ApplicationModule()
         #AppModuleObj.appModuleId = datastore[i]["appModuleId"]
         AppModuleObj.appModuleName =  datastore[i]["appModuleName"]
         AppModuleObj.appModuleVersion = datastore[i]["appModuleVersion"]
         AppModuleObj.appModuleDescription = datastore[i]["appModuleDescription"]
         create_app_module(airavataClient,authz_token,AppModuleObj,gatewayid)
    

    print("all modules created !!")




    ModulesList = get_all_app_modules(airavataClient,authz_token,gatewayid)

    print("deployment creation starts!!")

    with open('DeploysData.txt') as file:
        datastore= json.load(file)

    for i in range(len(datastore)):
          AppDeployObj = ApplicationDeploymentDescription()
          AppDeployObj.appDeploymentId = datastore[i]["appDeploymentId"]
          #AppDeployObj.appModuleId =  "" #datastore[i]["appModuleId"]
          #extract name from moduleid
          appModuleName = datastore[i]["appModuleId"].split('-')[0]
          appModuleName = appModuleName[:-9]
          for mod in ModulesList:
             if(appModuleName == mod.appModuleName):
                AppDeployObj.appModuleId = mod.appModuleId
          AppDeployObj.computeHostId = datastore[i]["computeHostId"]
          AppDeployObj.executablePath = datastore[i]["executablePath"]
          AppDeployObj.parallelism = datastore[i]["parallelism"]
          #optional itens
          AppDeployObj.appDeploymentDescription = datastore[i]["appDeploymentDescription"]

          if datastore[i]["moduleLoadCmds"] is not None:
            list =[]
            for j in range(len(datastore[i]["moduleLoadCmds"])):
               x = datastore[i]["moduleLoadCmds"][j]["command"]
               y = datastore[i]["moduleLoadCmds"][j]["commandOrder"]
               #print(CommandObject(x,y))
               list.append(CommandObject(x,y))
               AppDeployObj.moduleLoadCmds = copy.deepcopy(list)

          if datastore[i]["libPrependPaths"] is not None:
            libprePath =[]
            for j in range(len(datastore[i]["libPrependPaths"])):
               x = datastore[i]["libPrependPaths"][j]["name"]
               y = datastore[i]["libPrependPaths"][j]["value"]
               z = datastore[i]["libPrependPaths"][j]["envPathOrder"]
               #print(SetEnvPaths(x,y))
               libprePath.append(SetEnvPaths(x,y,z))
               AppDeployObj.libPrependPaths = copy.deepcopy(libprePath)

          if datastore[i]["libAppendPaths"] is not None:
            libappPath =[]
            for j in range(len(datastore[i]["libAppendPaths"])):
               x = datastore[i]["libAppendPaths"][j]["name"]
               y = datastore[i]["libAppendPaths"][j]["value"]
               z = datastore[i]["libAppendPaths"][j]["envPathOrder"]
               #print(SetEnvPaths(x,y))
               libappPath.append(SetEnvPaths(x,y,z))
               AppDeployObj.libAppendPaths = copy.deepcopy(libappPath)

          if datastore[i]["setEnvironment"] is not None:
            setEnv = []
            for j in range(len(datastore[i]["setEnvironment"])):
               x = datastore[i]["setEnvironment"][j]["name"]
               y = datastore[i]["setEnvironment"][j]["value"]
               z = datastore[i]["setEnvironment"][j]["envPathOrder"]
               #print(SetEnvPaths(x,y))
               setEnv.append(SetEnvPaths(x,y,z))
               AppDeployObj.setEnvironment = copy.deepcopy(setEnv)
       
          if datastore[i]["preJobCommands"] is not None:
            prejobCmd = []
            for j in range(len(datastore[i]["preJobCommands"])):
               x = datastore[i]["preJobCommands"][j]["command"]
               y = datastore[i]["preJobCommands"][j]["commandOrder"]
               #print(CommandObject(x,y))
               prejobCmd.append(CommandObject(x,y))
               AppDeployObj.preJobCommands = copy.deepcopy(prejobCmd)
          
          if datastore[i]["postJobCommands"] is not None:
            postjobCmd = []
            for j in range(len(datastore[i]["postJobCommands"])):
               x = datastore[i]["postJobCommands"][j]["command"]
               y = datastore[i]["postJobCommands"][j]["commandOrder"]
               #print(CommandObject(x,y))
               postjobCmd.append(CommandObject(x,y))
               AppDeployObj.postJobCommands = copy.deepcopy(postjobCmd)

          create_app_deployment(airavataClient, authz_token,AppDeployObj,gatewayid)



    print("deployment creation ends!!")
    

    print("interface creation starts!!")

    with open('InterfaceData.txt') as file:
      datastore2= json.load(file)

    for i in range(len(datastore2)):
          AppInterfaceObj = ApplicationInterfaceDescription()
          AppInterfaceObj.applicationName =  datastore2[i]["applicationName"] 
          AppInterfaceObj.applicationDescription =  datastore2[i]["applicationDescription"]  
          AppInterfaceObj.applicationModules = list(datastore2[i]["applicationModules"])

          if datastore2[i]["applicationInputs"] is not None:
             appInputs = []
             for j in range(len(datastore2[i]["applicationInputs"])): 
               x = datastore2[i]["applicationInputs"][j]["name"]
               y = datastore2[i]["applicationInputs"][j]["value"]
               z = datastore2[i]["applicationInputs"][j]["type"]
               a = datastore2[i]["applicationInputs"][j]["applicationArgument"]
               b = datastore2[i]["applicationInputs"][j]["standardInput"]
               c = datastore2[i]["applicationInputs"][j]["userFriendlyDescription"]
               d = datastore2[i]["applicationInputs"][j]["metaData"]
               e = datastore2[i]["applicationInputs"][j]["inputOrder"]
               f = datastore2[i]["applicationInputs"][j]["isRequired"]
               g = datastore2[i]["applicationInputs"][j]["requiredToAddedToCommandLine"]
               h = datastore2[i]["applicationInputs"][j]["dataStaged"]
               k = datastore2[i]["applicationInputs"][j]["storageResourceId"]
               appInputs.append(InputDataObjectType(x,y,z,a,b,c,d,e,f,g,h,k))
             AppInterfaceObj.applicationInputs = copy.deepcopy(appInputs)  
          
          if datastore2[i]["applicationOutputs"] is not None:
             appOutputs = []
             for j in range(len(datastore2[i]["applicationOutputs"])): 
               x = datastore2[i]["applicationOutputs"][j]["name"]
               y = datastore2[i]["applicationOutputs"][j]["value"]
               
               z = datastore2[i]["applicationOutputs"][j]["type"]
               a = datastore2[i]["applicationOutputs"][j]["applicationArgument"]
               b = datastore2[i]["applicationOutputs"][j]["isRequired"]
               c = datastore2[i]["applicationOutputs"][j]["requiredToAddedToCommandLine"]
               d = datastore2[i]["applicationOutputs"][j]["dataMovement"]
               e = datastore2[i]["applicationOutputs"][j]["location"]
               f = datastore2[i]["applicationOutputs"][j]["searchQuery"]
               g = datastore2[i]["applicationOutputs"][j]["outputStreaming"]
               h = datastore2[i]["applicationOutputs"][j]["storageResourceId"]
               appOutputs.append(OutputDataObjectType(x,y,z,a,b,c,d,e,f,g,h))
             AppInterfaceObj.applicationOutputs = copy.deepcopy(appOutputs) 
          
          create_app_interface(airavataClient,authz_token,AppInterfaceObj,gatewayid) 

    print("interface creation ends!!")

    transport.close() 

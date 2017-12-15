'''
[shubham@149-161-216-64 samples]$ python createAndLaunch.py /home/shubham/Documents/airavata/airavata-api/airavata-client-sdks/airavata-python-sdk/src/main/resources/samples/input1.com
No handlers could be found for logger "thrift.transport.sslcompat"
testProjNamecli_76b757fa-8d95-4910-944b-ac1f0f705407
experimentmodel :  testProjNamecli_76b757fa-8d95-4910-944b-ac1f0f705407
Traceback (most recent call last):
  File "createAndLaunch.py", line 140, in <module>
    create_and_launch_experiment(airavataClient,authz_token,gatewayID,appID,computeResourceName,username,projectid,experimentName,args.filepath)
  File "createAndLaunch.py", line 107, in create_and_launch_experiment
    experimentId = airavataClient.createExperiment(authz_token,gatewayID,experimentModel)
  File "../lib/apache/airavata/api/Airavata.py", line 5294, in createExperiment
    return self.recv_createExperiment()
  File "../lib/apache/airavata/api/Airavata.py", line 5313, in recv_createExperiment
    raise x
thrift.Thrift.TApplicationException: Required field 'projectId' is unset! 
Struct:ExperimentModel(experimentId:DO_NOT_SET_AT_CLIENTS, projectId:null, gatewayId:shubhamtestbed, 
experimentType:SINGLE_APPLICATION, userName:null, experimentName:Test create and launch exp, 
executionId:Gaussian_a39c4bad-2109-4262-899d-07ef52c54dc4, 
experimentInputs:[InputDataObjectType(name:Input-File, value:/home/shubham/Documents/airavata/airavata-api/airavata-client-sdks/airavata-python-sdk/
src/main/resources/samples/input1.com, type:URI, applicationArgument:, standardInput:false, userFriendlyDescription:Gaussian input file specifying 
desired calculation type, model chemistry, molecular system and other parameters., metaData:, inputOrder:1, isRequired:true, requiredToAddedToCommandLine:true,
 dataStaged:false, isReadOnly:false)], experimentOutputs:[OutputDataObjectType(name:Gaussian-Application-Output, value:Gaussian.log, type:URI, 
 applicationArgument:, isRequired:true, requiredToAddedToCommandLine:true, dataMovement:true, location:, searchQuery:, outputStreaming:false), 
 OutputDataObjectType(name:Gaussian-Job-Standard-Error, value:, type:STDERR, applicationArgument:, isRequired:true, requiredToAddedToCommandLine:false, 
 dataMovement:false, location:, searchQuery:, outputStreaming:false), OutputDataObjectType(name:Gaussian-Job-Standard-Output, value:, type:STDOUT, 
 applicationArgument:, isRequired:true, requiredToAddedToCommandLine:false, dataMovement:false, location:, searchQuery:, outputStreaming:false)])


'''



import sys

sys.path.append('../lib')

from apache.airavata.api import Airavata
from apache.airavata.api.ttypes import *

from apache.airavata.model.workspace.ttypes import *
from apache.airavata.model.security.ttypes import AuthzToken
from apache.airavata.model.experiment.ttypes import *
from apache.airavata.model.appcatalog.appdeployment.ttypes import *
from apache.airavata.model.appcatalog.appinterface.ttypes import *
from apache.airavata.model.application.io.ttypes import *
from apache.airavata.model.scheduling.ttypes import *

import argparse
import configparser

from thrift import Thrift
from thrift.transport import TSSLSocket
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

#Sample App id =Echo_cd90cd5f-2286-404d-b46d-8948755ea451
#

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

def get_authz_token(token,username,gatewayID):
    return AuthzToken(accessToken=token, claimsMap={'gatewayID': gatewayID, 'userName': username})  


     
def fetch_projectid(airavataClient,authz_token,gatewayID,username):
    userProjects = airavataClient.getUserProjects(authz_token,gatewayID,username,-1,0)
    #print userProjects
    print userProjects[0].projectID
    if (userProjects == None ):
       print 'Projectnot found'
    else:
       projectid = userProjects[0].projectID
    return projectid 

def create_experiment_model(airavataClient,authz_token,gatewayID,appInterfaceId,computeResourceName,username,projectid,
    experimentName,filepath):
    applicationInputs = airavataClient.getApplicationInputs(authz_token,appInterfaceId)

    #handling inputs for Gaussing and CPMD apps
    for appInput in applicationInputs:
        if appInput.name == 'Input-File':
            appInput.value = filepath 
        if appInput.name == 'INP-Input-File':
            appInput.value = filepath   
        if appInput.name == 'PPLibrary':
            appInput.value =  "Sample String"  

    applicationOutputs = airavataClient.getApplicationOutputs(authz_token,appInterfaceId)
    computeResourceList = airavataClient.getAllComputeResourceNames(authz_token)
    #print computeResourceList
    computeResourceId =''
    for computeRes in computeResourceList:
        if(computeResourceName == computeRes):
            computeResourceId = computeRes
    resourceSchedulingObj = ComputationalResourceSchedulingModel()
    resourceSchedulingObj.resourceHostId = computeResourceId
    resourceSchedulingObj.totalCPUCount = 8 #cores
    resourceSchedulingObj.nodeCount = 1 #nodes
    resourceSchedulingObj.queueName = 'cpu' #queue   ####compute,gpu or shared
    resourceSchedulingObj.wallTimeLimit = 15 #wallTime

    userConfigsObj = UserConfigurationDataModel()
    userConfigsObj.computationalResourceScheduling = resourceSchedulingObj

    experimentObj = ExperimentModel();
    experimentObj.projectid = 'testProjNamecli_76b757fa-8d95-4910-944b-ac1f0f705407' #projectid
    experimentObj.gatewayId = gatewayID
    experimentObj.username = username
    experimentObj.experimentName = experimentName
    experimentObj.executionId = appInterfaceId
    experimentObj.experimentInputs = applicationInputs
    experimentObj.experimentOutputs = applicationOutputs
    return experimentObj

def create_and_launch_experiment   (airavataClient,authz_token,gatewayID,appInterfaceId,computeResourceName,username,projectid,
    experimentName,filepath):
    print projectid
    #projectid = fetch_projectid(airavataClient,authz_token,gatewayID,username)   
    experimentModel = create_experiment_model(airavataClient,authz_token,gatewayID,appInterfaceId,computeResourceName,username,projectid,
    experimentName,filepath)
    experimentId = airavataClient.createExperiment(authz_token,gatewayID,experimentModel)
    print 'created experimet, ID' , experimentId
    airavataClient.launchExperiment(authz_token,experimentId,gatewayID)
    print 'experiment launched'


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser = argparse.ArgumentParser(description ="Create and launch experiment")
    parser.add_argument('filepath',type=str, help= "filepath for input files")
    args = parser.parse_args()

    config = configparser.RawConfigParser()
    config.read('../conf/airavata-client.properties')
    token = config.get('GatewayProperties', 'cred_token_id')

    username= config.get('AiravataServer', 'username')
    gatewayID = config.get('GatewayProperties', 'gateway_id')
    authz_token = get_authz_token(token,username,gatewayID)
    #print(authz_token)

    hostname = config.get('AiravataServer', 'host')
    port = config.get('AiravataServer', 'port')

    transport = get_transport(hostname, 9930)
    transport.open()
    airavataClient = get_airavata_client(transport)
    
    #values for testing
    appID = 'Gaussian_a39c4bad-2109-4262-899d-07ef52c54dc4'
    projectid = 'testProjNamecli_76b757fa-8d95-4910-944b-ac1f0f705407'
    experimentName = 'Test create and launch exp' 
    computeResourceName = 'bigred2.uits.iu.edu'
    create_and_launch_experiment(airavataClient,authz_token,gatewayID,appID,computeResourceName,username,projectid,experimentName,args.filepath)
    print 'Newly created experiment Id', newExpId

    transport.close()
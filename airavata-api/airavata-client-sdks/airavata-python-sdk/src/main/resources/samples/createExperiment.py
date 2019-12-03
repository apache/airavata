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

def create_experiment(airavataClient,authz_token,gatewayID,experimentObj):
    newExpId = airavataClient.createExperiment(authz_token,gatewayID,experimentObj) 
    return newExpId
     

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description ="Create new experiment ")
    parser.add_argument('appID',type=str, help= "Application Id associated with experiment")
    parser.add_argument('projID',type=str, help= "Project Id associated with experiment")
    args = parser.parse_args()
    print args

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

    experimentObj = ExperimentModel()
    experimentObj.userName = username
    experimentObj.experimentName = "cli-test-experiment"
    experimentObj.description = "experiment to test python cli"
    
    experimentObj.projectId = args.projID
    experimentObj.experimentType = ExperimentType.SINGLE_APPLICATION
    experimentObj.gatewayId = gatewayID

    newExpId = create_experiment(airavataClient,authz_token,gatewayID,experimentObj)
    


    print 'Newly created experiment Id', newExpId

    transport.close()
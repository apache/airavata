#!/usr/bin/env python

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

import sys, ConfigParser
import time

sys.path.append('../lib')
sys.path.append('../') # necessary on some machines

from apache.airavata.api import Airavata
from apache.airavata.model.experiment.ttypes import ExperimentModel, UserConfigurationDataModel, ExperimentType
from apache.airavata.model.workspace.ttypes import Project
from apache.airavata.model.scheduling.ttypes import ComputationalResourceSchedulingModel
from apache.airavata.model.security.ttypes import AuthzToken
from apache.airavata.model.status.ttypes import ExperimentState

from thrift import Thrift
from thrift.transport import TSocket, TTransport
from thrift.protocol import TBinaryProtocol
from thrift.transport.THttpClient import THttpClient

# from apache.airavata.api import Airavata
# from apache.airavata.model.experiment.ttypes import *
# from apache.airavata.model.security.ttypes import *
# from apache.airavata.model.status.ttypes import *
# from apache.airavata.model.scheduling.ttypes import *

# from thrift import Thrift
# from thrift.transport import TSocket
# from thrift.transport import TTransport
# from thrift.protocol import TBinaryProtocol

class AiravataClient():
    """Wrapper around airavataClient object"""
    def __init__(self, config):
        # Read Airavata Client properties
        airavataConfig = ConfigParser.RawConfigParser()
        airavataConfig.read(config)

        self.host = airavataConfig.get('AiravataServer', 'host')
        self.port = airavataConfig.getint('AiravataServer', 'port')
        self.cred = airavataConfig.get('GatewayProperties', 'cred_token_id')
        self.gatewayId = airavataConfig.get('GatewayProperties', 'gateway_id')

        # Create a socket to the Airavata Server
        socket = TSocket.TSocket(self.host, self.port)
        socket.setTimeout(10000)

        # Use Buffered Protocol to speed up over raw sockets
        self.transport = TTransport.TBufferedTransport(socket)

        # Airavata currently uses Binary Protocol
        protocol = TBinaryProtocol.TBinaryProtocol(self.transport)

        # Create a Airavata client to use the protocol encoder
        self.client = Airavata.Client(protocol)

        # Create dummy token
        self.token =  AuthzToken("default-token")


    # Context manager methods
    def __enter__(self):
        self.open()
        return self


    def __exit__(self, type, value, traceback):
        self.close()


    def open(self):
        self.transport.open()


    def close(self):
        self.transport.close()


    def printProperties(self):
        print 'Host: {}'.format(self.host)
        print 'Port: {}'.format(self.port)


    def printVersion(self):
        print 'Server version: {}'.format(self.client.getAPIVersion(self.token))


    def getAllAppModules(self, gatewayId):
        return self.client.getAllAppModules(self.token, gatewayId)


    def getAllComputeResourceNames(self):
        return self.client.getAllComputeResourceNames(self.token)


    def getComputeResource(self, computeResourceId):
        return self.client.getComputeResource(self.token, computeResourceId)


    def getAllApplicationInterfaceNames(self):
        return self.client.getAllApplicationInterfaceNames(self.token, self.gatewayId)


    def getExperiment(self, expId):
        """
        Returns:
            The airavata experiment_model.ExperimentModel corresponding to experiment ID
        
        """
        experiment = self.client.getExperiment(self.token, expId)
        # print 'Experiment configurationdata->computationalResource: {}'.format(experiment.userConfigurationData.computationalResourceScheduling.queueName)
        return experiment


    def getExperimentsInProject(self, projectId, limit, offset):
        """
        Args:
            projectId: (str) Identifier of the project
            limit: (int) Amount of results to be fetched
            offset: (int) The starting point of the results to be fetched

        Returns:
            list<experiment_model.ExperimentModel>
        """
        return self.client.getExperimentsInProject(self.token, projectId, limit, offset)


    def getProject(self, projectId):
        """
        Returns:
            The airavata workspace_model.Project getProject corresponding to project ID
        """
        return self.client.getProject(self.token, projectId) 


    def createProject(self, project):
        return self.client.createProject(self.token, self.gatewayId, project) 


    def createSampleExperiment(self):
        """Creates a sample Amber experiment

        Returns:
            The experiment ID (str) corresponding to newly created experiment
        """
        amberId = "Amber_66ca2b6c-ef36-409d-9f2b-67630c760609"
        #amberId = "Amber_74ad818e-7633-476a-b861-952de9b0a529"
        inputs = self.client.getApplicationInputs(self.token,amberId)
        for input in inputs:
            # print input.name
            if input.name == "Heat_Restart_File":
                input.value = "file:///home/airavata/production/appInputs/AMBER_FILES/02_Heat.rst"
            elif input.name == "Parameter_Topology_File":
                input.value ="file:///home/airavata/production/appInputs/AMBER_FILES/prmtop"
            elif input.name == "Production_Control_File":
                input.value = "file:///home/airavata/production/appInputs/AMBER_FILES/03_Prod.in"

        outputs = self.client.getApplicationOutputs(self.token, amberId)
        # for output in outputs:
        #    print output.name
        #projectId =  "gsoc_2015_be5a201b-9228-4dd9-9961-ba61b17bf527"
        projectId = "test_project_dd38ab8f-74ae-4ae6-a3ab-2c08cd41b77b" 
        stampedeHostName = "stampede.tacc.xsede.org"

        numberOfExps = len(self.getExperimentsInProject(projectId, 100, 0))

        experiment = ExperimentModel()
        experiment.gatewayId = 'default'
        experiment.projectId = projectId
        experiment.experimentType = ExperimentType.SINGLE_APPLICATION
        experiment.userName = "admin"

        # So i can keep track of how many experiments I have submitted
        experiment.experimentName = "Sample_experiment_{0}".format(numberOfExps + 1)
        experiment.description = "Test experiment"
        experiment.executionId = amberId
        experiment.experimentInputs = inputs
        experiment.experimentOutputs = outputs

        computeResources = self.client.getAvailableAppInterfaceComputeResources(self.token, amberId)
        id = None
        for key, value in computeResources.iteritems():
            # print key , " " , value
            if value == stampedeHostName:
                id = key
                break

        # Create a computational resource scheduling model
        crsm = ComputationalResourceSchedulingModel()
        crsm.totalCPUCount = 4
        crsm.nodeCount = 1
        crsm.queueName = "development"
        crsm.wallTimeLimit = 30
        crsm.totalPhysicalMemory = 1
        crsm.resourceHostId = id

        ucdm = UserConfigurationDataModel()
        ucdm.computationalResourceScheduling = crsm
        ucdm.airavataAutoSchedule = False
        ucdm.overrideManualScheduledParams = False
        experiment.userConfigurationData  = ucdm

        expId = self.client.createExperiment(self.token, "default", experiment)
        return expId


    def createExperiment(self, experiment):
        """
        Args:
            experiment = (experiment_model.ExperimentModel)

        Returns:
            experiment ID (str) of newly created experiment
        """
        return self.client.createExperiment(self.token, self.gatewayId, experiment)

    
    def cloneExperiment(self, existingExperimentId, newExperimentName):
        """Clone a specified experiment with a new name. A copy of the experiment configuration is
        made and is persisted with new metadata. The client has to subsequently update this
        configuration if needed and launch the cloned experiment.

        Args:
            existingExperimentId = (str)
            newExperimentName = (str)

        Returns:
            experiment ID (str) of newly created experiment
        """
        return self.client.cloneExperiment(self.token, existingExperimentId, newExperimentName)


    def launchExperiment(self, expId):
        self.client.launchExperiment(self.token, expId, self.cred)


    def getExperimentStatus(self, expId):
        """
        Returns:
            status_models.ExperimentStatus 
        """
        return self.client.getExperimentStatus(self.token, expId)


    def getJobStatuses(self, expId):
        """
        Returns:
            map<string, status_models.JobStatus>
        """
        return self.client.getJobStatuses(self.token, expId)


    def getJobDetails(self, expId):
        """
        Returns:
            list<job_model.JobModel>
        """
        return self.client.getJobDetails(self.token, expId)


    def getExperimentOutputs(self, expId):
        """
        Returns:
            list<application_io_models.OutputDataObjectType>
        """
        return self.client.getExperimentOutputs(self.token, expId)


    def getIntermediateOutputs(self, expId):
        """
        Returns:
            list<application_io_models.OutputDataObjectType>
        """
        return self.client.getIntermediateOutputs(self.token, expId)


    def validateExperiment(self, expId):
        """ Validate experiment configuration. True generally indicates the experiment is
        ready to be launched

        Returns:
            True or False

        """
        return self.client.validateExperiment(self.token, expId)



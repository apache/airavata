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

sys.path.append('../lib')

from apache.airavata.api import Airavata
from apache.airavata.api.ttypes import *
from apache.airavata.model.workspace.ttypes import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

try:
    # Read Airavata Client properties
    airavataConfig = ConfigParser.RawConfigParser()
    airavataConfig.read('../conf/airavata-client.properties')

    # Create a socket to the Airavata Server
    transport = TSocket.TSocket(airavataConfig.get('AiravataServer', 'host'), airavataConfig.get('AiravataServer', 'port'))

    # Use Buffered Protocol to speedup over raw sockets
    transport = TTransport.TBufferedTransport(transport)

    # Airavata currently uses Binary Protocol
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    # Create a Airavata client to use the protocol encoder
    airavataClient = Airavata.Client(protocol)

    # Connect to Airavata Server
    transport.open()

    #Create Project
    project = Project()
    project.owner = "smarru"
    project.name = "CLI-Test"
    project.description = "Test project to illustrate Python Client"

    print 'Created Project with Id:', airavataClient.createProject("sdsc", project)

    print 'Airavata Server Version is:', airavataClient.getAPIVersion()

    # Close Connection to Airavata Server
    transport.close()

except Thrift.TException, tx:
    print '%s' % (tx.message)


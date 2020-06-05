#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import logging
import os
import paramiko
from scp import SCPClient


logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)
# create console handler with a higher log level
handler = logging.StreamHandler()
handler.setLevel(logging.DEBUG)
# create formatter and add it to the handler
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
handler.setFormatter(formatter)
# add the handler to the logger
logger.addHandler(handler)


class SFTPConnector(object):

    def __init__(self, host, port, username, password):
        self.host = host
        self.port = port
        self.username = username
        self.password = password

        ssh = paramiko.SSHClient()
        self.ssh = ssh
        # Trust all key policy on remote host

        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())


    def upload_files(self, local_path, project_name, exprement_id):
        remote_path = "/" + project_name + "/" + exprement_id + "/"

        self.ssh.connect(self.host, self.port, self.username, password = self.password)
        with SCPClient(self.ssh.get_transport()) as conn:
            conn.put(local_path, remote_path, recursive=True)
        self.ssh.close()

        pathsuffix = "/" + self.username + remote_path
        return pathsuffix

    def download_files(self, local_path, project_name, exprement_id):
        remote_path = "/" + project_name + "/" + exprement_id + "/"

        self.ssh.connect(self.host, self.port, self.username, password = self.password)
        with SCPClient(self.ssh.get_transport()) as conn:
            conn.get(remote_path=remote_path, local_path= local_path, recursive= True)
        self.ssh.close()

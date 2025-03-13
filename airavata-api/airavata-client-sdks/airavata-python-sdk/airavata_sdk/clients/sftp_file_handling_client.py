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
from datetime import datetime

import paramiko
from paramiko import SFTPClient, Transport
from scp import SCPClient

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logging.getLogger("paramiko").setLevel(logging.WARNING)


class SFTPConnector(object):

    def __init__(self, host, port, username, password):
        self.host = host
        self.port = port
        self.username = username
        self.password = password

        ssh = paramiko.SSHClient()
        self.ssh = ssh
        # self.sftp = paramiko.SFTPClient()
        # Trust all key policy on remote host

        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())


    def upload_files(self, local_path: str, project_name: str, exprement_id: str):
        project_name = project_name.replace(" ", "_")
        time = datetime.now().strftime('%Y-%m-%d %H:%M:%S').replace(" ", "_")
        time = time.replace(":", "_")
        time = time.replace("-", "_")
        exprement_id = exprement_id+"_"+time
        remote_path = "/" + project_name + "/" + exprement_id + "/"
        pathsuffix = self.username + remote_path
        files = os.listdir(local_path)
        for file in files:
                try:
                    transport = Transport(sock=(self.host, int(self.port)))
                    transport.connect(username=self.username, password=self.password)
                    connection = SFTPClient.from_transport(transport)
                    assert connection is not None
                    try:
                        base_path = "/" + project_name
                        connection.chdir(base_path)  # Test if remote_path exists
                    except IOError:
                        connection.mkdir(base_path)
                    try:
                        connection.chdir(remote_path)  # Test if remote_path exists
                    except IOError:
                        connection.mkdir(remote_path)
                    connection.put(os.path.join(local_path, file), remote_path + "/" + file)
                finally:
                    transport.close()
        return pathsuffix

    def download_files(self, local_path: str, remote_path: str):
        self.ssh.connect(self.host, self.port, self.username, password = self.password)
        transport = self.ssh.get_transport()
        assert transport is not None
        with SCPClient(transport) as conn:
            conn.get(remote_path=remote_path, local_path= local_path, recursive= True)
        self.ssh.close()

    @staticmethod
    def uploading_info(uploaded_file_size: str, total_file_size: str):
        logging.info('uploaded_file_size : {} total_file_size : {}'.format(uploaded_file_size, total_file_size))
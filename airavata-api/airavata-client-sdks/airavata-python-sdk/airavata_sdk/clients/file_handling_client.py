#  Licrecursive=Nonepache Software Foundation (ASF) under one or more
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
import paramiko
from paramiko import SSHClient
from scp import SCPClient

ssh = SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.load_system_host_keys()

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


class FileHandler(object):

    def __init__(self, host, port, username, passphrase, privateKeyFilePath):
        self.host = host
        self.port = port
        self.username = username
        self.password = passphrase
        self.filePath = privateKeyFilePath

    def upload_file(self, files, remote_path, recursive, preserve_item):
        try:
            ssh.connect(self.host, self.port, self.username, passphrase=self.password, pkey=self.filePath)
            with SCPClient(ssh.get_transport()) as scp:
                scp.put(files, remote_path, recursive, preserve_item)
        finally:
            scp.close()

    def download_file(self, remote_path, local_path, recursive, preserve_item):
        try:
            ssh.connect(self.host, self.port, self.username, passphrase=self.password, pkey=self.filePath)
            with SCPClient(ssh.get_transport()) as scp:
                scp.get(remote_path, local_path, recursive, preserve_item)
        finally:
            scp.close()

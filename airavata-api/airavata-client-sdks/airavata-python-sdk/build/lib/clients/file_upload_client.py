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

import paramiko
from paramiko import SSHClient
from scp import SCPClient

ssh = SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.load_system_host_keys()


class FileHandler(object):

    def __init__(self, host, port, username, password):
        self.host = host
        self.port = port
        self.username = username
        self.password = password


def upload_file(self, files, remote_path, recursive, preserve_item):
    ssh.connect(self.host, self.port, self.username, self.password)
    with SCPClient(ssh.get_transport()) as scp:
        scp.put(files, remote_path, recursive, preserve_item)
        scp.close()


def download_file(self, remote_path, local_path, recursive, preserve_item):
    ssh.connect(self.host, self.port, self.username, self.password)
    with SCPClient(ssh.get_transport()) as scp:
        scp.get(self, remote_path, local_path, recursive, preserve_item)
        scp.close()

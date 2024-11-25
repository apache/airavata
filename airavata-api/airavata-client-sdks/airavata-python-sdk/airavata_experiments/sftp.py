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
from pathlib import Path
from datetime import datetime
from rich.progress import Progress

import paramiko
from paramiko import SFTPClient, Transport
from scp import SCPClient

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logging.getLogger("paramiko").setLevel(logging.WARNING)


def create_pkey(pkey_path):
  if pkey_path is not None:
    return paramiko.RSAKey.from_private_key_file(pkey_path)
  return None


class SFTPConnector(object):

  def __init__(self, host, port, username, password=None, pkey=None):
    self.host = host
    self.port = port
    self.username = username
    self.password = password
    self.pkey = pkey

    ssh = paramiko.SSHClient()
    self.ssh = ssh
    # self.sftp = paramiko.SFTPClient()
    # Trust all key policy on remote host

    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

  def upload_files(self, localpaths: list[Path], remote_path: str):
    transport = Transport(sock=(self.host, int(self.port)))
    if self.pkey is not None:
      transport.connect(username=self.username, pkey=create_pkey(self.pkey))
    else:
      transport.connect(username=self.username, password=self.password)
    try:
      with Progress() as progress:
        task = progress.add_task("Uploading...", total=len(localpaths)-1)
        for file in localpaths:
          connection = SFTPClient.from_transport(transport)
          assert connection is not None
          try:
            connection.lstat(remote_path)  # Test if remote_path exists
          except IOError:
            connection.mkdir(remote_path)
          remote_fpath = remote_path + "/" + file.name
          connection.put(file, remote_fpath)
          progress.update(task, advance=1, description=f"Uploading: {file.name}")
        progress.update(task, completed=True)
    finally:
      transport.close()
    return remote_path

  def make_experiment_dir(self, project_name: str, exprement_id, remote_base=""):
    project_name = project_name.replace(" ", "_")
    time = datetime.now().strftime("%Y-%m-%d %H:%M:%S").replace(" ", "_")
    time = time.replace(":", "_")
    time = time.replace("-", "_")
    exprement_id = exprement_id + "_" + time
    base_path = remote_base + "/" + project_name
    remote_path = base_path + "/" + exprement_id
    transport = Transport(sock=(self.host, int(self.port)))
    if self.pkey is not None:
      transport.connect(username=self.username,
                        pkey=create_pkey(self.pkey))
    else:
      transport.connect(username=self.username, password=self.password)

    try:
      connection = SFTPClient.from_transport(transport)
      assert connection is not None
      try:
        connection.lstat(base_path)  # Test if remote_path exists
      except IOError:
        connection.mkdir(base_path)
      try:
        connection.lstat(remote_path)  # Test if remote_path exists
      except IOError:
        connection.mkdir(remote_path)
    finally:
      transport.close()

    return remote_path

  def download_files(self, local_path, remote_path):
    if self.pkey is not None:
      self.ssh.connect(self.host, self.port, self.username,
                       pkey=create_pkey(self.pkey))
    else:
      self.ssh.connect(self.host, self.port,
                       self.username, password=self.password)

    transport = self.ssh.get_transport()
    assert transport is not None
    with SCPClient(transport) as conn:
      conn.get(remote_path=remote_path,
               local_path=local_path, recursive=True)
    self.ssh.close()

  @staticmethod
  def uploading_info(uploaded_file_size, total_file_size):
    logging.info("uploaded_file_size : {} total_file_size : {}".format(
        uploaded_file_size, total_file_size))

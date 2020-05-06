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
import pysftp

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

    def upload_files(self, local_path, project_name, exprement_id):
        remote_path = "/" + project_name + "/" + exprement_id + "/"
        cnopts = pysftp.CnOpts()
        cnopts.hostkeys = None
        with  pysftp.Connection(host=self.host, port=self.port, username=self.username,
                                password=self.password, cnopts=cnopts) as sftp:
            try:
                sftp.mkdir(remote_path)
            except OSError:
                pass
            sftp.put_r(localpath=local_path, remotepath=remote_path, confirm=True, preserve_mtime=False)
        sftp.close()
        pathsuffix = "/" + self.username + remote_path
        return pathsuffix

    def download_files(self, local_path, project_name, exprement_id):
        remote_path = "/" + project_name + "/" + exprement_id + "/"
        cnopts = pysftp.CnOpts()
        cnopts.hostkeys = None
        with  pysftp.Connection(host=self.host, port=self.port, username=self.username,
                                password=self.password, cnopts=cnopts) as sftp:
            sftp.get_r(remotedir=remote_path, localdir=local_path, preserve_mtime=False)
        sftp.close()

#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import os
import yaml

from org.apache.airavata.cloud.provider.CloudProviderBase import CloudProviderBase
from org.apache.airavata.cloud.provider.aws.AWSProvider import AWSProvider
from org.apache.airavata.cloud.provider.openstack.OpenStackProvider import OpenStackProvider


class CloudProvider(CloudProviderBase):

    @classmethod
    def __init__(cls, cloud_name, config_file):
        """
        This method initializes the CloudInterface
        :param cloud_name:
        :param config_file:
        """

        # Read the cloudbridge config file
        try:
            config_file = os.path.expanduser(config_file) # get full path
            with open(config_file, 'r') as stream:
                config = yaml.load(stream)['cloudbridge']
                cls.config = config
        except Exception as ex:
            print 'Failed to read config file: ' + config_file
            raise ex

        # Check if cloudname present in config
        if cloud_name not in cls.config['clouds']:
            print 'Cloud: ' + cloud_name + ' is not present in the config file.'
            raise
        else:
            cloud_details = cls.config['clouds'][cloud_name]
            if cloud_name == 'aws':
                # Instantiate AWS Provider
                provider = AWSProvider(
                    cloud_name,
                    cloud_details
                )
                cls.provider = provider
                cls.provider_class = AWSProvider
            elif cloud_name == 'jetstream':
                # Instantiate OpenStack Provider
                provider = OpenStackProvider(
                    cloud_name,
                    cloud_details
                )
                cls.provider = provider
                cls.provider_class = OpenStackProvider

        return
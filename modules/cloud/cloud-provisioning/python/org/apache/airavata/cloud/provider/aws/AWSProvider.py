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

from cloudbridge.cloud.factory import CloudProviderFactory
from cloudbridge.cloud.factory import ProviderList
from org.apache.airavata.cloud.provider.CloudProviderBase import CloudProviderBase


class AWSProvider(CloudProviderBase):

    @classmethod
    def __init__(cls, cloud_name, cloud_details):
        """
        Initialize the AWS Provider
        :param cloud_name:
        :param cloud_details:
        """
        creds_config = cloud_details['credentials']
        compute_config = cloud_details['compute']

        # Initialize the params
        cls.cloud = cloud_name
        cls.image = compute_config['ec2_image_id']
        cls.flavor = compute_config['ec2_flavor_name']
        cls.secgroup = compute_config['ec2_secgroup_name']
        cls.keypair = compute_config['ec2_keypair_name']

        # Initialize the CloudBridge provider
        cls.provider = CloudProviderFactory().create_provider(ProviderList.AWS, config=creds_config)

    @classmethod
    def create_vm(cls, instance_name):
        """
        Method to create a new virtual machine
            on Amazon EC2 cloud
        :param instance_name:
        :return:
        """

        print 'Creating a new VM on Amazon EC2, Input: ' \
            'Instance-name: ' + instance_name + ', ' \
            'Image: ' + cls.image + ', ' \
            'Flavor: ' + cls.flavor + ', ' \
            'Secgroup: ' + cls.secgroup + ',' \
            'Keypair: ' + cls.keypair

        try:
            # Find the ec2 image
            image = cls.provider.compute.images.get(cls.image)
            print 'EC2 Image: [' + image.name + '], [' + image.id + ']'

            # Find the ec2 flavor
            flavor = cls.provider.compute.instance_types.find(name=cls.flavor)
            if not flavor:
                raise 'Flavor: ' + cls.flavor + ' not found!'
            else:
                flavor = flavor[0]

            # Find the ec2 keypair
            keypair = cls.provider.security.key_pairs.find(name=cls.keypair)
            if not keypair:
                raise 'Keypair: ' + cls.keypair + ' not found!'
            else:
                keypair = keypair[0]

            # Find the security group
            secgroup = cls.provider.security.security_groups.find(name=cls.secgroup)
            if not secgroup:
                raise 'Secgroup: ' + cls.secgroup + ' not found!'
            else:
                secgroup = secgroup[0]

            # Create the instance
            instance = cls.provider.compute.instances.create(name=instance_name, image=image, instance_type=flavor,
                                                     key_pair=keypair, security_groups=[secgroup])

            print 'Instance created, waiting for it to get ready!'

            # wait till instance ready
            instance.wait_till_ready()

            # get public ip
            public_ips = instance.public_ips

            print 'EC2 Instance created, Name: ' + instance_name + \
                  ', State: ' + instance.state + \
                  ', Public IP: ' + ','.join(public_ips)

            return

        except Exception as ex:
            print 'Error creating a virtual machine on EC2: ' + ex.message.format(ex.args)
            raise ex

    @classmethod
    def delete_vm(cls, instance_id):
        """
        Method to terminate a virtual machine
            on Amazon EC2
        :param instance_id:
        :return:
        """

        try:
            # Find the instance
            instance = cls.provider.compute.instances.get(instance_id)
            if not instance:
                raise 'Instance with ID: ' + instance_id + ', not found!'
            else:
                instance.terminate()
                print 'Instance with ID: ' + instance_id + ', has been terminated!'
        except Exception as ex:
            print 'Error terminating a virtual machine on EC2: ' + ex.message.format(ex.args)
            raise ex

    @classmethod
    def list_vms(cls):
        """
        Method to list all virtual machines
            on Amacone EC2
        :return:
        """

        try:
            # list ec2 instances
            instances = cls.provider.compute.instances.list()
            for index, instance in enumerate(instances):
                print 'Index: ' + str(index+1) + \
                      ', Name: ' + instance.name + \
                      ', ID: ' + instance.id + \
                      ', State: ' + instance.state
        except Exception as ex:
            print 'Error listing virtual machines on EC2: ' + ex.message.format(ex.args)
            raise ex

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

""" cloudbridge-test.py

    Usage:
        cloudbridge-test.py instance-create <instance-name> [--cloud=CLOUD]
        cloudbridge-test.py instance-delete <instance-id> [--cloud=CLOUD]
        cloudbridge-test.py instance-list [--cloud=CLOUD]
        cloudbridge-test.py -h
    Arguments:
        <instance-name> : name of the virtual machine
        <instance-id>   : unique identifier of the virtual machine
    Options:
        -h,--help       : show this help message
        --cloud=CLOUD   : target cloud [aws/jetstream]
"""

from docopt import docopt
from org.apache.airavata.cloud.provider.CloudProvider import CloudProvider


def main(docopt_args):
    """
    Method to process command-line args
    :param docopt_args:
    :return:
    """

    # Instance create command
    if docopt_args['instance-create']:
        instance_name = docopt_args['<instance-name>']
        target_cloud = docopt_args['--cloud'] or 'aws'  # default to aws
        print 'Instance Create | Name: ' + instance_name + ', Cloud: ' + target_cloud

        # Get cloud provider & create vm
        provider = CloudProvider(cloud_name=target_cloud,
                                 config_file='cloudbridge.yaml').provider
        provider.create_vm(instance_name=instance_name)

    # Instance delete command
    elif docopt_args['instance-delete']:
        instance_id = docopt_args['<instance-id>']
        target_cloud = docopt_args['--cloud'] or 'aws'  # default to aws
        print 'Instance Delete | Name: ' + instance_id + ', Cloud: ' + target_cloud

        # Get cloud provider & delete vm
        provider = CloudProvider(cloud_name=target_cloud,
                                 config_file='cloudbridge.yaml').provider
        provider.delete_vm(instance_id=instance_id)

    elif docopt_args['instance-list']:
        target_cloud = docopt_args['--cloud'] or 'aws'  # default to aws
        print 'Instance List | Cloud: ' + target_cloud
        # Get cloud provider & list vms
        provider = CloudProvider(cloud_name=target_cloud,
                                 config_file='cloudbridge.yaml').provider
        provider.list_vms()


# Start of script
if __name__ == '__main__':
    # read the commandline args
    args = docopt(__doc__)
    # run the main program
    main(args)
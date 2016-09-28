# CloudBridge
CloudBridge python library to manage virtual machines on amazon ec2 and openstack based clouds. Note: Only EC2 is supported as of now. OpenStack support will be added soon.

## Installation

Run the following command to install the required python libraries.

    pip install -r requirements.txt
    

## Configuration

The __cloudbridge.yaml__ file contains the EC2/Jetstream configuration information.
Please update the _credentials_ section in this file with your EC2 credentials.
Also update the _compute_ section to set IMAGE, FLAVOR, SECURITY-GROUP, KEYPAIR of your choice.


## Usage

The __cloudbridge-test.py__ python script is the main file that needs to be run. Below is the usage:

    
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


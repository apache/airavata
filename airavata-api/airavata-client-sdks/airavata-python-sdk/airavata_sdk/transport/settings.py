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

import configparser
import os

config = configparser.ConfigParser()

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
defaultSettings = os.path.join(BASE_DIR, "transport", "settings.ini")
config.read(defaultSettings)


class APIServerClientSettings(object):

    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.API_SERVER_HOST = config.get('APIServer', 'API_HOST')
        self.API_SERVER_PORT = config.getint('APIServer', 'API_PORT')
        self.API_SERVER_SECURE = config.getboolean('APIServer', 'API_SECURE')


class IAMAdminClientSettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
        self.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
        self.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')


class TenantProfileServerClientSettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
        self.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
        self.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')


class GroupManagerClientSettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
        self.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
        self.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')


class SharingAPIClientSettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.SHARING_API_HOST = config.get('SharingServer', 'SHARING_API_HOST')
        self.SHARING_API_PORT = config.getint('SharingServer', 'SHARING_API_PORT')
        self.SHARING_API_SECURE = config.getboolean('SharingServer', 'SHARING_API_SECURE')


class CredentialStoreAPIClientSettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.CREDENTIAL_STORE_API_HOST = config.get('CredentialStoreServer', 'CREDENTIAL_STORE_API_HOST')
        self.CREDENTIAL_STORE_API_PORT = config.getint('CredentialStoreServer', 'CREDENTIAL_STORE_API_PORT')
        self.CREDENTIAL_STORE_API_SECURE = config.getboolean('CredentialStoreServer', 'CREDENTIAL_STORE_API_SECURE')


class UserProfileClientSettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
        self.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
        self.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')


class ThriftSettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.THRIFT_CLIENT_POOL_KEEPALIVE = config.getfloat('Thrift', 'THRIFT_CLIENT_POOL_KEEPALIVE')


class KeycloakConfiguration(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        # self.KEYCLOAK_CA_CERTIFICATE = os.path.join(BASE_DIR, "samples", "resources", "incommon_rsa_server_ca.pem")
        self.CLIENT_ID = config.get('KeycloakServer', 'CLIENT_ID')
        self.CLIENT_SECRET = config.get('KeycloakServer', 'CLIENT_SECRET')
        self.TOKEN_URL = config.get('KeycloakServer', 'TOKEN_URL')
        self.USER_INFO_URL = config.get('KeycloakServer', 'USER_INFO_URL')
        self.VERIFY_SSL = config.getboolean('KeycloakServer', 'VERIFY_SSL')


class GatewaySettings(object):
    def __init__(self, configFileLocation=None):
        if configFileLocation is not None:
            config.read(configFileLocation)
        self.GATEWAY_ID = config.get('Gateway', 'GATEWAY_ID')
        self.GATEWAY_DATA_STORE_RESOURCE_ID = config.get('Gateway', 'GATEWAY_DATA_STORE_RESOURCE_ID')
        self.GATEWAY_DATA_STORE_DIR = config.get('Gateway', 'GATEWAY_DATA_STORE_DIR')
        self.GATEWAY_DATA_STORE_HOSTNAME = config.get('Gateway', 'GATEWAY_DATA_STORE_HOSTNAME')
        self.FILE_UPLOAD_TEMP_DIR = config.get('Gateway', 'FILE_UPLOAD_TEMP_DIR')

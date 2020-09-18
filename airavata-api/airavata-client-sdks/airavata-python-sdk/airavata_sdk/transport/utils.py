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

import thrift_connector.connection_pool as connection_pool
from thrift.protocol import TBinaryProtocol
from thrift.protocol.TMultiplexedProtocol import TMultiplexedProtocol
from thrift.transport import TSocket, TSSLSocket, TTransport

from airavata.api import Airavata
from airavata.api.sharing import SharingRegistryService
from airavata.service.profile.groupmanager.cpi import GroupManagerService
from airavata.service.profile.groupmanager.cpi.constants import (
    GROUP_MANAGER_CPI_NAME
)
from airavata.service.profile.iam.admin.services.cpi import IamAdminServices
from airavata.service.profile.iam.admin.services.cpi.constants import (
    IAM_ADMIN_SERVICES_CPI_NAME
)
from airavata.service.profile.tenant.cpi import TenantProfileService
from airavata.service.profile.tenant.cpi.constants import (
    TENANT_PROFILE_CPI_NAME
)
from airavata.service.profile.user.cpi import UserProfileService
from airavata.service.profile.user.cpi.constants import USER_PROFILE_CPI_NAME
from airavata.api.credential.store import CredentialStoreService

from transport.settings import APIServerClientSettings, UserProfileClientSettings, TenantProfileServerClientSettings, \
    IAMAdminClientSettings, GroupManagerClientSettings, SharingAPIClientSettings, CredentialStoreAPIClientSettings, \
    ThriftSettings

log = logging.getLogger(__name__)

default_api_server_settings = APIServerClientSettings()
default_user_profile_server_settings = UserProfileClientSettings()
default_tenant_profile_client_settings = TenantProfileServerClientSettings()
default_iam_client_settings = IAMAdminClientSettings()
default_group_manager_client_settings = GroupManagerClientSettings()
default_sharing_API_client_settings = SharingAPIClientSettings()
default_credential_store_client_settings = CredentialStoreAPIClientSettings()
thrift_settings = ThriftSettings()


class ThriftConnectionException(Exception):
    pass


class ThriftClientException(Exception):
    pass


class CustomThriftClient(connection_pool.ThriftClient):
    secure = False
    validate = False

    @classmethod
    def get_socket_factory(cls):
        if not cls.secure:
            return connection_pool.ThriftClient.get_socket_factory()
        else:
            def factory(host, port):
                return TSSLSocket.TSSLSocket(host, port, validate=cls.validate)

            return factory

    def ping(self):
        try:
            self.client.getAPIVersion()
        except Exception as e:
            log.debug("getAPIVersion failed: {}".format(str(e)))
            raise


class MultiplexThriftClientMixin:
    service_name = None

    @classmethod
    def get_protoco_factory(cls):
        def factory(transport):
            protocol = TBinaryProtocol.TBinaryProtocol(transport)
            multiplex_prot = TMultiplexedProtocol(protocol, cls.service_name)
            return multiplex_prot

        return factory


class AiravataAPIThriftClient(CustomThriftClient):
    secure = default_api_server_settings.API_SERVER_SECURE


class GroupManagerServiceThriftClient(MultiplexThriftClientMixin,
                                      CustomThriftClient):
    service_name = GROUP_MANAGER_CPI_NAME
    secure = default_group_manager_client_settings.PROFILE_SERVICE_SECURE


class IAMAdminServiceThriftClient(MultiplexThriftClientMixin,
                                  CustomThriftClient):
    service_name = IAM_ADMIN_SERVICES_CPI_NAME
    secure = default_iam_client_settings.PROFILE_SERVICE_SECURE


class TenantProfileServiceThriftClient(MultiplexThriftClientMixin,
                                       CustomThriftClient):
    service_name = TENANT_PROFILE_CPI_NAME
    secure = default_tenant_profile_client_settings.PROFILE_SERVICE_SECURE


class UserProfileServiceThriftClient(MultiplexThriftClientMixin,
                                     CustomThriftClient):
    service_name = USER_PROFILE_CPI_NAME
    secure = default_user_profile_server_settings.PROFILE_SERVICE_SECURE


class CredentialStoreServiceThriftClient(CustomThriftClient):
    secure = default_credential_store_client_settings.CREDENTIAL_STORE_API_SECURE


class SharingAPIThriftClient(CustomThriftClient):
    secure = default_sharing_API_client_settings.SHARING_API_SECURE


def initialize_api_client_pool(host=default_api_server_settings.API_SERVER_HOST,
                               port=default_api_server_settings.API_SERVER_PORT,
                               is_secure=default_api_server_settings.API_SERVER_SECURE):
    AiravataAPIThriftClient.secure = is_secure
    airavata_api_client_pool = connection_pool.ClientPool(
        Airavata,
        host,
        port,
        connection_class=AiravataAPIThriftClient,
        keepalive=thrift_settings.THRIFT_CLIENT_POOL_KEEPALIVE
    )
    return airavata_api_client_pool


def initialize_group_manager_client(host=default_group_manager_client_settings.PROFILE_SERVICE_HOST,
                                    port=default_group_manager_client_settings.PROFILE_SERVICE_PORT,
                                    is_secure=default_group_manager_client_settings.PROFILE_SERVICE_SECURE):
    GroupManagerServiceThriftClient.secure = is_secure
    group_manager_client_pool = connection_pool.ClientPool(
        GroupManagerService,
        host,
        port,
        connection_class=GroupManagerServiceThriftClient,
        keepalive=thrift_settings.THRIFT_CLIENT_POOL_KEEPALIVE
    )
    return group_manager_client_pool


def initialize_iam_admin_client(host=default_iam_client_settings.PROFILE_SERVICE_HOST,
                                port=default_iam_client_settings.PROFILE_SERVICE_PORT,
                                is_secure=default_iam_client_settings.PROFILE_SERVICE_SECURE):
    IAMAdminServiceThriftClient.secure = is_secure
    iamadmin_client_pool = connection_pool.ClientPool(
        IamAdminServices,
        host,
        port,
        connection_class=IAMAdminServiceThriftClient,
        keepalive=thrift_settings.THRIFT_CLIENT_POOL_KEEPALIVE
    )
    return iamadmin_client_pool


def initialize_tenant_profile_client(host=default_tenant_profile_client_settings.PROFILE_SERVICE_HOST,
                                     port=default_tenant_profile_client_settings.PROFILE_SERVICE_PORT,
                                     is_secure=default_tenant_profile_client_settings.PROFILE_SERVICE_SECURE):
    TenantProfileServiceThriftClient.secure = is_secure

    tenant_profile_client_pool = connection_pool.ClientPool(
        TenantProfileService,
        host,
        port,
        connection_class=TenantProfileServiceThriftClient,
        keepalive=thrift_settings.THRIFT_CLIENT_POOL_KEEPALIVE
    )
    return tenant_profile_client_pool


def initialize_user_profile_client(host=default_user_profile_server_settings.PROFILE_SERVICE_HOST,
                                   port=default_user_profile_server_settings.PROFILE_SERVICE_PORT,
                                   is_secure=default_user_profile_server_settings.PROFILE_SERVICE_SECURE):
    UserProfileServiceThriftClient.secure = is_secure
    user_profile_client_pool = connection_pool.ClientPool(
        UserProfileService,
        host,
        port,
        connection_class=UserProfileServiceThriftClient,
        keepalive=thrift_settings.THRIFT_CLIENT_POOL_KEEPALIVE
    )
    return user_profile_client_pool


def initialize_sharing_registry_client(host=default_sharing_API_client_settings.SHARING_API_HOST,
                                       port=default_sharing_API_client_settings.SHARING_API_PORT,
                                       is_secure=default_sharing_API_client_settings.SHARING_API_SECURE):
    SharingAPIThriftClient.secure = is_secure

    sharing_api_client_pool = connection_pool.ClientPool(
        SharingRegistryService,
        host,
        port,
        connection_class=SharingAPIThriftClient,
        keepalive=thrift_settings.THRIFT_CLIENT_POOL_KEEPALIVE
    )
    return sharing_api_client_pool

def initialize_credential_store_client(host=default_credential_store_client_settings.CREDENTIAL_STORE_API_HOST,
                                       port=default_credential_store_client_settings.CREDENTIAL_STORE_API_PORT,
                                       is_secure=default_credential_store_client_settings.CREDENTIAL_STORE_API_SECURE):
    CredentialStoreService.secure = is_secure

    credential_store_api_client_pool = connection_pool.ClientPool(
        CredentialStoreService,
        host,
        port,
        connection_class=CredentialStoreServiceThriftClient,
        keepalive=thrift_settings.THRIFT_CLIENT_POOL_KEEPALIVE
    )
    return credential_store_api_client_pool

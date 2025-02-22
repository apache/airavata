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
from typing import Generic, TypeVar, Optional, cast

from thrift.protocol import TBinaryProtocol
from thrift.protocol.TMultiplexedProtocol import TMultiplexedProtocol
from thrift.transport import TSocket, TSSLSocket, TTransport

from airavata.api import Airavata
from airavata.api.credential.store import CredentialStoreService
from airavata.api.sharing import SharingRegistryService
from airavata.service.profile.groupmanager.cpi import GroupManagerService
from airavata.service.profile.iam.admin.services.cpi import IamAdminServices
from airavata.service.profile.tenant.cpi import TenantProfileService
from airavata.service.profile.user.cpi import UserProfileService
from airavata_sdk.transport import settings

from airavata.service.profile.groupmanager.cpi.constants import GROUP_MANAGER_CPI_NAME
from airavata.service.profile.iam.admin.services.cpi.constants import IAM_ADMIN_SERVICES_CPI_NAME
from airavata.service.profile.tenant.cpi.constants import TENANT_PROFILE_CPI_NAME
from airavata.service.profile.user.cpi.constants import USER_PROFILE_CPI_NAME

log = logging.getLogger(__name__)

default_api_server_settings = settings.APIServerSettings()
default_profile_server_settings = settings.ProfileServerSettings()
default_sharing_server_settings = settings.SharingServerSettings()
default_credential_store_server_settings = settings.CredentialStoreServerSettings()
default_thrift_settings = settings.ThriftSettings()

T = TypeVar(
  'T',
  Airavata.Client,
  SharingRegistryService.Client,
  GroupManagerService.Client,
  IamAdminServices.Client,
  TenantProfileService.Client,
  UserProfileService.Client,
  CredentialStoreService.Client,
)

class ThriftClient(Generic[T]):
  host: str
  port: int
  secure: bool
  service_name: Optional[str]
  transport: TTransport.TTransportBase
  client: T

  def __init__(self, klass, host: str, port: int, secure: bool = False, service_name: Optional[str] = None):
    self.host = host
    self.port = port
    self.secure = secure
    self.service_name = service_name

    if self.secure:
      self.transport = TSSLSocket.TSSLSocket(self.host, self.port, validate=False, socket_keepalive=True)
    else:
      self.transport = TSocket.TSocket(self.host, self.port, socket_keepalive=True)
    self.transport = TTransport.TBufferedTransport(self.transport)
    protocol = TBinaryProtocol.TBinaryProtocol(self.transport)
    if self.service_name:
      protocol = TMultiplexedProtocol(protocol, self.service_name)
    
    self.client = klass(protocol)

    # open transport at constructor time
    self.transport.open()

  def close(self):
    if self.transport:
      self.transport.close()

  def ping(self):
    assert self.client is not None
    try:
      self.client.getAPIVersion() # type: ignore
    except Exception as e:
      log.debug("getAPIVersion failed: {}".format(str(e)))
      raise


def initialize_api_client_pool(
    host=default_api_server_settings.API_SERVER_HOST,
    port=default_api_server_settings.API_SERVER_PORT,
    secure=default_api_server_settings.API_SERVER_SECURE,
):
  return ThriftClient[Airavata.Client](Airavata.Client, host, port, secure).client


def initialize_group_manager_client(
    host=default_profile_server_settings.PROFILE_SERVICE_HOST,
    port=default_profile_server_settings.PROFILE_SERVICE_PORT,
    secure=default_profile_server_settings.PROFILE_SERVICE_SECURE,
):
  return ThriftClient[GroupManagerService.Client](GroupManagerService.Client, host, port, secure, GROUP_MANAGER_CPI_NAME).client


def initialize_iam_admin_client(
    host=default_profile_server_settings.PROFILE_SERVICE_HOST,
    port=default_profile_server_settings.PROFILE_SERVICE_PORT,
    secure=default_profile_server_settings.PROFILE_SERVICE_SECURE,
):
  return ThriftClient[IamAdminServices.Client](IamAdminServices.Client, host, port, secure, IAM_ADMIN_SERVICES_CPI_NAME).client


def initialize_tenant_profile_client(
    host=default_profile_server_settings.PROFILE_SERVICE_HOST,
    port=default_profile_server_settings.PROFILE_SERVICE_PORT,
    secure=default_profile_server_settings.PROFILE_SERVICE_SECURE,
):
  return ThriftClient[TenantProfileService.Client](TenantProfileService.Client, host, port, secure, TENANT_PROFILE_CPI_NAME).client


def initialize_user_profile_client(
    host=default_profile_server_settings.PROFILE_SERVICE_HOST,
    port=default_profile_server_settings.PROFILE_SERVICE_PORT,
    secure=default_profile_server_settings.PROFILE_SERVICE_SECURE,
):
  return ThriftClient[UserProfileService.Client](UserProfileService.Client, host, port, secure, USER_PROFILE_CPI_NAME).client


def initialize_sharing_registry_client(
    host=default_sharing_server_settings.SHARING_API_HOST,
    port=default_sharing_server_settings.SHARING_API_PORT,
    secure=default_sharing_server_settings.SHARING_API_SECURE,
):
  return ThriftClient[SharingRegistryService.Client](SharingRegistryService.Client, host, port, secure).client


def initialize_credential_store_client(
    host=default_credential_store_server_settings.CREDENTIAL_STORE_API_HOST,
    port=default_credential_store_server_settings.CREDENTIAL_STORE_API_PORT,
    secure=default_credential_store_server_settings.CREDENTIAL_STORE_API_SECURE,
):
  return ThriftClient[CredentialStoreService.Client](CredentialStoreService.Client, host, port, secure).client

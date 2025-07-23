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
import certifi
import ssl
import time
from typing import Optional, TypeVar

from thrift.protocol import TBinaryProtocol
from thrift.protocol.TMultiplexedProtocol import TMultiplexedProtocol
from thrift.transport import TSocket, TSSLSocket, TTransport

from airavata.api import Airavata
from airavata.api.credential.store import CredentialStoreService
from airavata.api.sharing import SharingRegistryService
from airavata.service.profile.groupmanager.cpi import GroupManagerService
from airavata.service.profile.groupmanager.cpi.constants import GROUP_MANAGER_CPI_NAME
from airavata.service.profile.iam.admin.services.cpi import IamAdminServices
from airavata.service.profile.iam.admin.services.cpi.constants import IAM_ADMIN_SERVICES_CPI_NAME
from airavata.service.profile.tenant.cpi import TenantProfileService
from airavata.service.profile.tenant.cpi.constants import TENANT_PROFILE_CPI_NAME
from airavata.service.profile.user.cpi import UserProfileService
from airavata.service.profile.user.cpi.constants import USER_PROFILE_CPI_NAME
from airavata_sdk import Settings

log = logging.getLogger(__name__)

settings = Settings()

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

class ThriftClient:
  host: str
  port: int
  secure: bool
  service_name: Optional[str]
  transport: TTransport.TTransportBase
  max_retries: int
  retry_delay: float

  def __init__(self, klass, host: str, port: int, secure: bool = False, service_name: Optional[str] = None, 
               max_retries: Optional[int] = None, retry_delay: Optional[float] = None):
    self.host = host
    self.port = port
    self.secure = secure
    self.service_name = service_name
    self.max_retries = max_retries or settings.THRIFT_CONNECTION_MAX_RETRIES
    self.retry_delay = retry_delay or settings.THRIFT_CONNECTION_RETRY_DELAY

    # create and validate transport
    self.transport = self._create_transport()
    protocol = TBinaryProtocol.TBinaryProtocol(self.transport)
    if self.service_name:
      protocol = TMultiplexedProtocol(protocol, self.service_name)
    self.client = klass(protocol)

    self._validate_transport()

  def _create_transport(self):
    """Create transport with enhanced SSL configuration"""
    if self.secure:
      transport = TSSLSocket.TSSLSocket(
        self.host,
        self.port,
        cert_reqs=ssl.CERT_REQUIRED,
        ca_certs=certifi.where(),
        socket_keepalive=True,
      )
    else:
      transport = TSocket.TSocket(
        self.host, 
        self.port, 
        socket_keepalive=True,
      )
    return TTransport.TBufferedTransport(transport)

  def _validate_transport(self):
    """Open transport with retry logic to handle connection issues"""
    for attempt in range(self.max_retries):
      try:
        log.debug(f"[AV] Attempting to connect to {self.host}:{self.port} (attempt {attempt + 1}/{self.max_retries})")
        if self.transport.isOpen():
          self.transport.close()
        self.transport.open()
        version = self.client.getAPIVersion() # type: ignore
        log.debug(f"[AV] Connected to {self.host}:{self.port} passed! API version={version}")
        break
      except Exception as e:
        log.debug(f"[AV] Connection attempt {attempt + 1} failed: {repr(e)}")
        time.sleep(self.retry_delay * (attempt + 1))
    else:
      error_msg = f"[AV] Failed to connect to {self.host}:{self.port} after {self.max_retries} attempts"
      log.error(error_msg)
      raise Exception(error_msg)


  def close(self):
    if self.transport:
      try:
        self.transport.close()
      except Exception as e:
        log.warning(f"Error closing transport: {str(e)}")
    


def initialize_api_client_pool(
    host=settings.API_SERVER_HOSTNAME,
    port=settings.API_SERVER_PORT,
    secure=settings.API_SERVER_SECURE,
) -> Airavata.Client:
  return ThriftClient(Airavata.Client, host, port, secure).client


def initialize_group_manager_client(
    host=settings.PROFILE_SERVICE_HOST,
    port=settings.PROFILE_SERVICE_PORT,
    secure=settings.PROFILE_SERVICE_SECURE,
) -> GroupManagerService.Client:
  return ThriftClient(GroupManagerService.Client, host, port, secure, GROUP_MANAGER_CPI_NAME).client


def initialize_iam_admin_client(
    host=settings.PROFILE_SERVICE_HOST,
    port=settings.PROFILE_SERVICE_PORT,
    secure=settings.PROFILE_SERVICE_SECURE,
) -> IamAdminServices.Client:
  return ThriftClient(IamAdminServices.Client, host, port, secure, IAM_ADMIN_SERVICES_CPI_NAME).client


def initialize_tenant_profile_client(
    host=settings.PROFILE_SERVICE_HOST,
    port=settings.PROFILE_SERVICE_PORT,
    secure=settings.PROFILE_SERVICE_SECURE,
) -> TenantProfileService.Client:
  return ThriftClient(TenantProfileService.Client, host, port, secure, TENANT_PROFILE_CPI_NAME).client


def initialize_user_profile_client(
    host=settings.PROFILE_SERVICE_HOST,
    port=settings.PROFILE_SERVICE_PORT,
    secure=settings.PROFILE_SERVICE_SECURE,
) -> UserProfileService.Client:
  return ThriftClient(UserProfileService.Client, host, port, secure, USER_PROFILE_CPI_NAME).client


def initialize_sharing_registry_client(
    host=settings.SHARING_API_HOST,
    port=settings.SHARING_API_PORT,
    secure=settings.SHARING_API_SECURE,
) -> SharingRegistryService.Client:
  return ThriftClient(SharingRegistryService.Client, host, port, secure).client


def initialize_credential_store_client(
    host=settings.CREDENTIAL_STORE_API_HOST,
    port=settings.CREDENTIAL_STORE_API_PORT,
    secure=settings.CREDENTIAL_STORE_API_SECURE,
) -> CredentialStoreService.Client:
  return ThriftClient(CredentialStoreService.Client, host, port, secure).client

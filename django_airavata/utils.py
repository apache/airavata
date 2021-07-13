import logging
import ssl
from contextlib import contextmanager

import thrift_connector.connection_pool as connection_pool
from airavata.api import Airavata
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
from django.conf import settings
from thrift.protocol import TBinaryProtocol
from thrift.protocol.TMultiplexedProtocol import TMultiplexedProtocol
from thrift.transport import TSocket, TSSLSocket, TTransport

log = logging.getLogger(__name__)


class ThriftConnectionException(Exception):
    pass


class ThriftClientException(Exception):
    pass


def get_unsecure_transport(hostname, port):
    # Create a socket to the Airavata Server
    transport = TSocket.TSocket(hostname, port)

    # Use Buffered Protocol to speedup over raw sockets
    transport = TTransport.TBufferedTransport(transport)
    return transport


def get_secure_transport(hostname, port):

    # Create a socket to the Airavata Server
    # TODO: validate server certificate
    transport = TSSLSocket.TSSLSocket(hostname, port, validate=False)

    # Use Buffered Protocol to speedup over raw sockets
    transport = TTransport.TBufferedTransport(transport)
    return transport


def get_transport(hostname, port, secure=True):
    if secure:
        transport = get_secure_transport(hostname, port)
    else:
        transport = get_unsecure_transport(hostname, port)
    return transport


def create_airavata_client(transport):

    # Airavata currently uses Binary Protocol
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    # Create a Airavata client to use the protocol encoder
    client = Airavata.Client(protocol)
    return client


def get_binary_protocol(transport):
    return TBinaryProtocol.TBinaryProtocol(transport)


def create_group_manager_client(transport):
    protocol = get_binary_protocol(transport)
    multiplex_prot = TMultiplexedProtocol(protocol, GROUP_MANAGER_CPI_NAME)
    return GroupManagerService.Client(multiplex_prot)


def create_iamadmin_client(transport):
    protocol = get_binary_protocol(transport)
    multiplex_prot = TMultiplexedProtocol(protocol,
                                          IAM_ADMIN_SERVICES_CPI_NAME)
    return IamAdminServices.Client(multiplex_prot)


def create_tenant_profile_client(transport):
    protocol = get_binary_protocol(transport)
    multiplex_prot = TMultiplexedProtocol(protocol, TENANT_PROFILE_CPI_NAME)
    return TenantProfileService.Client(multiplex_prot)


def create_user_profile_client(transport):
    protocol = get_binary_protocol(transport)
    multiplex_prot = TMultiplexedProtocol(protocol, USER_PROFILE_CPI_NAME)
    return UserProfileService.Client(multiplex_prot)


def get_airavata_client():
    """Get Airavata API client as context manager (use in `with statement`)."""
    return get_thrift_client(settings.AIRAVATA_API_HOST,
                             settings.AIRAVATA_API_PORT,
                             settings.AIRAVATA_API_SECURE,
                             create_airavata_client)


def get_group_manager_client():
    """Group Manager client as context manager (use in `with statement`)."""
    return get_thrift_client(settings.PROFILE_SERVICE_HOST,
                             settings.PROFILE_SERVICE_PORT,
                             settings.PROFILE_SERVICE_SECURE,
                             create_group_manager_client)


def get_iam_admin_client():
    """IAM Admin client as context manager (use in `with statement`)."""
    return get_thrift_client(settings.PROFILE_SERVICE_HOST,
                             settings.PROFILE_SERVICE_PORT,
                             settings.PROFILE_SERVICE_SECURE,
                             create_iamadmin_client)


def get_tenant_profile_client():
    """Tenant Profile client as context manager (use in `with statement`)."""
    return get_thrift_client(settings.PROFILE_SERVICE_HOST,
                             settings.PROFILE_SERVICE_PORT,
                             settings.PROFILE_SERVICE_SECURE,
                             create_tenant_profile_client)


def get_user_profile_client():
    """User Profile client as context manager (use in `with statement`)."""
    return get_thrift_client(settings.PROFILE_SERVICE_HOST,
                             settings.PROFILE_SERVICE_PORT,
                             settings.PROFILE_SERVICE_SECURE,
                             create_user_profile_client)


@contextmanager
def get_thrift_client(host, port, is_secure, client_generator):
    transport = get_transport(host, port, is_secure)
    client = client_generator(transport)

    try:
        transport.open()
        log.debug("Thrift connection opened to {}:{}, "
                  "secure={}".format(host, port, is_secure))
        try:
            yield client
        except Exception as e:
            log.exception("Thrift client error occurred")
            raise ThriftClientException(
                "Thrift client error occurred: " + str(e)) from e
        finally:
            if transport.isOpen():
                transport.close()
                log.debug("Thrift connection closed to {}:{}, "
                          "secure={}".format(host, port, is_secure))
    except ThriftClientException as tce:
        # Allow thrift client errors to bubble up
        raise tce
    except Exception as e:
        msg = "Failed to open thrift connection to {}:{}, secure={}".format(
            host, port, is_secure)
        log.debug(msg)
        raise ThriftConnectionException(msg) from e


class CustomThriftClient(connection_pool.ThriftClient):
    secure = False
    validate = False

    @classmethod
    def get_socket_factory(cls):
        if not cls.secure:
            return super().get_socket_factory()
        else:
            def factory(host, port):
                return TSSLSocket.TSSLSocket(host, port,
                                             cert_reqs=(ssl.CERT_REQUIRED
                                                        if cls.validate
                                                        else ssl.CERT_NONE))
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
    secure = settings.AIRAVATA_API_SECURE


class GroupManagerServiceThriftClient(MultiplexThriftClientMixin,
                                      CustomThriftClient):
    service_name = GROUP_MANAGER_CPI_NAME
    secure = settings.PROFILE_SERVICE_SECURE


class IAMAdminServiceThriftClient(MultiplexThriftClientMixin,
                                  CustomThriftClient):
    service_name = IAM_ADMIN_SERVICES_CPI_NAME
    secure = settings.PROFILE_SERVICE_SECURE


class TenantProfileServiceThriftClient(MultiplexThriftClientMixin,
                                       CustomThriftClient):
    service_name = TENANT_PROFILE_CPI_NAME
    secure = settings.PROFILE_SERVICE_SECURE


class UserProfileServiceThriftClient(MultiplexThriftClientMixin,
                                     CustomThriftClient):
    service_name = USER_PROFILE_CPI_NAME
    secure = settings.PROFILE_SERVICE_SECURE


airavata_api_client_pool = connection_pool.ClientPool(
    Airavata,
    settings.AIRAVATA_API_HOST,
    settings.AIRAVATA_API_PORT,
    connection_class=AiravataAPIThriftClient,
    keepalive=settings.THRIFT_CLIENT_POOL_KEEPALIVE
)
group_manager_client_pool = connection_pool.ClientPool(
    GroupManagerService,
    settings.PROFILE_SERVICE_HOST,
    settings.PROFILE_SERVICE_PORT,
    connection_class=GroupManagerServiceThriftClient,
    keepalive=settings.THRIFT_CLIENT_POOL_KEEPALIVE
)
iamadmin_client_pool = connection_pool.ClientPool(
    IamAdminServices,
    settings.PROFILE_SERVICE_HOST,
    settings.PROFILE_SERVICE_PORT,
    connection_class=IAMAdminServiceThriftClient,
    keepalive=settings.THRIFT_CLIENT_POOL_KEEPALIVE
)
tenant_profile_client_pool = connection_pool.ClientPool(
    TenantProfileService,
    settings.PROFILE_SERVICE_HOST,
    settings.PROFILE_SERVICE_PORT,
    connection_class=TenantProfileServiceThriftClient,
    keepalive=settings.THRIFT_CLIENT_POOL_KEEPALIVE
)
user_profile_client_pool = connection_pool.ClientPool(
    UserProfileService,
    settings.PROFILE_SERVICE_HOST,
    settings.PROFILE_SERVICE_PORT,
    connection_class=UserProfileServiceThriftClient,
    keepalive=settings.THRIFT_CLIENT_POOL_KEEPALIVE
)

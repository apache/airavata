
from airavata.api import Airavata
from airavata.api.sharing import SharingRegistryService
from airavata.service.profile.groupmanager.cpi.constants \
    import GROUP_MANAGER_CPI_NAME
from airavata.service.profile.groupmanager.cpi import GroupManagerService
from airavata.service.profile.iam.admin.services.cpi.constants \
    import IAM_ADMIN_SERVICES_CPI_NAME
from airavata.service.profile.iam.admin.services.cpi import IamAdminServices
from airavata.service.profile.tenant.cpi.constants \
    import TENANT_PROFILE_CPI_NAME
from airavata.service.profile.tenant.cpi import TenantProfileService
from airavata.service.profile.user.cpi.constants \
    import USER_PROFILE_CPI_NAME
from airavata.service.profile.user.cpi import UserProfileService

from thrift import Thrift
from thrift.transport import TSSLSocket
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.protocol.TMultiplexedProtocol import TMultiplexedProtocol

from django.conf import settings

import logging

logger = logging.getLogger(__name__)

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

def get_airavata_client(transport):

    # Airavata currently uses Binary Protocol
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    # Create a Airavata client to use the protocol encoder
    client=Airavata.Client(protocol)
    return client

def get_sharing_client(transport):

    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    return SharingRegistryService.Client(protocol)


def get_binary_protocol(transport):
    return TBinaryProtocol.TBinaryProtocol(transport)


def get_group_manager_client(protocol):
    multiplex_prot = TMultiplexedProtocol(protocol, GROUP_MANAGER_CPI_NAME)
    return GroupManagerService.Client(multiplex_prot)


def get_iamadmin_client(protocol):
    multiplex_prot = TMultiplexedProtocol(protocol, IAM_ADMIN_SERVICES_CPI_NAME)
    return IamAdminServices.Client(multiplex_prot)


def get_tenant_profile_client(protocol):
    multiplex_prot = TMultiplexedProtocol(protocol, TENANT_PROFILE_CPI_NAME)
    return TenantProfileService.Client(multiplex_prot)


def get_user_profile_client(protocol):
    multiplex_prot = TMultiplexedProtocol(protocol, USER_PROFILE_CPI_NAME)
    return UserProfileService.Client(multiplex_prot)


def airavata_client(get_response):
    "Open and close Airavata client for each request"

    def middleware(request):

        # If user is logged in create an airavata api client for the request
        if request.user.is_authenticated:
            transport = get_transport(settings.AIRAVATA_API_HOST, settings.AIRAVATA_API_PORT, settings.AIRAVATA_API_SECURE)
            airavata_client = get_airavata_client(transport)

            try:
                transport.open()
            except Exception as e:
                logger.exception("Failed to open thrift connection to API server")

            if transport.isOpen():
                request.airavata_client = airavata_client
            else:
                # if request.airavata_client is None, this will indicate to view
                # code that the API server is down
                request.airavata_client = None

            response = get_response(request)

            if transport.isOpen():
                transport.close()
        else:
            response = get_response(request)

        return response

    return middleware

def sharing_client(get_response):
    "Open and close Sharing registry client for each request"

    def middleware(request):

        # If user is logged in create an airavata api client for the request
        if request.user.is_authenticated:
            transport = get_transport(settings.SHARING_API_HOST, settings.SHARING_API_PORT, settings.SHARING_API_SECURE)
            sharing_client = get_sharing_client(transport)

            try:
                transport.open()
            except Exception as e:
                logger.exception("Failed to open thrift connection to Sharing Registry server")

            if transport.isOpen():
                request.sharing_client = sharing_client
            else:
                # if request.sharing_client is None, this will indicate to view
                # code that the Sharing server is down
                request.sharing_client = None

            response = get_response(request)

            if transport.isOpen():
                transport.close()
        else:
            response = get_response(request)

        return response

    return middleware


def profile_service_client(get_response):
    """Open and close Profile Service client for each request.

    Usage:
        request.profile_service['group_manager'].getGroup(
            request.authz_token, groupId)
    """
    def middleware(request):

        # If user is logged in create a profile service client for the request
        if request.user.is_authenticated:
            transport = get_transport(settings.PROFILE_SERVICE_HOST,
                                      settings.PROFILE_SERVICE_PORT,
                                      settings.PROFILE_SERVICE_SECURE)
            binary_prot = get_binary_protocol(transport)
            group_manager_client = get_group_manager_client(binary_prot)
            iam_admin_client = get_iamadmin_client(binary_prot)
            tenant_profile_client = get_tenant_profile_client(binary_prot)
            user_profile_client = get_user_profile_client(binary_prot)

            try:
                transport.open()
            except Exception as e:
                logger.exception("Failed to open thrift connection to "
                                 "Profile Service server")

            if transport.isOpen():
                request.profile_service = {
                    'group_manager': group_manager_client,
                    'iam_admin': iam_admin_client,
                    'tenant_profile': tenant_profile_client,
                    'user_profile': user_profile_client,
                }
            else:
                # if request.profile_service is None, this will indicate to
                # view code that the Profile Service is down
                request.profile_service = None

            response = get_response(request)

            if transport.isOpen():
                transport.close()
        else:
            response = get_response(request)

        return response

    return middleware

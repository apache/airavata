
from apache.airavata.api import Airavata

from thrift import Thrift
from thrift.transport import TSSLSocket
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

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

def get_transport():
    if settings.AIRAVATA_API_SECURE:
        transport = get_secure_transport(settings.AIRAVATA_API_HOST, settings.AIRAVATA_API_PORT)
    else:
        transport = get_unsecure_transport(settings.AIRAVATA_API_HOST, settings.AIRAVATA_API_PORT)
    return transport

def get_airavata_client(transport):

    # Airavata currently uses Binary Protocol
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    # Create a Airavata client to use the protocol encoder
    return Airavata.Client(protocol)

def airavata_client(get_response):
    "Open and close Airavata client for each request"

    def middleware(request):

        # If user is logged in create an airavata api client for the request
        if request.user.is_authenticated:
            transport = get_transport()
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
                logger.debug("transport closed in middleware")
        else:
            response = get_response(request)

        return response

    return middleware

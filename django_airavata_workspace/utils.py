
from apache.airavata.api import Airavata

from thrift import Thrift
from thrift.transport import TSSLSocket
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

from django.conf import settings

def get_transport(hostname, port):
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

def get_airavata_client(transport=None):

    if not transport:
        if settings.AIRAVATA_API_SECURE:
            transport = get_secure_transport(settings.AIRAVATA_API_HOST, settings.AIRAVATA_API_PORT)
        else:
            transport = get_transport(settings.AIRAVATA_API_HOST, settings.AIRAVATA_API_PORT)
        transport.open()

    # Airavata currently uses Binary Protocol
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    # Create a Airavata client to use the protocol encoder
    return Airavata.Client(protocol)

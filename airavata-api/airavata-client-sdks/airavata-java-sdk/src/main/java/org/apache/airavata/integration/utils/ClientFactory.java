package org.apache.airavata.integration.utils;

import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.api.Airavata;

public class ClientFactory implements ThriftClientPool.ClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);

    private String serverHost;
    private int port;
    private String trustStorePath;
    private String trustStorePassword;
    int clientTimeOut;


    public ClientFactory(String serverHost, int port, String trustStorePath, String trustStorePassword, int clientTimeOut) {
        this.serverHost = serverHost;
        this.port = port;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.clientTimeOut = clientTimeOut;
    }

    @Override
    public Object make(TProtocol tProtocol) {
        try {
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            params.setTrustStore(trustStorePath, trustStorePassword);
            TSocket transport = TSSLTransportFactory.getClientSocket(serverHost, port, clientTimeOut, params);
            TProtocol protocol = new TBinaryProtocol(transport);
            return new Airavata.Client(protocol);
        } catch (TTransportException e) {
            LOGGER.error("Unable to connect to the server at " + serverHost + ":" + port);
        }
        return null;
    }

}

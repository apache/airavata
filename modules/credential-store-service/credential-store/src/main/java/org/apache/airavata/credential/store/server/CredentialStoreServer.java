
package org.apache.airavata.credential.store.server;


import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class CredentialStoreServer  implements IServer {
    private final static Logger logger = LoggerFactory.getLogger(CredentialStoreServer.class);
    private static final String SERVER_NAME = "Credential Store Server";
    private static final String SERVER_VERSION = "1.0";

    private IServer.ServerStatus status;
    private TServer server;

    public CredentialStoreServer() {
        setStatus(IServer.ServerStatus.STOPPED);
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }

    @Override
    public void start() throws Exception {
        try {
            setStatus(ServerStatus.STARTING);
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            String keystorePath = ServerSettings.getCredentialStoreKeyStorePath();
            String keystorePWD = ServerSettings.getCredentialStoreKeyStorePassword();
            String host = ServerSettings.getCredentialStoreServerHost();
            final int port = Integer.valueOf(ServerSettings.getCredentialStoreServerPort());
            params.setKeyStore(keystorePath, keystorePWD);

            TServerSocket serverTransport = TSSLTransportFactory.getServerSocket(
                    port, 10000, InetAddress.getByName(host), params);
            CredentialStoreService.Processor processor = new CredentialStoreService.Processor(new CredentialStoreServerHandler());

            server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
                    processor(processor));
            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(ServerStatus.STOPPED);
                    logger.info("Credential Store Server Stopped.");
                }
            }.start();
            new Thread() {
                public void run() {
                    while(!server.isServing()){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (server.isServing()){
                        setStatus(ServerStatus.STARTED);
                        logger.info("Starting Credential Store Server on Port " + port);
                        logger.info("Listening to Credential Store Clients ....");
                    }
                }
            }.start();
        } catch (TTransportException e) {
            setStatus(ServerStatus.FAILED);
            logger.error("Error while starting the credential store service", e);
            throw new Exception("Error while starting the credential store service", e);
        }
    }

    public static void main(String[] args) {
        try {
            new CredentialStoreServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void stop() throws Exception {
        if (server!=null && server.isServing()){
            setStatus(ServerStatus.STOPING);
            server.stop();
        }
    }

    @Override
    public void restart() throws Exception {
        stop();
        start();
    }

    @Override
    public void configure() throws Exception {

    }

    @Override
    public ServerStatus getStatus() throws Exception {
        return null;
    }

    private void setStatus(IServer.ServerStatus stat){
        status=stat;
        status.updateTime();
    }

    public TServer getServer() {
        return server;
    }

    public void setServer(TServer server) {
        this.server = server;
    }

}

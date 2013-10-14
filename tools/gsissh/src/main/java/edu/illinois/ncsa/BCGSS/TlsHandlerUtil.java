package edu.illinois.ncsa.BCGSS;
import edu.illinois.ncsa.bouncycastle.crypto.tls.*;

import java.io.*;

public class TlsHandlerUtil {
    private TlsProtocolHandler tlsHandler;
    private TlsClient tlsClient;
    private CircularByteBuffer netInStream;
    private ByteArrayOutputStream netOutStream;
    private boolean connectionThreadStarted = false;
    private IOException connectionThreadException = null;

    /*
    public TlsHandlerUtil(TlsClient client) {
        this(client, new TlsProtocolVersion[] {TlsProtocolVersion.TLSv10,
                                               TlsProtocolVersion.SSLv3});
    }
    */

    //public TlsHandlerUtil(TlsClient client, TlsProtocolVersion[] protocols) {
    public TlsHandlerUtil(TlsClient client) {
        this.tlsClient = client;

        this.netInStream = new CircularByteBuffer(
                CircularByteBuffer.INFINITE_SIZE);

        //TODO: set a good initial size of buffer?
        this.netOutStream = new ByteArrayOutputStream();

        this.tlsHandler = new TlsProtocolHandler(
                netInStream.getInputStream(), netOutStream);
        //this.tlsHandler.setEnabledProtocols(protocols);
    }

    /**
     *
     * @param inNetBuf
     * @return
     */
    public byte[] nextHandshakeToken(byte[] inNetBuf) throws IOException {
        return nextHandshakeToken(inNetBuf, 0, inNetBuf.length);
    }

    /**
     *
     * @param inNetBuf
     * @param off
     * @param len
     * @return
     * @throws java.io.IOException
     */
    public byte[] nextHandshakeToken(byte[] inNetBuf, int off, int len)
            throws IOException {
        if (isHandshakeFinished()) {
            return null;
        }

        if (! isConnectionThreadStarted()) {
            (new ConnectionThread()).start();
        }


        if (tlsHandler.getHandshakeBlocking() > 0) {
            tlsHandler.decHandshakeBlocking(inNetBuf.length);
        }

        netInStream.getOutputStream().write(inNetBuf, off, len);

        // block until the TlsProtocolHandler's record stream blocks
        // or until the handshake is finished.  After either, a handshake
        // token may have been produced
        while (tlsHandler.getHandshakeBlocking() == 0 &&
               ! isHandshakeFinished()) {

            IOException e = getConnectionThreadException();
            if (e != null) {
                throw new IOException("TLS connection thread exception", e);
            }

            try {
                Thread.sleep(25);
            } catch (InterruptedException e1) {
                throw new IOException("Handshake interrupted while waiting " +
                        "for new network data to be processed", e1);
            }
        }

        byte[] token = drainNetOutStream();

        if (token.length > 0) {
            return token;
        }

        if (tlsHandler.getHandshakeBlocking() > 0) {
            // no token produced; need more data
            return null;
        }

        if (isHandshakeFinished()) {
            return null;
        } else {
            throw new IOException("No handshake data available, but the " +
                "record stream is not blocking and wasn't interrupted");
        }
    }

    /**
     * 
     * @param appData
     * @return
     * @throws IOException
     */
    public byte[] wrap(byte[] appData) throws IOException {
        return wrap(appData, 0, appData.length);
    }

    /**
     *
     * @param appData
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public byte[] wrap(byte[] appData, int off, int len) throws IOException {
        if (! isHandshakeFinished()) {
            return null;
        }

        tlsHandler.getOutputStream().write(appData, off, len);
        return drainNetOutStream();
    }

    /**
     *
     * @param netData
     * @return
     * @throws IOException
     */
    public byte[] unwrap(byte[] netData) throws IOException {
        return unwrap(netData, 0, netData.length);
    }

    /**
     *
     * @param netData
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public byte[] unwrap(byte[] netData, int off, int len) throws IOException {
        if (! isHandshakeFinished()) {
            return null;
        }

        if (netData.length == 0) {
            return null;
        }

        netInStream.getOutputStream().write(netData, off, len);

        // Force the record to be processed in order to put an unknown
        // amount of data in the application queue.  It's assumed that
        // the netData parameter is a full SSL record; if it's not, then
        // this method will block indefinitely
        byte[] tmp = new byte[1];
        tlsHandler.getInputStream().read(tmp, 0, 1);

        int avail = tlsHandler.getApplicationDataQueueSize();

        if (avail == 0) {
            return tmp;
        }

        byte[] appBuf = new byte[avail + 1];
        appBuf[0] = tmp[0];
        tlsHandler.getInputStream().read(appBuf, 1, avail);

        return appBuf;
    }

    /**
     *
     * @return
     * @throws java.io.IOException
     */
    public byte[] close() throws IOException {
        tlsHandler.close();
        return drainNetOutStream();
    }

    /**
     *
     * @return
     */
    public boolean isHandshakeFinished() {
        return this.tlsHandler.isHandshakeFinished();
    }

    /**
     *
     * @return
     */
    private byte[] drainNetOutStream() {
        byte[] rval = netOutStream.toByteArray();
        netOutStream.reset();
        return rval;
    }

    /**
     *
     * @param b
     */
    private synchronized void setConnectionThreadStarted(boolean b) {
        connectionThreadStarted = b;
    }

    /**
     *
     * @return
     */
    private synchronized boolean isConnectionThreadStarted() {
        return connectionThreadStarted;
    }

    /**
     *
     * @return
     */
    private IOException getConnectionThreadException() {
        return connectionThreadException;
    }

    /**
     *
     * @param e
     */
    private void setConnectionThreadException(IOException e) {
        this.connectionThreadException = e;
    }

    /**
     *
     */
    private class ConnectionThread extends Thread {
        /**
         *
         */
        public void run() {
            setConnectionThreadStarted(true);
            try {
                tlsHandler.connect(tlsClient);
            } catch (IOException e) {
                setConnectionThreadException(e);
            }
            //System.out.println("TLS connection thread done");
        }
    }
}

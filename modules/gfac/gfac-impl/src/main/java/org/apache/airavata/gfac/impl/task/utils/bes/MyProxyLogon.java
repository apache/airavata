/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.apache.airavata.gfac.impl.task.utils.bes;

import eu.emi.security.authn.x509.CommonX509TrustManager;
import eu.emi.security.authn.x509.X509CertChainValidator;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Base64;

import javax.net.ssl.*;
import javax.security.auth.login.FailedLoginException;
import java.io.*;
import java.net.ProtocolException;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The MyProxyLogon class provides an interface for retrieving credentials from
 * a MyProxy server.
 * <p/>
 * First, use <code>setHost</code>, <code>setPort</code>,
 * <code>setUsername</code>, <code>setPassphrase</code>,
 * <code>setCredentialName</code>, <code>setLifetime</code> and
 * <code>requestTrustRoots</code> to configure. Then call <code>connect</code>,
 * <code>logon</code>, <code>getCredentials</code>, then
 * <code>disconnect</code>. Use <code>getCertificates</code> and
 * <code>getPrivateKey</code> to access the retrieved credentials, or
 * <code>writeProxyFile</code> or <code>saveCredentialsToFile</code> to
 * write them to a file. Use <code>writeTrustRoots</code>,
 * <code>getTrustedCAs</code>, <code>getCRLs</code>,
 * <code>getTrustRootData</code>, and <code>getTrustRootFilenames</code>
 * for trust root information.
 *
 * (modified for use with UNICORE)
 *
 * @version 1.1
 * @see <a href="http://myproxy.ncsa.uiuc.edu/">MyProxy Project Home Page</a>
 * 
 */
public class MyProxyLogon {
    
	public final static String version = "1.1";

    private enum State {
        READY, CONNECTED, LOGGEDON, DONE
    }

    public final static String VERSION = "VERSION=MYPROXYv2";
    private final static String GETCOMMAND = "COMMAND=0";
    private final static String TRUSTROOTS = "TRUSTED_CERTS=";
    private final static String USERNAME = "USERNAME=";
    private final static String PASSPHRASE = "PASSPHRASE=";
    private final static String LIFETIME = "LIFETIME=";
    private final static String CREDNAME = "CRED_NAME=";
    public final static String RESPONSE = "RESPONSE=";
    private final static String ERROR = "ERROR=";
    private final static String DN = "CN=ignore";

    public final int DEFAULT_KEY_SIZE = 2048;
    private int keySize = DEFAULT_KEY_SIZE;
    private final static String keyAlg = "RSA";
    private State state = State.READY;
    private String host = "localhost";
    private String username;
    private String credname;
    private char[] passphrase;
    private int port = 7512;
    private int lifetime = 43200;
    private SSLSocket socket;
    private BufferedInputStream socketIn;
    private BufferedOutputStream socketOut;
    private KeyPair keypair;
    private Collection<X509Certificate> certificateChain;
    private String[] trustrootFilenames;
    private String[] trustrootData;
    private KeyManagerFactory keyManagerFactory;
    private TrustManager trustManager;
    
    /**
     * Constructs a MyProxyLogon object.
     */
    public MyProxyLogon() {
        super();
        host = System.getenv("MYPROXY_SERVER");
        if (host == null) {
            host = "myproxy.teragrid.org";
        }
        String portString = System.getenv("MYPROXY_SERVER_PORT");
        if (portString != null) {
            port = Integer.parseInt(portString);
        }
        username = System.getProperty("user.name");
    }

    
    /**
     * sets the internal trust manager using the supplied validator
     */
    public void setValidator(X509CertChainValidator validator){
    	 CommonX509TrustManager mtm = new CommonX509TrustManager(validator);
    	 setTrustManager(mtm);
    }

    /**
     * Sets the hostname of the MyProxy server. Defaults to localhost.
     *
     * @param host MyProxy server hostname
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the port of the MyProxy server. Defaults to 7512.
     *
     * @param port MyProxy server port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the key size.
     *
     * @param keySize
     */
    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    /**
     * Gets the MyProxy username.
     *
     * @return MyProxy server port
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the MyProxy username. Defaults to user.name.
     *
     * @param username MyProxy username
     */
    public void setUsername(String username) {
    	this.username = username;
    }

    /**
     * Sets the optional MyProxy credential name.
     *
     * @param credname credential name
     */
    public void setCredentialName(String credname) {
       this.credname = credname;
    }

    /**
     * Sets the MyProxy passphrase.
     *
     * @param passphrase MyProxy passphrase
     */
    public void setPassphrase(char[] passphrase) {
       this.passphrase = passphrase;
    }

    /**
     * Sets the requested credential lifetime. Defaults to 43200 seconds (12
     * hours).
     *
     * @param seconds Credential lifetime
     */
    public void setLifetime(int seconds) {
        lifetime = seconds;
    }

    /**
     * Gets the certificates returned from the MyProxy server by
     * getCredentials().
     *
     * @return Collection of java.security.cert.Certificate objects
     */
    public Collection<X509Certificate> getCertificates() {
        return certificateChain;
    }

    
    // for unit testing
    static PrivateKey testingPrivateKey;
    
    /**
     * Gets the private key generated by getCredentials().
     *
     * @return PrivateKey
     */
    public PrivateKey getPrivateKey() {
    	if(testingPrivateKey!=null){
			//for unit testing
			return testingPrivateKey;	
		}
		return keypair.getPrivate();
    }

    /**
     * Connects to the MyProxy server at the desired host and port. Requires
     * host authentication via SSL. The host's certificate subject must
     * match the requested hostname. If CA certificates are found in the
     * standard GSI locations, they will be used to verify the server's
     * certificate. If trust roots are requested and no CA certificates are
     * found, the server's certificate will still be accepted.
     */
    public void connect() throws IOException, GeneralSecurityException {
        SSLContext sc = SSLContext.getInstance("SSL");
        if(trustManager==null){
        	throw new IllegalStateException("No trust manager has been set!");
        }
        TrustManager[] trustAllCerts = new TrustManager[]{trustManager};
        sc.init(getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
        SSLSocketFactory sf = sc.getSocketFactory();
        socket = (SSLSocket) sf.createSocket(host, port);
        socket.startHandshake();
        socketIn = new BufferedInputStream(socket.getInputStream());
        socketOut = new BufferedOutputStream(socket.getOutputStream());
        state = State.CONNECTED;
    }

    /**
     * Set the key manager factory for use in client-side SSLSocket
     * certificate-based authentication to the MyProxy server.
     * Call this before connect().
     *
     * @param keyManagerFactory Key manager factory to use
     */
    public void setKeyManagerFactory(KeyManagerFactory keyManagerFactory) {
        this.keyManagerFactory = keyManagerFactory;
    }


	public void setTrustManager(TrustManager trustManager) {
		this.trustManager = trustManager;
	}

	/**
     * Disconnects from the MyProxy server.
     */
    public void disconnect() throws IOException {
        socket.close();
        socket = null;
        socketIn = null;
        socketOut = null;
        state = State.READY;
    }

    /**
     * Logs on to the MyProxy server by issuing the MyProxy GET command.
     */
    public void logon() throws IOException, GeneralSecurityException {
        String line;
        char response;

        if (state != State.CONNECTED) {
            connect();
        }

        socketOut.write('0');
        socketOut.flush();
        socketOut.write(VERSION.getBytes());
        socketOut.write('\n');
        socketOut.write(GETCOMMAND.getBytes());
        socketOut.write('\n');
        socketOut.write(USERNAME.getBytes());
        socketOut.write(username.getBytes());
        socketOut.write('\n');
        socketOut.write(PASSPHRASE.getBytes());
        socketOut.write(new String(passphrase).getBytes());
        socketOut.write('\n');
        socketOut.write(LIFETIME.getBytes());
        socketOut.write(Integer.toString(lifetime).getBytes());
        socketOut.write('\n');
        if (credname != null) {
            socketOut.write(CREDNAME.getBytes());
            socketOut.write(credname.getBytes());
            socketOut.write('\n');
        }
        socketOut.flush();

        line = readLine(socketIn);
        if (line == null) {
            throw new EOFException();
        }
        if (!line.equals(VERSION)) {
            throw new ProtocolException("bad MyProxy protocol VERSION string: "
                    + line);
        }
        line = readLine(socketIn);
        if (line == null) {
            throw new EOFException();
        }
        if (!line.startsWith(RESPONSE)
                || line.length() != RESPONSE.length() + 1) {
            throw new ProtocolException(
                    "bad MyProxy protocol RESPONSE string: " + line);
        }
        response = line.charAt(RESPONSE.length());
        if (response == '1') {
            StringBuffer errString;

            errString = new StringBuffer("MyProxy logon failed");
            while ((line = readLine(socketIn)) != null) {
                if (line.startsWith(ERROR)) {
                    errString.append('\n');
                    errString.append(line.substring(ERROR.length()));
                }
            }
            throw new FailedLoginException(errString.toString());
        } else if (response == '2') {
            throw new ProtocolException(
                    "MyProxy authorization RESPONSE not implemented");
        } else if (response != '0') {
            throw new ProtocolException(
                    "unknown MyProxy protocol RESPONSE string: " + line);
        }
        while ((line = readLine(socketIn)) != null) {
            if (line.startsWith(TRUSTROOTS)) {
                String filenameList = line.substring(TRUSTROOTS.length());
                trustrootFilenames = filenameList.split(",");
                trustrootData = new String[trustrootFilenames.length];
                for (int i = 0; i < trustrootFilenames.length; i++) {
                    String lineStart = "FILEDATA_" + trustrootFilenames[i]
                            + "=";
                    line = readLine(socketIn);
                    if (line == null) {
                        throw new EOFException();
                    }
                    if (!line.startsWith(lineStart)) {
                        throw new ProtocolException(
                                "bad MyProxy protocol RESPONSE: expecting "
                                        + lineStart + " but received " + line);
                    }
                    trustrootData[i] = new String(Base64.decode(line
                            .substring(lineStart.length())));
                }
            }
        }
        state = State.LOGGEDON;
    }

   
    /**
     * Retrieves credentials from the MyProxy server.
     */
    public void getCredentials() throws IOException, GeneralSecurityException {

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(keyAlg);
        keyGenerator.initialize(keySize);
        keypair = keyGenerator.genKeyPair();
        Security.addProvider(new BouncyCastleProvider());
        
        org.bouncycastle.pkcs.PKCS10CertificationRequest pkcs10 = null;
        try{
        	pkcs10 = generateCertificationRequest(DN, keypair);
        }
        catch(Exception ex){
        	throw new GeneralSecurityException(ex);
        }
        getCredentials(pkcs10.getEncoded());
    }

    
    public X509Certificate getCertificate() {
        if (certificateChain == null) {
           return null;
        }
        Iterator<X509Certificate> iter = this.certificateChain.iterator();
        return iter.next();
    }
    
    
    private KeyManager[] getKeyManagers() {
        return keyManagerFactory != null? keyManagerFactory.getKeyManagers() : null ;
    }

    private void getCredentials(byte[] derEncodedCertRequest) throws IOException, GeneralSecurityException {
        if (state != State.LOGGEDON) {
            logon();
        }
        socketOut.write(derEncodedCertRequest);
        socketOut.flush();
        int numCertificates = socketIn.read();
        if (numCertificates == -1) {
            throw new IOException("Error: connection aborted");
        } else if (numCertificates == 0 || numCertificates < 0) {
            throw new GeneralSecurityException("Error: bad number of certificates sent by server");
        }
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        certificateChain = new ArrayList<X509Certificate>();
        for(int i = 0; i<numCertificates; i++){
        	X509Certificate c = (X509Certificate)certFactory.generateCertificate(socketIn);
        	certificateChain.add(c);
        }
        state = State.DONE;
    }

    private String readLine(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (int c = is.read(); c > 0 && c != '\n'; c = is.read()) {
            sb.append((char) c);
        }
        if (sb.length() > 0) {
            return new String(sb);
        }
        return null;
    }

	private org.bouncycastle.pkcs.PKCS10CertificationRequest generateCertificationRequest(String dn, KeyPair kp)
			throws Exception{
		X500Name subject=new X500Name(dn);
		PublicKey pubKey=kp.getPublic();
		PrivateKey privKey=kp.getPrivate();
		AsymmetricKeyParameter pubkeyParam = PublicKeyFactory.createKey(pubKey.getEncoded());
		SubjectPublicKeyInfo publicKeyInfo=SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubkeyParam);
		PKCS10CertificationRequestBuilder builder=new PKCS10CertificationRequestBuilder(subject, publicKeyInfo);
		AlgorithmIdentifier signatureAi = new AlgorithmIdentifier(OIWObjectIdentifiers.sha1WithRSA);
		BcRSAContentSignerBuilder signerBuilder=new BcRSAContentSignerBuilder(
				signatureAi, AlgorithmIdentifier.getInstance(OIWObjectIdentifiers.idSHA1));
		AsymmetricKeyParameter pkParam = PrivateKeyFactory.createKey(privKey.getEncoded());
		ContentSigner signer=signerBuilder.build(pkParam);
		return builder.build(signer);
	}
}


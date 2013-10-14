package edu.illinois.ncsa.BCGSS;

import org.bouncycastle.jce.provider.X509CertificateObject;
import org.globus.gsi.*;
import org.globus.gsi.bc.*;
import org.globus.gsi.gssapi.*;
import org.globus.gsi.util.*;
import org.gridforum.jgss.*;
import org.ietf.jgss.*;
import org.ietf.jgss.Oid;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;

public class BCGSSContextImpl implements ExtendedGSSContext {
    /**
     * Used to distinguish between a token created by
     * <code>wrap</code> with {@link GSSConstants#GSI_BIG
     * GSSConstants.GSI_BIG}
     * QoP and a regular token created by <code>wrap</code>.
     */
    public static final int GSI_WRAP = 26;
    /**
     * SSL3_RT_GSSAPI_OPENSSL
     */

    private static final int GSI_SEQUENCE_SIZE = 8;

    private static final int GSI_MESSAGE_DIGEST_PADDING = 12;

    private static final byte[] SSLHANDSHAKE_PAD_1 = {0x36};

    private static final String[] NO_ENCRYPTION = {"SSL_RSA_WITH_NULL_MD5"};

    private static final String[] ENABLED_PROTOCOLS = {"TLSv1", "SSLv3"};

    private static final byte[] DELEGATION_TOKEN =
            new byte[]{GSIConstants.DELEGATION_CHAR};

    /**
     * Handshake state
     */
    protected int state = HANDSHAKE;

    /* handshake states */
    private static final int
            HANDSHAKE = 0,
            CLIENT_START_DEL = 2,
            CLIENT_END_DEL = 3,
            SERVER_START_DEL = 4,
            SERVER_END_DEL = 5;

    /**
     * Delegation state
     */
    protected int delegationState = DELEGATION_START;

    /* delegation states */
    private static final int
            DELEGATION_START = 0,
            DELEGATION_SIGN_CERT = 1,
            DELEGATION_COMPLETE_CRED = 2;

    /**
     * Delegation finished indicator
     */
    protected boolean delegationFinished = false;

    // gss context state variables
    protected boolean credentialDelegation = false;
    protected boolean anonymity = false;
    protected boolean encryption = false;
    protected boolean established = false;

    /**
     * The name of the context initiator
     */
    protected GSSName sourceName = null;

    /**
     * The name of the context acceptor
     */
    protected GSSName targetName = null;

    // these can be set via setOption
    protected GSIConstants.DelegationType delegationType =
            GSIConstants.DelegationType.LIMITED;
    protected Integer gssMode = GSIConstants.MODE_GSI;
    protected Boolean checkContextExpiration = Boolean.FALSE;
    protected Boolean rejectLimitedProxy = Boolean.FALSE;
    protected Boolean requireClientAuth = Boolean.TRUE;
    protected Boolean acceptNoClientCerts = Boolean.FALSE;
    protected Boolean requireAuthzWithDelegation = Boolean.TRUE;

    // *** implementation-specific variables ***

    /**
     * Credential of this context. Might be anonymous
     */
    protected GlobusGSSCredentialImpl ctxCred;

    /**
     * Expected target name. Used for authorization in initiator
     */
    protected GSSName expectedTargetName = null;

    /**
     * Context expiration date.
     */
    protected Date goodUntil = null;

    protected boolean conn = false;

    protected BouncyCastleCertProcessingFactory certFactory;

    protected Map proxyPolicyHandlers;

    /**
     * Limited peer credentials
     */
    protected Boolean peerLimited = null;

    private TlsHandlerUtil tlsHU = null;
    private GlobusTlsClient tlsClient = null;
    private GlobusTlsCipherFactory cipherFactory = null;

    /**
     *
     * @param target
     * @param cred
     * @throws org.ietf.jgss.GSSException
     */
    public BCGSSContextImpl(GSSName target, GlobusGSSCredentialImpl cred)
            throws GSSException {
        if (cred == null) {
            throw new GSSException(GSSException.NO_CRED);
        }

        this.expectedTargetName = target;
        this.ctxCred = cred;
    }

    /**
     *
     * @throws org.globus.gsi.gssapi.GlobusGSSException
     */
    private void init() throws GlobusGSSException {
        this.certFactory = BouncyCastleCertProcessingFactory.getDefault();
        this.state = HANDSHAKE;


        try {
            this.cipherFactory = new GlobusTlsCipherFactory();
            this.tlsClient =
                    new GlobusTlsClient(this.ctxCred.getX509Credential(),
                                        this.cipherFactory);
        } catch (Exception e) {
            throw new GlobusGSSException(GSSException.FAILURE, e);
        }

        // TODO: set enabled cipher suites in client?
        // TODO: enable null encryption ciphers on user request?

        /*
       TlsProtocolVersion[] tlsVersion =
            new TlsProtocolVersion[] {TlsProtocolVersion.TLSv10,
                                      TlsProtocolVersion.SSLv3};
                                      */
            //new TlsProtocolVersion[] {TlsProtocolVersion.TLSv10};
            //new TlsProtocolVersion[] {TlsProtocolVersion.SSLv3};

       //this.tlsHU = new TlsHandlerUtil(this.tlsClient, tlsVersion);
        this.tlsHU = new TlsHandlerUtil(this.tlsClient);
    }

    /**
     *
     * @param cert
     * @return
     * @throws GSSException
     */
    private X509Certificate bcConvert(X509Certificate cert)
            throws GSSException {
        if (!(cert instanceof X509CertificateObject)) {
            try {
                return CertificateLoadUtil.loadCertificate(new ByteArrayInputStream(cert.getEncoded()));
            } catch (Exception e) {
                throw new GlobusGSSException(GSSException.FAILURE, e);
            }
        } else {
            return cert;
        }
    }

    private void handshakeFinished()
            throws IOException {
        //TODO: enable encryption depending on cipher suite decided in handshake
        this.encryption = true;
        //System.out.println("encryption alg: " + cs);
    }

    /**
     *
     */
    private void setDone() {
        this.established = true;
    }

    /**
     *
     * @param date
     */
    private void setGoodUntil(Date date) {
        if (this.goodUntil == null) {
            this.goodUntil = date;
        } else if (date.before(this.goodUntil)) {
            this.goodUntil = date;
        }
    }

    /**
     *
     * @throws GSSException
     */
    protected void checkContext()
            throws GSSException {
        if (!this.conn || !isEstablished()) {
            throw new GSSException(GSSException.NO_CONTEXT);
        }

        if (this.checkContextExpiration && getLifetime() <= 0) {
            throw new GSSException(GSSException.CONTEXT_EXPIRED);
        }
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setGssMode(Object value)
            throws GSSException {
        if (!(value instanceof Integer)) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"GSS mode", Integer.class});
        }
        Integer v = (Integer) value;
        if (v.equals(GSIConstants.MODE_GSI) ||
                v.equals(GSIConstants.MODE_SSL)) {
            this.gssMode = v;
        } else {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION,
                    "badGssMode");
        }
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setDelegationType(Object value)
            throws GSSException {
        GSIConstants.DelegationType v;
        if (value instanceof GSIConstants.DelegationType)
            v = (GSIConstants.DelegationType) value;
        else if (value instanceof Integer) {
            v = GSIConstants.DelegationType.get(((Integer) value).intValue());
        } else {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"delegation type",
                            GSIConstants.DelegationType.class});
        }
        if (v == GSIConstants.DelegationType.FULL ||
                v == GSIConstants.DelegationType.LIMITED) {
            this.delegationType = v;
        } else {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION,
                    "badDelegType");
        }
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setCheckContextExpired(Object value)
            throws GSSException {
        if (!(value instanceof Boolean)) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"check context expired", Boolean.class});
        }
        this.checkContextExpiration = (Boolean) value;
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setRejectLimitedProxy(Object value)
            throws GSSException {
        if (!(value instanceof Boolean)) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"reject limited proxy", Boolean.class});
        }
        this.rejectLimitedProxy = (Boolean) value;
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setRequireClientAuth(Object value)
            throws GSSException {
        if (!(value instanceof Boolean)) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"require client auth", Boolean.class});
        }
        this.requireClientAuth = (Boolean) value;
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setRequireAuthzWithDelegation(Object value)
            throws GSSException {

        if (!(value instanceof Boolean)) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"require authz with delehation",
                            Boolean.class});
        }
        this.requireAuthzWithDelegation = (Boolean) value;
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setAcceptNoClientCerts(Object value)
            throws GSSException {
        if (!(value instanceof Boolean)) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"accept no client certs", Boolean.class});
        }
        this.acceptNoClientCerts = (Boolean) value;
    }

    /**
     *
     * @param value
     * @throws GSSException
     */
    protected void setProxyPolicyHandlers(Object value)
            throws GSSException {
        if (!(value instanceof Map)) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION_TYPE,
                    "badType",
                    new Object[]{"Proxy policy handlers",
                            Map.class});
        }
        this.proxyPolicyHandlers = (Map) value;
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    // Methods below are part of the (Extended)GSSContext implementation

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    /**
     *
     * @param option
     *        option type.
     * @param value
     *        option value.
     * @throws GSSException
     */
    public void setOption(Oid option, Object value) throws GSSException {
        if (option == null) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_ARGUMENT,
                    "nullOption");
        }
        if (value == null) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_ARGUMENT,
                    "nullOptionValue");
        }

        if (option.equals(GSSConstants.GSS_MODE)) {
            setGssMode(value);
        } else if (option.equals(GSSConstants.DELEGATION_TYPE)) {
            setDelegationType(value);
        } else if (option.equals(GSSConstants.CHECK_CONTEXT_EXPIRATION)) {
            setCheckContextExpired(value);
        } else if (option.equals(GSSConstants.REJECT_LIMITED_PROXY)) {
            setRejectLimitedProxy(value);
        } else if (option.equals(GSSConstants.REQUIRE_CLIENT_AUTH)) {
            setRequireClientAuth(value);
        } else if (option.equals(GSSConstants.TRUSTED_CERTIFICATES)) {
            // setTrustedCertificates(value);
            throw new GSSException(GSSException.UNAVAILABLE);
        } else if (option.equals(GSSConstants.PROXY_POLICY_HANDLERS)) {
            setProxyPolicyHandlers(value);
        } else if (option.equals(GSSConstants.ACCEPT_NO_CLIENT_CERTS)) {
            setAcceptNoClientCerts(value);
        } else if (option.equals(GSSConstants
                .AUTHZ_REQUIRED_WITH_DELEGATION)) {
            setRequireAuthzWithDelegation(value);
        } else {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.UNKNOWN_OPTION,
                    "unknownOption",
                    new Object[]{option});
        }
    }

    /**
     *
     * @param option
     * @return
     * @throws GSSException
     */
    public Object getOption(Oid option) throws GSSException {
        if (option == null) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_ARGUMENT,
                    "nullOption");
        }

        if (option.equals(GSSConstants.GSS_MODE)) {
            return this.gssMode;
        } else if (option.equals(GSSConstants.DELEGATION_TYPE)) {
            return this.delegationType;
        } else if (option.equals(GSSConstants.CHECK_CONTEXT_EXPIRATION)) {
            return this.checkContextExpiration;
        } else if (option.equals(GSSConstants.REJECT_LIMITED_PROXY)) {
            return this.rejectLimitedProxy;
        } else if (option.equals(GSSConstants.REQUIRE_CLIENT_AUTH)) {
            return this.requireClientAuth;
        } else if (option.equals(GSSConstants.TRUSTED_CERTIFICATES)) {
            // return this.tc;
            throw new GSSException(GSSException.UNAVAILABLE);
        } else if (option.equals(GSSConstants.PROXY_POLICY_HANDLERS)) {
            // return this.proxyPolicyHandlers;
            throw new GSSException(GSSException.UNAVAILABLE);
        } else if (option.equals(GSSConstants.ACCEPT_NO_CLIENT_CERTS)) {
            return this.acceptNoClientCerts;
        }

        return null;
    }

    /**
     * Initiate the delegation of a credential.
     *
     * This function drives the initiating side of the credential
     * delegation process. It is expected to be called in tandem with the
     * {@link #acceptDelegation(int, byte[], int, int) acceptDelegation}
     * function.
     * <BR>
     * The behavior of this function can be modified by
     * {@link GSSConstants#DELEGATION_TYPE GSSConstants.DELEGATION_TYPE}
     * and
     * {@link GSSConstants#GSS_MODE GSSConstants.GSS_MODE} context
     * options.
     * The {@link GSSConstants#DELEGATION_TYPE GSSConstants.DELEGATION_TYPE}
     * option controls delegation type to be performed. The
     * {@link GSSConstants#GSS_MODE GSSConstants.GSS_MODE}
     * option if set to
     * {@link org.globus.gsi.GSIConstants#MODE_SSL GSIConstants.MODE_SSL}
     * results in tokens that are not wrapped.
     *
     * @param credential
     *        The credential to be delegated. May be null
     *        in which case the credential associated with the security
     *        context is used.
     * @param mechanism
     *        The desired security mechanism. May be null.
     * @param lifetime
     *        The requested period of validity (seconds) of the delegated
     *        credential.
     * @return A token that should be passed to <code>acceptDelegation</code> if
     *         <code>isDelegationFinished</code> returns false. May be null.
     * @exception GSSException containing the following major error codes:
     *            <code>GSSException.FAILURE</code>
     */
    public byte[] initDelegation(GSSCredential credential, Oid mechanism,
                                 int lifetime, byte[] buf, int off, int len)
            throws GSSException {
        //TODO: implement this
        return new byte[0];
    }

    /*
     *  acceptDelegation unimplemented
     */
    public byte[] acceptDelegation(int i, byte[] bytes, int i1, int i2)
            throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /*
        getDelegatedCredential unimplemented (would be set by acceptDelegation)
     */
    public GSSCredential getDelegatedCredential() {
        return null;
    }

    /**
     *
     * @return
     */
    public boolean isDelegationFinished() {
        return this.delegationFinished;
    }

    /**
     * Retrieves arbitrary data about this context.
     * Currently supported oid: <UL>
     * <LI>
     * {@link GSSConstants#X509_CERT_CHAIN GSSConstants.X509_CERT_CHAIN}
     * returns certificate chain of the peer (<code>X509Certificate[]</code>).
     * </LI>
     * </UL>
     *
     * @param oid the oid of the information desired.
     * @return the information desired. Might be null.
     * @exception GSSException containing the following major error codes:
     *            <code>GSSException.FAILURE</code>
     */
    public Object inquireByOid(Oid oid) throws GSSException {
        if (oid == null) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_ARGUMENT,
                    "nullOption");
        }

        if (oid.equals(GSSConstants.X509_CERT_CHAIN)) {
            if (isEstablished()) {
                // converting certs is slower but keeping converted certs
                // takes lots of memory.
                try {
                    Certificate[] peerCerts;
                    //TODO:  used to get this from
                    //  SSLEngine.getSession().getPeerCertificates()
                    peerCerts = null;
                    if (peerCerts != null && peerCerts.length > 0) {
                        return (X509Certificate[]) peerCerts;
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    throw new GlobusGSSException(
                            GSSException.DEFECTIVE_CREDENTIAL,
                            e
                    );
                }
            }
        } else if (oid.equals(GSSConstants.RECEIVED_LIMITED_PROXY)) {
            return this.peerLimited;
        }

        return null;
    }

    public void setBannedCiphers(String[] strings) {
        //To change body of implemented methods use File | Settings | File Templates.
        throw new NotImplementedException();
    }

    /**
     * This function drives the initiating side of the context establishment
     * process. It is expected to be called in tandem with the
     * {@link #acceptSecContext(byte[], int, int) acceptSecContext} function.
     * <BR>
     * The behavior of context establishment process can be modified by
     * {@link GSSConstants#GSS_MODE GSSConstants.GSS_MODE},
     * {@link GSSConstants#DELEGATION_TYPE GSSConstants.DELEGATION_TYPE}, and
     * {@link GSSConstants#REJECT_LIMITED_PROXY GSSConstants.REJECT_LIMITED_PROXY}
     * context options. If the {@link GSSConstants#GSS_MODE GSSConstants.GSS_MODE}
     * option is set to
     * {@link org.globus.gsi.GSIConstants#MODE_SSL GSIConstants.MODE_SSL}
     * the context establishment process will be compatible with regular SSL
     * (no credential delegation support). If the option is set to
     * {@link org.globus.gsi.GSIConstants#MODE_GSI GSIConstants.GSS_MODE_GSI}
     * credential delegation during context establishment process will performed.
     * The delegation type to be performed can be set using the
     * {@link GSSConstants#DELEGATION_TYPE GSSConstants.DELEGATION_TYPE}
     * context option. If the {@link GSSConstants#REJECT_LIMITED_PROXY
     * GSSConstants.REJECT_LIMITED_PROXY} option is enabled,
     * a peer presenting limited proxy credential will be automatically
     * rejected and the context establishment process will be aborted.
     *
     * @return a byte[] containing the token to be sent to the peer.
     *         null indicates that no token is generated (needs more data).
     */
    public byte[] initSecContext(byte[] inBuff, int off, int len)
            throws GSSException {

        if (!this.conn) {
            //System.out.println("enter initializing in initSecContext");
            if (this.credentialDelegation) {
                if (this.gssMode.equals(GSIConstants.MODE_SSL)) {
                    throw new GlobusGSSException(GSSException.FAILURE,
                            GlobusGSSException.BAD_ARGUMENT,
                            "initCtx00");
                }
                if (this.anonymity) {
                    throw new GlobusGSSException(GSSException.FAILURE,
                            GlobusGSSException.BAD_ARGUMENT,
                            "initCtx01");
                }
            }

            if (this.anonymity || this.ctxCred.getName().isAnonymous()) {
                this.anonymity = true;
            } else {
                this.anonymity = false;

                if (ctxCred.getUsage() != GSSCredential.INITIATE_ONLY &&
                    ctxCred.getUsage() != GSSCredential.INITIATE_AND_ACCEPT) {
                    throw new GlobusGSSException(
                            GSSException.DEFECTIVE_CREDENTIAL,
                            GlobusGSSException.UNKNOWN,
                            "badCredUsage");
                }
            }

            init();

            this.conn = true;
        }

        // Unless explicitly disabled, check if delegation is
        // requested and expected target is null
        if (!Boolean.FALSE.equals(this.requireAuthzWithDelegation)) {

            if (this.expectedTargetName == null &&
                    this.credentialDelegation) {
                throw new GlobusGSSException(GSSException.FAILURE,
                        GlobusGSSException.BAD_ARGUMENT,
                        "initCtx02");
            }
        }

        byte[] returnToken = null;

        switch (state) {
            case HANDSHAKE:
                try {
                    returnToken = this.tlsHU.nextHandshakeToken(inBuff);

                    if (this.tlsHU.isHandshakeFinished()) {
                        //System.out.println("initSecContext handshake finished");
                        handshakeFinished(); // just enable encryption

                        Certificate[] chain = this.tlsClient.getPeerCerts();
                        if (!(chain instanceof X509Certificate[])) {
                            throw new Exception(
                               "Certificate chain not of type X509Certificate");
                        }

                        for (X509Certificate cert : (X509Certificate[]) chain) {
                            setGoodUntil(cert.getNotAfter());
                        }

                        String identity = BouncyCastleUtil.getIdentity(
                                bcConvert(
                                        BouncyCastleUtil.getIdentityCertificate(
                                                (X509Certificate[]) chain)));
                        this.targetName =
                                new GlobusGSSName(CertificateUtil.toGlobusID(
                                        identity, false));

                        this.peerLimited = ProxyCertificateUtil.isLimitedProxy(
                                BouncyCastleUtil.getCertificateType(
                                        (X509Certificate) chain[0]));

                        // initiator
                        if (this.anonymity) {
                            this.sourceName = new GlobusGSSName();
                        } else {
                            for (X509Certificate cert :
                                    ctxCred.getCertificateChain()) {
                                setGoodUntil(cert.getNotAfter());
                            }
                            this.sourceName = this.ctxCred.getName();
                        }

                        // mutual authentication test
                        if (this.expectedTargetName != null &&
                           !this.expectedTargetName.equals(this.targetName)) {
                            throw new GlobusGSSException(
                                    GSSException.UNAUTHORIZED,
                                    GlobusGSSException.BAD_NAME,
                                    "authFailed00",
                                    new Object[]{this.expectedTargetName,
                                            this.targetName});
                        }

                        if (this.gssMode.equals(GSIConstants.MODE_GSI)) {
                            this.state = CLIENT_START_DEL;
                            // if there is a token to return then break
                            // otherwise we fall through to delegation
                            if (returnToken != null && returnToken.length > 0) {
                                break;
                            }
                        } else {
                            setDone();
                            break;
                        }

                    } else { // handshake not complete yet
                        break;
                    }
                } catch (IOException e) {
                    throw new GlobusGSSException(GSSException.FAILURE, e);
                } catch (Exception e) {
                    throw new GlobusGSSException(GSSException.FAILURE, e);
                }

            case CLIENT_START_DEL:

                // sanity check - might be invalid state
                if (this.state != CLIENT_START_DEL ||
                        (returnToken != null && returnToken.length > 0) ) {
                    throw new GSSException(GSSException.FAILURE);
                }

                try {
                    String deleg;

                    if (getCredDelegState()) {
                        deleg = Character.toString(
                                GSIConstants.DELEGATION_CHAR);
                        this.state = CLIENT_END_DEL;
                    } else {
                        deleg = Character.toString('0');
                        setDone();
                    }

                    // TODO: Force ASCII encoding?
                    byte[] a = deleg.getBytes();
                    // SSL wrap the delegation token
                    returnToken = this.tlsHU.wrap(a);
                } catch (Exception e) {
                    throw new GlobusGSSException(GSSException.FAILURE, e);
                }

                break;

            case CLIENT_END_DEL:

                if (inBuff == null || inBuff.length == 0) {
                    throw new GSSException(GSSException.DEFECTIVE_TOKEN);
                }

                try {
                    // SSL unwrap the token on the inBuff (it's a CSR)
                    byte[] certReq = this.tlsHU.unwrap(inBuff);

                    if (certReq.length == 0) break;

                    X509Certificate[] chain =
                            this.ctxCred.getCertificateChain();

                    X509Certificate cert = this.certFactory.createCertificate(
                            new ByteArrayInputStream(certReq),
                            chain[0],
                            this.ctxCred.getPrivateKey(),
                            -1,
                            BouncyCastleCertProcessingFactory.decideProxyType(
                                    chain[0], this.delegationType));

                    byte[] enc = cert.getEncoded();
                    // SSL wrap the encoded cert and return that buffer
                    returnToken = this.tlsHU.wrap(enc);
                    setDone();
                } catch (GeneralSecurityException e) {
                    throw new GlobusGSSException(GSSException.FAILURE, e);
                } catch (IOException e) {
                    throw new GlobusGSSException(GSSException.FAILURE, e);
                }

                break;

            default:
                throw new GSSException(GSSException.FAILURE);
        }

        //TODO: Why is there a check for CLIENT_START_DEL?
        if (returnToken != null && returnToken.length > 0 ||
            this.state == CLIENT_START_DEL) {
            return returnToken;
        } else
            return null;
    }

    /**
     * It works just like
     * {@link #initSecContext(byte[], int, int) initSecContext} method.
     * It reads one SSL token from input stream, calls
     * {@link #initSecContext(byte[], int, int) acceptSecContext} method and
     * writes the output token to the output stream (if any)
     * SSL token is not read on the initial call.
     */
    public int initSecContext(InputStream in, OutputStream out)
            throws GSSException {
        byte[] inToken = null;
        try {
            if (!this.conn) {
                inToken = new byte[0];
            } else {
                inToken = SSLUtil.readSslMessage(in);
            }
            byte[] outToken = initSecContext(inToken, 0, inToken.length);
            if (outToken != null) {
                out.write(outToken);
                return outToken.length;
            } else {
                return 0;
            }
        } catch (IOException e) {
            throw new GlobusGSSException(GSSException.FAILURE, e);
        }
    }

    /*
        acceptSecContext not implemented
     */
    public byte[] acceptSecContext(byte[] bytes, int i, int i1)
            throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /*
        acceptSecContext not implemented
     */
    public void acceptSecContext(InputStream in, OutputStream out)
            throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     *
     * @return
     */
    public boolean isEstablished() {
        return this.established;
    }

    /**
     *
     * @throws GSSException
     */
    public void dispose() throws GSSException {
        // does nothing
    }

    /*
        getWrapSizeLimit unimplemented
     */
    public int getWrapSizeLimit(int i, boolean b, int i1) throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }


    /**
     * Wraps a message for integrity and protection.
     * Returns a GSI-wrapped token when privacy is not requested and
     * QOP requested is set to
     * {@link GSSConstants#GSI_BIG GSSConstants.GSI_BIG}. Otherwise
     * a regular SSL-wrapped token is returned.
     */
    public byte[] wrap(byte[] inBuf, int off, int len, MessageProp prop)
            throws GSSException {
        checkContext();

        byte[] token = null;
        boolean doGSIWrap = false;

        if (prop != null) {
            if (prop.getQOP() != 0 && prop.getQOP() != GSSConstants.GSI_BIG) {
                throw new GSSException(GSSException.BAD_QOP);
            }
            doGSIWrap = (!prop.getPrivacy() &&
                    prop.getQOP() == GSSConstants.GSI_BIG);
        }

        if (doGSIWrap) {
            throw new GSSException(GSSException.UNAVAILABLE);
        } else {
            try {
                token = this.tlsHU.wrap(inBuf, off, len);
            } catch (IOException e) {
                throw new GlobusGSSException(GSSException.FAILURE, e);
            }

            if (prop != null) {
                prop.setPrivacy(this.encryption);
                prop.setQOP(0);
            }
        }

        return token;
    }

    /*
        wrap(InputStream, OutputStream) unimplemented
     */
    public void wrap(InputStream in, OutputStream out, MessageProp msgProp)
            throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     * Unwraps a token generated by <code>wrap</code> method on the other side
     * of the context.  The input token can either be a regular SSL-wrapped
     * token or GSI-wrapped token. Upon return from the method the
     * <code>MessageProp</code> object will contain the applied QOP and privacy
     * state of the message. In case of GSI-wrapped token the applied QOP will
     * be set to {@link GSSConstants#GSI_BIG GSSConstants.GSI_BIG}
     */
    public byte[] unwrap(byte[] inBuf, int off, int len, MessageProp prop)
            throws GSSException {
        checkContext();

        byte[] token = null;

        /*
         * see if the token is a straight SSL packet or
         * one of ours made by wrap using get_mic
         */
        if (inBuf[off] == GSI_WRAP &&
                inBuf[off + 1] == 3 &&
                inBuf[off + 2] == 0) {
            throw new GSSException(GSSException.UNAVAILABLE);
        } else {
            try {
                token = this.tlsHU.unwrap(inBuf, off, len);
            } catch (IOException e) {
                throw new GlobusGSSException(GSSException.FAILURE, e);
            }

            if (prop != null) {
                prop.setPrivacy(this.encryption);
                prop.setQOP(0);
            }
        }

        return token;
    }

    /*
        unwrap(InputStream, OutputStream) unimplemented
     */
    public void unwrap(InputStream in, OutputStream out, MessageProp msgProp)
            throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     *
     * @param inBuf
     * @param off
     * @param len
     * @param prop
     * @return
     * @throws GSSException
     */
    public byte[] getMIC(byte[] inBuf, int off, int len, MessageProp prop)
            throws GSSException {
        checkContext();

        if (prop != null && (prop.getQOP() != 0 || prop.getPrivacy())) {
            throw new GSSException(GSSException.BAD_QOP);
        }

        long sequence = this.cipherFactory.getTlsBlockCipher().getWriteMac()
                .getSequenceNumber();

        int md_size = this.cipherFactory.getDigest().getDigestSize();

        byte[] mic = new byte[GSI_MESSAGE_DIGEST_PADDING + md_size];

        System.arraycopy(toBytes(sequence), 0, mic, 0, GSI_SEQUENCE_SIZE);
        System.arraycopy(toBytes(len, 4), 0, mic, GSI_SEQUENCE_SIZE, 4);

        this.cipherFactory.getTlsBlockCipher().getWriteMac().incSequence();

        int pad_ct = (48 / md_size) * md_size;

        try {
            MessageDigest md = MessageDigest.getInstance(
                    this.cipherFactory.getDigest().getAlgorithmName());

            md.update(this.cipherFactory.getTlsBlockCipher().getWriteMac()
                    .getMACSecret());
            for (int i = 0; i < pad_ct; i++) {
                md.update(SSLHANDSHAKE_PAD_1);
            }
            md.update(mic, 0, GSI_MESSAGE_DIGEST_PADDING);
            md.update(inBuf, off, len);

            byte[] digest = md.digest();

            System.arraycopy(digest, 0, mic, GSI_MESSAGE_DIGEST_PADDING, digest.length);
        } catch (NoSuchAlgorithmException e) {
            throw new GlobusGSSException(GSSException.FAILURE, e);
        }

        if (prop != null) {
            prop.setPrivacy(false);
            prop.setQOP(0);
        }

        return mic;
    }

    /*
        getMIC(InputStream, OutputStream) unimplemented
     */
    public void getMIC(InputStream in, OutputStream out, MessageProp msgProp)
            throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     *
     * @param inTok
     * @param tokOff
     * @param tokLen
     * @param inMsg
     * @param msgOff
     * @param msgLen
     * @param prop
     * @throws GSSException
     */
    public void verifyMIC(byte[] inTok, int tokOff, int tokLen,
                          byte[] inMsg, int msgOff, int msgLen,
                          MessageProp prop) throws GSSException {
                checkContext();

        String digestAlg = this.cipherFactory.getDigest().getAlgorithmName();
        int md_size = this.cipherFactory.getDigest().getDigestSize();

        if (tokLen != (GSI_MESSAGE_DIGEST_PADDING + md_size)) {
            throw new GlobusGSSException(GSSException.DEFECTIVE_TOKEN,
                                         GlobusGSSException.TOKEN_FAIL,
                                         "tokenFail00",
                                         new Object[] {new Integer(tokLen),
                                                       new Integer(GSI_MESSAGE_DIGEST_PADDING +
                                                                   md_size)});
        }

        int bufLen = SSLUtil.toInt(inTok, tokOff + GSI_SEQUENCE_SIZE);
        if (bufLen != msgLen) {
            throw new GlobusGSSException(GSSException.DEFECTIVE_TOKEN,
                                         GlobusGSSException.TOKEN_FAIL,
                                         "tokenFail01",
                                         new Object[] {new Integer(msgLen), new Integer(bufLen)});
        }

        int pad_ct = (48 / md_size) * md_size;

        byte [] digest = null;

        try {
            MessageDigest md =
                MessageDigest.getInstance(digestAlg);

            md.update(this.cipherFactory.getTlsBlockCipher().getReadMac()
                    .getMACSecret());
            for(int i=0;i<pad_ct;i++) {
                md.update(SSLHANDSHAKE_PAD_1);
            }
            md.update(inTok, tokOff, GSI_MESSAGE_DIGEST_PADDING);
            md.update(inMsg, msgOff, msgLen);

            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new GlobusGSSException(GSSException.FAILURE, e);
        }

        byte [] token = new byte[tokLen-GSI_MESSAGE_DIGEST_PADDING];
        System.arraycopy(inTok, tokOff+GSI_MESSAGE_DIGEST_PADDING, token, 0, token.length);

        if (!Arrays.equals(digest, token)) {
            throw new GlobusGSSException(GSSException.BAD_MIC,
                                         GlobusGSSException.BAD_MIC,
                                         "tokenFail02");
        }

        long tokSeq = SSLUtil.toLong(inTok, tokOff);
        long readSeq = this.cipherFactory.getTlsBlockCipher().getReadMac()
                .getSequenceNumber();
        long seqTest = tokSeq - readSeq;

        if (seqTest > 0) {
            // gap token
            throw new GSSException(GSSException.GAP_TOKEN);
        } else if (seqTest < 0) {
            // old token
            throw new GSSException(GSSException.OLD_TOKEN);
        } else {
            this.cipherFactory.getTlsBlockCipher().getReadMac().incSequence();
        }

        if (prop != null) {
            prop.setPrivacy(false);
            prop.setQOP(0);
        }
    }

    /*
        verifyMIC(InputStream, InputStream) unimplemented
    */
    public void verifyMIC(InputStream tokStream, InputStream msgStream,
                          MessageProp msgProp) throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /*
        export not implemented
     */
    public byte[] export() throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     *
     * @param state
     * @throws GSSException
     */
    public void requestMutualAuth(boolean state) throws GSSException {
        if (!state) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION,
                    "mutualAuthOn");
        }
    }

    /**
     *
     * @param state
     * @throws GSSException
     */
    public void requestReplayDet(boolean state) throws GSSException {
        if (!state) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION,
                    "replayDet");
        }
    }

    /**
     *
     * @param state
     * @throws GSSException
     */
    public void requestSequenceDet(boolean state) throws GSSException {
        if (!state) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION,
                    "seqDet");
        }
    }

    /**
     *
     * @param state
     * @throws GSSException
     */
    public void requestCredDeleg(boolean state) throws GSSException {
        this.credentialDelegation = state;
    }

    /**
     *
     * @param state
     * @throws GSSException
     */
    public void requestAnonymity(boolean state) throws GSSException {
        this.anonymity = state;
    }

    /**
     *
     * @param state
     * @throws GSSException
     */
    public void requestConf(boolean state) throws GSSException {
        //TODO: unencrypted not possible
        this.encryption = true;
    }

    /**
     *
     * @param state
     * @throws GSSException
     */
    public void requestInteg(boolean state) throws GSSException {
        if (!state) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.BAD_OPTION,
                    "integOn");
        }
    }

    /**
     *
     * @param lifetime
     * @throws GSSException
     */
    public void requestLifetime(int lifetime) throws GSSException {
        if (lifetime == GSSContext.INDEFINITE_LIFETIME) {
            throw new GlobusGSSException(GSSException.FAILURE,
                    GlobusGSSException.UNKNOWN,
                    "badLifetime00");
        }

        if (lifetime != GSSContext.DEFAULT_LIFETIME) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, lifetime);
            setGoodUntil(calendar.getTime());
        }
    }

    /*
        setChannelBinding unimplemented
     */
    public void setChannelBinding(ChannelBinding cb) throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     *
     * @return
     */
    public boolean getCredDelegState() {
        return this.credentialDelegation;
    }

    /**
     *
     * @return
     */
    public boolean getMutualAuthState() {
        return true;  // always on with gsi
    }

    /**
     *
     * @return
     */
    public boolean getReplayDetState() {
        return true;  // always on with ssl
    }

    /**
     *
     * @return
     */
    public boolean getSequenceDetState() {
        return true;  // always on with ssl
    }

    /**
     *
     * @return
     */
    public boolean getAnonymityState() {
        return this.anonymity;
    }

    /*
        isTransferable unimplemented
     */
    public boolean isTransferable() throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     *
     * @return
     */
    public boolean isProtReady() {
        return isEstablished();
    }

    /**
     *
     * @return
     */
    public boolean getConfState() {
        return this.encryption;
    }

    public boolean getIntegState() {
        return true;  // always on with ssl
    }

    /**
     *
     * @return
     */
    public int getLifetime() {
        if (goodUntil != null) {
            return (int) ((goodUntil.getTime() - System.currentTimeMillis())
                    / 1000);
        } else {
            return -1;
        }
    }

    /**
     *
     * @return
     * @throws GSSException
     */
    public GSSName getSrcName() throws GSSException {
        return this.sourceName;
    }

    /**
     *
     * @return
     * @throws GSSException
     */
    public GSSName getTargName() throws GSSException {
        return this.targetName;
    }

    /**
     *
     * @return
     * @throws GSSException
     */
    public Oid getMech() throws GSSException {
        return GSSConstants.MECH_OID;
    }

    /*
        getDelegCred unimplemented (would have been set by acceptSecContext
     */
    public GSSCredential getDelegCred() throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE);
    }

    /**
     *
     * @return
     * @throws GSSException
     */
    public boolean isInitiator() throws GSSException {
        return true;  // acceptor side currently isn't implemented
    }

    /**
     *
     * @param val
     * @return
     */
    public static byte[] toBytes(long val){
        return toBytes(val,8);
    }

    /**
     *
     * @param val
     * @return
     */
    public static byte[] toBytes(short val){
        return toBytes((long)val,2);
    }

    /**
     *
     * @param val
     * @param bytes
     * @return
     */
    public static byte[] toBytes(long val,int bytes){
        byte[] retval=new byte[bytes];

        while(bytes-->0){
            retval[bytes]=(byte)(val & 0xff);
            val>>=8;
        }

        return retval;
    }
}

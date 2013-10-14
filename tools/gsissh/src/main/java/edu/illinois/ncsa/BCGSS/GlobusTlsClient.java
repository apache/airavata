package edu.illinois.ncsa.BCGSS;

import edu.illinois.ncsa.bouncycastle.crypto.tls.*;
import edu.illinois.ncsa.bouncycastle.asn1.*;
import edu.illinois.ncsa.bouncycastle.asn1.x509.*;
import org.globus.common.CoGProperties;
import org.globus.gsi.CredentialException;
import org.globus.gsi.X509Credential;
import org.globus.gsi.X509ProxyCertPathParameters;
import org.globus.gsi.provider.GlobusProvider;
import org.globus.gsi.provider.KeyStoreParametersFactory;
import org.globus.gsi.stores.ResourceCertStoreParameters;
import org.globus.gsi.stores.ResourceSigningPolicyStore;
import org.globus.gsi.stores.ResourceSigningPolicyStoreParameters;
import org.globus.gsi.trustmanager.X509ProxyCertPathValidator;
import org.globus.gsi.util.CertificateUtil;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class GlobusTlsClient extends DefaultTlsClient
{
    private Certificate clientCert = new Certificate(new X509CertificateStructure[0]);
    private PrivateKey clientPrivateKey = null;
    private X509Certificate[] peerCerts = null;

    public X509Certificate[] getPeerCerts() {
        return peerCerts;
    }

    public GlobusTlsClient(X509Credential cred, GlobusTlsCipherFactory factory)
            throws IOException, CertificateException, CredentialException {
        super(factory);
        if (cred == null) {
            throw new IllegalArgumentException("'cred' cannot be null");
        }

        clientCert = new Certificate(
                X509CertArrayToStructArray(cred.getCertificateChain()));
        clientPrivateKey = cred.getPrivateKey();

        if (clientCert.getCerts().length == 0) {
            throw new IllegalArgumentException(
                    "'cred' contains no certificates");
        }

        if (clientPrivateKey == null) {
            throw new IllegalArgumentException("'clientPrivateKey' cannot be null");
        }
    }

    public TlsAuthentication getAuthentication() throws IOException {
        return new GlobusTlsAuth();
    }

    public int[] getCipherSuites() {
        return new int[] {
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
        };
    }

    public class GlobusTlsAuth implements TlsAuthentication {

        /**
         * Validates the server's certificate
         * @param certificate received from server
         * @throws IOException
         */
        public void notifyServerCertificate(Certificate certificate)
                throws IOException {
            try {
            peerCerts = X509CertStructArrayToCertArray(certificate.getCerts());

            String caCertsLocation =
                    "file:" + CoGProperties.getDefault().getCaCertLocations();
            String crlPattern = caCertsLocation + "/*.r*";
            String sigPolPattern = caCertsLocation + "/*.signing_policy";

            KeyStore keyStore = KeyStore.getInstance(
                    GlobusProvider.KEYSTORE_TYPE, GlobusProvider.PROVIDER_NAME);
            CertStore crlStore = CertStore.getInstance(
                    GlobusProvider.CERTSTORE_TYPE,
                    new ResourceCertStoreParameters(null, crlPattern));
            ResourceSigningPolicyStore sigPolStore =
                    new ResourceSigningPolicyStore(
                            new ResourceSigningPolicyStoreParameters(
                                    sigPolPattern));
            keyStore.load(
                    KeyStoreParametersFactory.createTrustStoreParameters(
                            caCertsLocation));
            X509ProxyCertPathParameters parameters =
                    new X509ProxyCertPathParameters(keyStore, crlStore,
                            sigPolStore, false);
            X509ProxyCertPathValidator validator =
                    new X509ProxyCertPathValidator();
            if (validator.engineValidate(CertificateUtil.getCertPath(peerCerts),
                    parameters) == null) {
                throw new Exception("X509ProxyCertPathValidator did not return a result");
            }
            } catch (Exception e) {
                e.printStackTrace();
                throw new TlsFatalAlert(AlertDescription.user_canceled);
            }
        }

        /**
         * Returns an object representing the client's credentials
         * @param request
         * @return the client's credentials
         * @throws IOException
         */
        public TlsCredentials getClientCredentials(CertificateRequest request)
                throws IOException {
            return new GlobusTlsCred();
        }
    }

    public class GlobusTlsCred implements TlsSignerCredentials {
        /**
         * Encrypts a hash with the client's private key, producing a signature
         * @param md5andsha1 the hash to encrypt
         * @return an array of bytes containing the signature
         * @throws IOException
         */
        public byte[] generateCertificateSignature(byte[] md5andsha1)
                throws IOException {
            // encrypt the input hash with the private key to produce signature
            try {
                Cipher cipher = Cipher.getInstance(clientPrivateKey.getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, clientPrivateKey);
                return cipher.doFinal(md5andsha1);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException(e);
            }
        }

        public Certificate getCertificate() {
            return clientCert;
        }
    }

       /**
     *
     * @param struct
     * @return
     * @throws CertificateException
     * @throws IOException
     */
    public static X509Certificate X509CertStructToCert(
            X509CertificateStructure struct) throws CertificateException,
            IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(struct.getEncoded());
        X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
        is.close();
        return cert;
    }

    /**
     *
     * @param structs
     * @return
     * @throws java.io.IOException
     * @throws java.security.cert.CertificateException
     */
    public static X509Certificate[] X509CertStructArrayToCertArray(
            X509CertificateStructure[] structs) throws IOException,
            CertificateException {
        X509Certificate[] certChain = new X509Certificate[structs.length];

        for (int i = 0; i < structs.length; ++i) {
            certChain[i] = X509CertStructToCert(structs[i]);
        }

        return certChain;
    }

    /**
     *
     * @param c
     * @return
     * @throws CertificateException
     * @throws IOException
     */
    public static X509CertificateStructure X509CertToStruct(X509Certificate c)
            throws CertificateException, IOException {
        ASN1InputStream is = new ASN1InputStream(c.getEncoded());
        DERObject o = is.readObject();
        return X509CertificateStructure.getInstance(o);
    }


    /**
     *
     * @param certs
     * @return
     * @throws CertificateException
     * @throws IOException
     */
    public static X509CertificateStructure[] X509CertArrayToStructArray(
            X509Certificate[] certs) throws CertificateException, IOException {
        X509CertificateStructure[] structs =
                new X509CertificateStructure[certs.length];

        for (int i = 0; i < certs.length; ++i) {
            structs[i] = X509CertToStruct(certs[i]);
        }

        return structs;
    }
}


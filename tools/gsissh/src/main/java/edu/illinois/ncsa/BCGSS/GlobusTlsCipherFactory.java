package edu.illinois.ncsa.BCGSS;

import edu.illinois.ncsa.bouncycastle.crypto.Digest;
import edu.illinois.ncsa.bouncycastle.crypto.tls.AlertDescription;
import edu.illinois.ncsa.bouncycastle.crypto.tls.DefaultTlsCipherFactory;
import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsBlockCipher;
import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsCipher;
import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsClientContext;
import edu.illinois.ncsa.bouncycastle.crypto.tls.TlsFatalAlert;

import java.io.IOException;

public class GlobusTlsCipherFactory extends DefaultTlsCipherFactory {
    protected TlsBlockCipher tlsBlockCipher;
    protected Digest digest;

    public TlsBlockCipher getTlsBlockCipher() {
        return tlsBlockCipher;
    }

    public Digest getDigest() {
        return digest;
    }

    public TlsCipher createCipher(TlsClientContext context,
                                     int encAlg, int digestAlg)
            throws IOException {
        TlsCipher cipher = super.createCipher(context, encAlg, digestAlg);
        if (cipher instanceof TlsBlockCipher) {
            tlsBlockCipher = (TlsBlockCipher) cipher;
        } else {
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }

        return cipher;
    }

    protected Digest createDigest(int digestAlgorithm) throws IOException {
        digest = super.createDigest(digestAlgorithm);
        return digest;
    }
}

package org.apache.airavata.helix.agent.ssh;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.PublicKey;
import java.util.Base64;

public final class SSHUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHUtil.class);

    private SSHUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String generatePublicKey(String privateKeyPEM) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(privateKeyPEM))) {
            Object pemObject = pemParser.readObject();
            PEMKeyPair keyPair = (PEMKeyPair) pemObject;
            SubjectPublicKeyInfo spki = keyPair.getPublicKeyInfo();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PublicKey publicKey = converter.getPublicKey(spki);

            java.security.interfaces.RSAPublicKey rsaPublicKey = (java.security.interfaces.RSAPublicKey) publicKey;

            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-rsa".getBytes().length);
            dos.write("ssh-rsa".getBytes());
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());

            return "ssh-rsa " + Base64.getEncoder().encodeToString(byteOs.toByteArray()) + " airavata-generated";
        } catch (IOException e) {
            LOGGER.error("Error while generating the public", e);
            throw new Exception("Error while generating the public key");
        }
    }
}

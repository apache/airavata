package org.apache.airavata.tools.load;

import javax.net.ssl.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class SecurityManager {

    private String trustStoreName = "client_truststore.jks";
    private String trustStorePassword = "airavata";

    public void loadCertificate(String host, int port) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException, URISyntaxException {

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(host, port);
        socket.startHandshake();
        SSLSession sslSession = socket.getSession();
        Certificate[] certificates = sslSession.getPeerCertificates();

        FileInputStream is = new FileInputStream(getTrustStorePath());

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, trustStorePassword.toCharArray());
        is.close();

        File keystoreFile = new File(getTrustStorePath());

        String certificateAlias = host;
        keystore.setCertificateEntry(certificateAlias, certificates[0]);

        FileOutputStream out = new FileOutputStream(keystoreFile);
        keystore.store(out, trustStorePassword.toCharArray());
        out.close();

        System.out.println("Certificates successfully loaded for " + host + ":" + port);
    }

    public String getTrustStorePath() throws URISyntaxException {
        URL trustStoreUrl = SecurityManager.class.getClassLoader().getResource(trustStoreName);

        String trustStorePath;
        if (trustStoreUrl.toURI().getPath() != null) {
            trustStorePath = trustStoreUrl.toURI().getPath();
        } else {
            trustStorePath = System.getProperty("airavata.home") + "/bin/" + trustStoreName;
        }
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }
}
